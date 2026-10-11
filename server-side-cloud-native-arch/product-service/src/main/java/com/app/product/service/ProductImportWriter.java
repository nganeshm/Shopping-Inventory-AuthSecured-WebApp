package com.app.product.service;

import com.app.product.dto.ExtractedProductRow;

import java.util.List;

/**
 * Checks rows against the catalog tables and, when every row is legal,
 * inserts products and their inventory rows.
 */
public interface ProductImportWriter {

    /** Row messages for unknown categories and skus that already exist. Empty when the batch can be saved. */
    List<String> findConflicts(List<ExtractedProductRow> rows);

    /** Persists every row. Caller must have already rejected conflicts. */
    int save(List<ExtractedProductRow> rows);
}
