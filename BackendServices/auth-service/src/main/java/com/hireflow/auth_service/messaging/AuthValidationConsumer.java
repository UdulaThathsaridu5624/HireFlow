package com.hireflow.auth_service.messaging;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.hireflow.auth_service.config.RabbitMQConfig;
import com.hireflow.auth_service.dto.ValidateResponse;
import com.hireflow.auth_service.model.User;
import com.hireflow.auth_service.repository.UserRepository;
import com.hireflow.auth_service.service.JwtService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthValidationConsumer {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @RabbitListener(queues = RabbitMQConfig.AUTH_VALIDATE_QUEUE)
    public ValidateResponse handleAuthValidation(Map<String, Object> message) {
        try {
            String token = (String) message.get("data");

            if (token == null || !jwtService.isTokenValid(token)) {
                return ValidateResponse.builder().valid(false).build();
            }

            String userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return ValidateResponse.builder().valid(false).build();
            }

            return ValidateResponse.builder()
                    .valid(true)
                    .userId(userId)
                    .role(role)
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();
        } catch (Exception e) {
            return ValidateResponse.builder().valid(false).build();
        }
    }
}
