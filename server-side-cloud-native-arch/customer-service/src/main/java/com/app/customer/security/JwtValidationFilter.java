package com.app.customer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Same independent, stateless JWT verification pattern as product-service —
 * same shared secret, no network call back to auth-service. Each service
 * trusts the signature, not a central session store.
 */
@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = Jwts.parser().verifyWith(signingKey()).build()
                        .parseSignedClaims(token).getPayload();

                String username = claims.getSubject();
                String email = claims.get("email", String.class);
                List<String> roles = claims.get("roles", List.class);
                List<GrantedAuthority> authorities = roles == null ? List.of() :
                        roles.stream().map(SimpleGrantedAuthority::new).map(GrantedAuthority.class::cast).toList();

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authToken.setDetails(email);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
