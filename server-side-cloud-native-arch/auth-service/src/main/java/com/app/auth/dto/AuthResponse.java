package com.app.auth.dto;

/** What the client gets back after a successful login: a bearer token. */
public record AuthResponse(String token, String tokenType, long expiresInMs) {}
