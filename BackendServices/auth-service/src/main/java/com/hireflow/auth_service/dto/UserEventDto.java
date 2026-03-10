package com.hireflow.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEventDto {
    private String userId;
    private String email;
    private String name;
    private String role;
    private String eventType;
    private String timestamp;
}
