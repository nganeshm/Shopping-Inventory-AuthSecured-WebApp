package com.app.product.service.impl;

import com.app.product.config.ProductImportProperties;
import com.app.product.dto.ExtractedProductRow;
import com.app.product.dto.ProductImportResponse;
import com.app.product.exception.BadProductImportException;
import com.app.product.exception.ProductImportRejectedException;
import com.app.product.service.ProductImportService;
import com.app.product.service.ProductImportWriter;
import com.app.product.service.SparkCsvExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProductImportServiceImpl implements ProductImportService {

    private final ProductImportProperties properties;
    private final SparkCsvExtractor sparkCsvExtractor;
    private final ProductImportWriter productImportWriter;
    private final ObjectMapper objectMapper;

    @Override
    public ProductImportResponse importCsv(MultipartFile file) {
        byte[] csv = readCsv(file);
        Path workDir = Path.of(properties.getStorageDir()).resolve(UUID.randomUUID().toString());
        try {
            Files.createDirectories(workDir);
            Path csvFile = workDir.resolve("upload.csv");
            Files.write(csvFile, csv);

            sparkCsvExtractor.extract(csvFile, workDir);

            List<ExtractedProductRow> rejected = readRows(workDir.resolve("rejected"));
            List<ExtractedProductRow> valid = readRows(workDir.resolve("valid"));
            valid.sort(Comparator.comparingInt(ExtractedProductRow::rowNumber));

            List<String> errors = new ArrayList<>();
            rejected.stream()
                    .sorted(Comparator.comparingInt(ExtractedProductRow::rowNumber))
                    .map(row -> "row " + row.rowNumber() + ": " + row.reason())
                    .forEach(errors::add);
            errors.addAll(productImportWriter.findConflicts(valid));
            if (!errors.isEmpty()) {
                throw new ProductImportRejectedException(errors);
            }
            return new ProductImportResponse(productImportWriter.save(valid));
        } catch (IOException ex) {
            throw new IllegalStateException("Could not store the product CSV", ex);
        }
    }

    private static byte[] readCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadProductImportException("file is required");
        }
        String filename = file.getOriginalFilename();
        if (filename != null && !filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw new BadProductImportException("file must be a .csv");
        }
        try {
            byte[] bytes = file.getBytes();
            if (bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xEF
                    && (bytes[1] & 0xFF) == 0xBB
                    && (bytes[2] & 0xFF) == 0xBF) {
                return Arrays.copyOfRange(bytes, 3, bytes.length);
            }
            return bytes;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read the upload", ex);
        }
    }

    private List<ExtractedProductRow> readRows(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        List<ExtractedProductRow> rows = new ArrayList<>();
        try (Stream<Path> files = Files.list(directory)) {
            for (Path part : files.filter(path -> path.getFileName().toString().startsWith("part-")).toList()) {
                try (BufferedReader reader = Files.newBufferedReader(part, StandardCharsets.UTF_8)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank()) {
                            rows.add(objectMapper.readValue(line, ExtractedProductRow.class));
                        }
                    }
                }
            }
        }
        return rows;
    }
}
