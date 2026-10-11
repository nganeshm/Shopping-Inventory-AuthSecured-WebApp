package com.app.product.service.impl;

import com.app.product.config.ProductImportProperties;
import com.app.product.exception.BadProductImportException;
import com.app.product.service.SparkCsvExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalSparkCsvExtractor implements SparkCsvExtractor {

    private static final String SPARK_HOME = "/opt/spark";

    private final ProductImportProperties properties;

    @Override
    public void extract(Path csvFile, Path workDir) {
        Path sparkSubmit = Path.of(properties.getSparkSubmit());
        if (!Files.isExecutable(sparkSubmit)) {
            throw new IllegalStateException("spark-submit not found at " + sparkSubmit);
        }
        Path jobJar = resolveJobJar();
        Path logFile = workDir.resolve("spark.log");

        List<String> command = List.of(
                sparkSubmit.toString(),
                "--master", "local[*]",
                "--class", "com.app.product.importjob.ProductCsvJob",
                jobJar.toString(),
                csvFile.toAbsolutePath().toString(),
                workDir.toAbsolutePath().toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.environment().put("SPARK_HOME", SPARK_HOME);
        processBuilder.environment().put("SPARK_LOCAL_IP", "127.0.0.1");
        processBuilder.redirectErrorStream(true);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.to(logFile.toFile()));

        int exitCode;
        try {
            log.info("extracting product csv with {}", command);
            exitCode = processBuilder.start().waitFor();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Spark import was interrupted", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to start spark-submit", ex);
        }

        if (exitCode == 0) {
            return;
        }
        String importError = importError(logFile);
        if (exitCode == 2) {
            throw new BadProductImportException(importError != null ? importError : "CSV could not be read");
        }
        throw new IllegalStateException("Spark import failed"
                + (importError != null ? ": " + importError : ""));
    }

    private Path resolveJobJar() {
        Path configured = Path.of(properties.getJobJar());
        List<Path> candidates = new ArrayList<>();
        if (configured.isAbsolute()) {
            candidates.add(configured);
        } else {
            Path cwd = Path.of("").toAbsolutePath();
            candidates.add(cwd.resolve(configured));
            if (cwd.getParent() != null) {
                candidates.add(cwd.getParent().resolve(configured));
            }
        }
        return candidates.stream()
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Product import job jar not found. Build it with: mvn -pl product-import-job package. Looked in "
                                + candidates));
    }

    private static String importError(Path logFile) {
        if (!Files.isRegularFile(logFile)) {
            return null;
        }
        try {
            return Files.readAllLines(logFile, StandardCharsets.UTF_8).stream()
                    .filter(line -> line.contains("IMPORT_ERROR:"))
                    .reduce((first, second) -> second)
                    .map(line -> line.substring(line.indexOf("IMPORT_ERROR:") + "IMPORT_ERROR:".length()).trim())
                    .orElse(null);
        } catch (IOException ex) {
            return null;
        }
    }
}
