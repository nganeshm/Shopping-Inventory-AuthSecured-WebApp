package com.app.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Gateway-level first line of defense: Spring Cloud Gateway is built on
 * WebFlux (reactive, non-blocking), so this is a GlobalFilter rather than a
 * Servlet OncePerRequestFilter like the ones in auth/product/customer
 * service. It runs for EVERY route before the request is proxied downstream.
 *
 * It does the cheap check once here (is there a syntactically valid,
 * correctly-signed, non-expired token at all?) so obviously bad requests are
 * rejected in one place instead of each service repeating that work. Each
 * downstream service STILL independently re-validates the token and applies
 * its own @PreAuthorize role checks — the gateway is a first filter, not a
 * replacement for per-service authorization (defense in depth).
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    // Paths that do not require a token at all.
    private static final List<String> OPEN_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/verify-email",
            "/api/auth/resend-otp",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/customers/register",
            "/actuator/health"
    );

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Browser preflight has no Authorization header. Rejecting it here
        // surfaces as a CORS error in the React app even when the real call is allowed.
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        boolean isOpenPath = OPEN_PATHS.stream().anyMatch(path::equals);
        // Public product/category browsing (GET only) also needs no token —
        // mirrors the permitAll() rule already declared in product-service's
        // own SecurityConfig, kept consistent here at the edge too.
        boolean isPublicCatalogGet = request.getMethod() != null
                && request.getMethod().name().equals("GET")
                && (path.startsWith("/api/products") || path.startsWith("/api/categories"));

        if (isOpenPath || isPublicCatalogGet) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing bearer token");
        }

        String token = authHeader.substring(7);
        try {
            Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token);
        } catch (Exception ex) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"error\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return -1; // run before routing filters
    }
}
