package com.ctse.profileservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValidateResponse {
    private boolean valid;
    private String userId;
    private String role;
    private String email;
    private String name;
}
