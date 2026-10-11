package com.app.product.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Local Spark 4.2 lives at /opt/spark. The import job jar is submitted with
 * that install's spark-submit so Spark never shares a classpath with this service.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "product.import")
public class ProductImportProperties {

    private String storageDir = "/tmp/product-imports";
    private String sparkSubmit = "/opt/spark/bin/spark-submit";
    private String jobJar = "product-import-job/target/product-import-job-2.0.0.jar";
}
