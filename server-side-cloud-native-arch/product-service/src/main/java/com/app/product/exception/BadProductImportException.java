package com.app.product.exception;

/** The upload itself cannot be read (empty file, wrong type, bad header). */
public class BadProductImportException extends RuntimeException {
    public BadProductImportException(String message) {
        super(message);
    }
}
