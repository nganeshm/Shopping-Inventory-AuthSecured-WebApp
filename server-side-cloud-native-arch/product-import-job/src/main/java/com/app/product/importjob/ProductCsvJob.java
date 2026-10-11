package com.app.product.importjob;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.functions;

import java.util.Arrays;
import java.util.List;

/**
 * Reads one product CSV with the local Spark install and splits it into
 * valid and rejected JSON lines. Referential checks (category exists, sku
 * already stored) stay in product-service, which owns those tables.
 *
 * Usage: ProductCsvJob &lt;csv-path&gt; &lt;output-dir&gt;
 */
public final class ProductCsvJob {

    static final List<String> COLUMNS = List.of(
            "sku", "name", "description", "price", "categoryName", "initialQuantity", "warehouseLocation");

    private ProductCsvJob() {
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("IMPORT_ERROR: usage: ProductCsvJob <csv-path> <output-dir>");
            System.exit(2);
        }

        SparkSession spark = SparkSession.builder()
                .appName("product-csv-import")
                .master("local[*]")
                .config("spark.driver.host", "127.0.0.1")
                .config("spark.driver.bindAddress", "127.0.0.1")
                .config("spark.ui.enabled", "false")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        int exit = 1;
        try {
            exit = run(spark, args[0], args[1]);
        } catch (Exception ex) {
            System.err.println("IMPORT_ERROR: " + ex.getMessage());
        } finally {
            spark.stop();
        }
        System.exit(exit);
    }

    static int run(SparkSession spark, String csvPath, String outputDir) {
        Dataset<Row> raw = spark.read()
                .option("header", "true")
                .option("encoding", "UTF-8")
                .option("inferSchema", "false")
                .csv(csvPath);

        List<String> present = Arrays.asList(raw.columns());
        List<String> missing = COLUMNS.stream().filter(column -> !present.contains(column)).toList();
        if (!missing.isEmpty()) {
            System.err.println("IMPORT_ERROR: CSV header must be: " + String.join(",", COLUMNS));
            return 2;
        }

        Dataset<Row> trimmed = raw;
        for (String column : COLUMNS) {
            trimmed = trimmed.withColumn(column, functions.trim(functions.col(column)));
            trimmed = trimmed.withColumn(column,
                    functions.when(functions.col(column).equalTo(""), functions.lit(null))
                            .otherwise(functions.col(column)));
        }

        Column anyValue = functions.coalesce(COLUMNS.stream().map(functions::col).toArray(Column[]::new));
        Dataset<Row> numbered = trimmed.withColumn("rowNumber", functions.row_number()
                .over(Window.orderBy(functions.monotonically_increasing_id()))
                .plus(1));
        Dataset<Row> rows = numbered
                .filter(anyValue.isNotNull())
                .withColumn("_skuRank", functions.row_number()
                        .over(Window.partitionBy(functions.col("sku")).orderBy(functions.col("rowNumber"))));

        Dataset<Row> marked = rows.withColumn("reason", functions.concat_ws("; ",
                functions.when(functions.col("sku").isNull(), functions.lit("sku is required"))
                        .when(functions.length(functions.col("sku")).gt(40), functions.lit("sku must be at most 40 characters")),
                functions.when(functions.col("sku").isNotNull().and(functions.col("_skuRank").gt(1)),
                        functions.lit("duplicate sku in file")),
                functions.when(functions.col("name").isNull(), functions.lit("name is required"))
                        .when(functions.length(functions.col("name")).gt(150), functions.lit("name must be at most 150 characters")),
                functions.when(functions.length(functions.col("description")).gt(1000),
                        functions.lit("description must be at most 1000 characters")),
                priceProblem(),
                functions.when(functions.col("categoryName").isNull(), functions.lit("categoryName is required")),
                functions.when(functions.col("initialQuantity").isNotNull()
                                .and(functions.col("initialQuantity").rlike("^\\d+$").equalTo(false)),
                        functions.lit("initialQuantity must be an integer >= 0")),
                functions.when(functions.length(functions.col("warehouseLocation")).gt(100),
                        functions.lit("warehouseLocation must be at most 100 characters"))
        )).cache();

        Dataset<Row> valid = marked.filter(functions.col("reason").equalTo(""))
                .select("rowNumber", "sku", "name", "description", "price",
                        "categoryName", "initialQuantity", "warehouseLocation");
        Dataset<Row> rejected = marked.filter(functions.length(functions.col("reason")).gt(0))
                .select("rowNumber", "reason");

        write(valid, outputDir + "/valid");
        write(rejected, outputDir + "/rejected");
        marked.unpersist();
        return 0;
    }

    private static Column priceProblem() {
        Column price = functions.col("price");
        return functions.when(price.isNull(), functions.lit("price is required"))
                .when(price.rlike("^\\d{1,10}(\\.\\d{1,2})?$").equalTo(false),
                        functions.lit("price must be greater than 0 with at most 2 decimal places"))
                .when(price.cast("decimal(12,2)").leq(functions.lit(0)),
                        functions.lit("price must be greater than 0"));
    }

    private static void write(Dataset<Row> data, String path) {
        data.repartition(1)
                .sortWithinPartitions("rowNumber")
                .write()
                .mode(SaveMode.Overwrite)
                .json(path);
    }
}
