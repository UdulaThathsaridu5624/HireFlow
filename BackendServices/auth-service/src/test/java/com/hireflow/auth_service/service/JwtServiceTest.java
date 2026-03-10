package com.hireflow.auth_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.hireflow.auth_service.model.User;
import com.hireflow.auth_service.model.UserRole;

class JwtServiceTest {

    private static final String SECRET_KEY = "testsecretkey12345678901234567890123456789012";

    private final JwtService jwtService = createJwtService();

    private static JwtService createJwtService() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(service, "expirationTime", 3600000L);
        return service;
    }

    private User buildUser() {
        return User.builder()
                .id("user-123")
                .email("test@example.com")
                .name("Test User")
                .password("encoded")
                .role(UserRole.CANDIDATE)
                .build();
    }

    @Test
    void generateTokenReturnsNonNullToken() {
        String token = jwtService.generateToken(buildUser());
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void isTokenValidReturnsTrueForValidToken() {
        String token = jwtService.generateToken(buildUser());
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValidReturnsFalseForInvalidToken() {
        assertFalse(jwtService.isTokenValid("invalid.token.here"));
    }

    @Test
    void extractUserIdReturnsCorrectUserId() {
        String token = jwtService.generateToken(buildUser());
        assertEquals("user-123", jwtService.extractUserId(token));
    }

    @Test
    void extractRoleReturnsCorrectRole() {
        String token = jwtService.generateToken(buildUser());
        assertEquals("CANDIDATE", jwtService.extractRole(token));
    }
}
