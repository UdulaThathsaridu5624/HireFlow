package com.ctse.profileservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.ctse.profileservice.config.RabbitConfig;
import com.ctse.profileservice.dto.ValidateResponse;

@ExtendWith(MockitoExtension.class)
class AuthValidationServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthValidationService authValidationService;

    @Test
    void validateReturnsValidResponseWhenTokenIsValid() {
        // Arrange
        String token = "valid-token";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("valid", true);
        mockResponse.put("userId", "user-123");
        mockResponse.put("role", "EMPLOYER");
        mockResponse.put("email", "employer@test.com");
        mockResponse.put("name", "Test Employer");

        when(rabbitTemplate.convertSendAndReceive(
                eq(RabbitConfig.HIREFLOW_EXCHANGE),
                eq("auth.validate"),
                any(Map.class)))
                .thenReturn(mockResponse);

        // Act
        ValidateResponse result = authValidationService.validate(token);

        // Assert
        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals("user-123", result.getUserId());
        assertEquals("EMPLOYER", result.getRole());
        assertEquals("employer@test.com", result.getEmail());
        assertEquals("Test Employer", result.getName());

        verify(rabbitTemplate).convertSendAndReceive(
                eq(RabbitConfig.HIREFLOW_EXCHANGE),
                eq("auth.validate"),
                any(Map.class));
    }

    @Test
    void validateReturnsInvalidResponseWhenTokenIsInvalid() {
        // Arrange
        String token = "invalid-token";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("valid", false);

        when(rabbitTemplate.convertSendAndReceive(
                eq(RabbitConfig.HIREFLOW_EXCHANGE),
                eq("auth.validate"),
                any(Map.class)))
                .thenReturn(mockResponse);

        // Act
        ValidateResponse result = authValidationService.validate(token);

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
    }

    @Test
    void validateReturnsInvalidResponseWhenExceptionThrown() {
        // Arrange
        String token = "error-token";

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                anyString(),
                any(Map.class)))
                .thenThrow(new RuntimeException("RabbitMQ Error"));

        // Act
        ValidateResponse result = authValidationService.validate(token);

        // Assert
        assertNotNull(result);
        assertFalse(result.isValid());
    }
}
