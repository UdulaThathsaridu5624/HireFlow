package com.hireflow.auth_service.messaging;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.hireflow.auth_service.config.RabbitMQConfig;
import com.hireflow.auth_service.dto.UserEventDto;
import com.hireflow.auth_service.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(User user) {
        Map<String, Object> message = buildMessage("user.registered", user);
        rabbitTemplate.convertAndSend(RabbitMQConfig.HIREFLOW_EXCHANGE, "user.registered", message);
    }

    public void publishUserLoggedIn(User user) {
        Map<String, Object> message = buildMessage("user.loggedIn", user);
        rabbitTemplate.convertAndSend(RabbitMQConfig.HIREFLOW_EXCHANGE, "user.loggedIn", message);
    }

    private Map<String, Object> buildMessage(String eventType, User user) {
        UserEventDto event = UserEventDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .eventType(eventType)
                .timestamp(Instant.now().toString())
                .build();

        Map<String, Object> message = new HashMap<>();
        message.put("pattern", eventType);
        message.put("data", event);
        return message;
    }
}
