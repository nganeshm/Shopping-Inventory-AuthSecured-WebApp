package com.app.product.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) { super(message); }
}
