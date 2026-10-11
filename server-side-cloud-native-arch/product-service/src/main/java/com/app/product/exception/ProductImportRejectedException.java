package com.app.product.exception;

import java.util.List;

/** One or more rows failed validation. Nothing was written. */
public class ProductImportRejectedException extends RuntimeException {

    private final List<String> details;

    public ProductImportRejectedException(List<String> details) {
        super("Product import rejected");
        this.details = List.copyOf(details);
    }

    public List<String> getDetails() {
        return details;
    }
}
