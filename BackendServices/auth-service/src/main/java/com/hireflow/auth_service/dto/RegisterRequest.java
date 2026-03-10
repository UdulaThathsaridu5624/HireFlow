package com.hireflow.auth_service.dto;

import com.hireflow.auth_service.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message="Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6 , message = "Password must be at least 6 characters long")
    private String password;

    @NotNull(message = "User role is required")
    private UserRole role;

}
