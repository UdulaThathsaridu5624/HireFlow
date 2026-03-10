package com.hireflow.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateResponse {
    private boolean valid;
    private String userId;
    private String role;
    private String email;
    private String name;
}
