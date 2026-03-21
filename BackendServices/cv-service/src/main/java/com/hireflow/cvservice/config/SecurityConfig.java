package com.hireflow.cvservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Value("${app.security.jwt-secret:${JWT_SECRET:change-me-to-a-long-shared-secret}}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**").permitAll()
                        .requestMatchers("/api/cv/**").hasRole("CANDIDATE")
                        .requestMatchers("/api/employer/**").hasRole("EMPLOYER")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                // Convert Date timestamps to Instant
                Instant issuedAt = claims.getIssuedAt() != null ? 
                        claims.getIssuedAt().toInstant() : null;
                Instant expiresAt = claims.getExpiration() != null ? 
                        claims.getExpiration().toInstant() : null;

                // Build claims map without timestamp fields (handled separately)
                Map<String, Object> claimsMap = new HashMap<>(claims);
                claimsMap.remove("iat");
                claimsMap.remove("exp");

                return Jwt.withTokenValue(token)
                        .header("alg", "HS256")
                        .subject(claims.getSubject())
                        .issuedAt(issuedAt)
                        .expiresAt(expiresAt)
                        .claims(c -> claimsMap.forEach((k, v) -> c.put(k, v)))
                        .build();
            } catch (Exception e) {
                throw new org.springframework.security.oauth2.jwt.BadJwtException("Invalid JWT: " + e.getMessage());
            }
        };
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        String role = jwt.getClaimAsString("userRole");
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }
}
