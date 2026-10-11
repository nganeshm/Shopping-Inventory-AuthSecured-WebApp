package com.app.auth.exception;

import java.time.Instant;
import java.util.List;

/** Uniform error body returned for every failure, across every endpoint. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {}
