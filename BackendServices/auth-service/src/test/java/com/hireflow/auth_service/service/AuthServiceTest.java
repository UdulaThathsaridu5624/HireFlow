package com.hireflow.auth_service.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hireflow.auth_service.dto.LoginRequest;
import com.hireflow.auth_service.dto.RegisterRequest;
import com.hireflow.auth_service.exception.ConflictException;
import com.hireflow.auth_service.exception.UnauthorizedException;
import com.hireflow.auth_service.messaging.AuthEventPublisher;
import com.hireflow.auth_service.model.RefreshToken;
import com.hireflow.auth_service.model.User;
import com.hireflow.auth_service.model.UserRole;
import com.hireflow.auth_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String JWT_TOKEN = "jwt-token";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String RAW_PASSWORD = "password";

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthEventPublisher authEventPublisher;

    @InjectMocks
    private AuthService authService;

    private final User testUser = User.builder()
            .id("user-123")
            .email(TEST_EMAIL)
            .name("Test User")
            .password(ENCODED_PASSWORD)
            .role(UserRole.CANDIDATE)
            .build();

    private final RefreshToken testRefreshToken = RefreshToken.builder()
            .id("token-id")
            .token(REFRESH_TOKEN)
            .user(testUser)
            .build();

    @Test
    void registerThrowsConflictWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(TEST_EMAIL);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        ConflictException ex = assertThrows(ConflictException.class, () -> authService.register(request));
        assertNotNull(ex);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerReturnsAuthResponseForValidRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setName("New User");
        request.setPassword("password123");
        request.setRole(UserRole.CANDIDATE);

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any())).thenReturn(testUser);
        when(jwtService.generateToken(any())).thenReturn(JWT_TOKEN);
        when(refreshTokenService.createRefreshToken(any())).thenReturn(testRefreshToken);

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        verify(authEventPublisher).publishUserRegistered(any());
    }

    @Test
    void loginThrowsUnauthorizedWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@example.com");
        request.setPassword(RAW_PASSWORD);

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertNotNull(ex);
    }

    @Test
    void loginThrowsUnauthorizedWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", ENCODED_PASSWORD)).thenReturn(false);

        UnauthorizedException ex2 = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertNotNull(ex2);
    }

    @Test
    void loginReturnsAuthResponseForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail(TEST_EMAIL);
        request.setPassword(RAW_PASSWORD);

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn(JWT_TOKEN);
        when(refreshTokenService.createRefreshToken(testUser)).thenReturn(testRefreshToken);

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals(JWT_TOKEN, response.getToken());
        verify(authEventPublisher).publishUserLoggedIn(testUser);
    }
}
