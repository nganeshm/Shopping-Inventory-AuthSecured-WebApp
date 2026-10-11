package com.app.product.service;

import java.nio.file.Path;

/**
 * Turns a product CSV into JSON lines using the local Spark install.
 * Implementations must not link Spark into this service's JVM.
 */
public interface SparkCsvExtractor {

    /**
     * @param csvFile  headered CSV
     * @param workDir  receives {@code valid/} and {@code rejected/} JSON output
     */
    void extract(Path csvFile, Path workDir);
}
