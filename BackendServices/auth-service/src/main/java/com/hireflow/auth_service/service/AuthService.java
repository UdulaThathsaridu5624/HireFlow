package com.hireflow.auth_service.service;

import com.hireflow.auth_service.dto.AuthResponse;
import com.hireflow.auth_service.dto.LoginRequest;
import com.hireflow.auth_service.dto.RegisterRequest;
import com.hireflow.auth_service.dto.ValidateResponse;
import com.hireflow.auth_service.model.User;
import com.hireflow.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest registerRequest){
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new RuntimeException("Email Already Exists");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest loginRequest){

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid Credentials"));

        if(!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword())){
            throw new RuntimeException("Invalid Credentials");
        }
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
    public ValidateResponse validate(String token) {

        if (!jwtService.isTokenValid(token)) {
            return ValidateResponse.builder()
                    .valid(false)
                    .build();

        }

        String userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);
        return ValidateResponse.builder()
                .valid(true)
                .userId(userId)
                .role(role)
                .build();
    }

}
