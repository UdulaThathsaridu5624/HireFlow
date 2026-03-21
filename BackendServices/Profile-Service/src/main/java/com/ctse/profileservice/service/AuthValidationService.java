package com.ctse.profileservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.ctse.profileservice.config.RabbitConfig;
import com.ctse.profileservice.dto.ValidateResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthValidationService {

    private final RabbitTemplate rabbitTemplate;

    public ValidateResponse validate(String token) {

        Map<String, Object> message = new HashMap<>();
        message.put("pattern", "auth.validate");
        message.put("data", token);

        try {
            Object rawResponse = rabbitTemplate.convertSendAndReceive(
                    RabbitConfig.HIREFLOW_EXCHANGE,
                    "auth.validate",
                    message);

            if (rawResponse == null) {
                log.error("Auth service did not respond");
                return invalidResponse();
            }

            // ✅ SAFE CONVERSION (avoids ClassCastException)
            ValidateResponse response = mapToValidateResponse(rawResponse);

            return response != null ? response : invalidResponse();

        } catch (Exception e) {
            log.error("Error during auth validation: {}", e.getMessage());
            return invalidResponse();
        }
    }

    // ✅ Convert LinkedHashMap → DTO (IMPORTANT FIX)
    private ValidateResponse mapToValidateResponse(Object raw) {
        if (raw instanceof ValidateResponse) {
            return (ValidateResponse) raw;
        }

        if (raw instanceof Map<?, ?> map) {
            ValidateResponse res = new ValidateResponse();

            res.setValid(Boolean.TRUE.equals(map.get("valid")));
            res.setUserId(map.get("userId") != null ? map.get("userId").toString() : null);
            res.setRole((String) map.get("role"));
            res.setEmail((String) map.get("email"));
            res.setName((String) map.get("name"));

            return res;
        }

        return null;
    }

    private ValidateResponse invalidResponse() {
        ValidateResponse res = new ValidateResponse();
        res.setValid(false);
        return res;
    }
}