package com.hireflow.auth_service.controller;

import com.hireflow.auth_service.dto.AuthResponse;
import com.hireflow.auth_service.dto.LoginRequest;
import com.hireflow.auth_service.dto.RegisterRequest;
import com.hireflow.auth_service.dto.ValidateResponse;
import com.hireflow.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest){
        AuthResponse authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("/login")

    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")

    public ResponseEntity<ValidateResponse> validate(
            @RequestHeader("Authorization") String authHeader) {


        String token = authHeader.replace("Bearer ", "");

        ValidateResponse response = authService.validate(token);
        return ResponseEntity.ok(response);
    }

}
