package com.hireflow.auth_service.service;

import com.hireflow.auth_service.dto.AuthResponse;
import com.hireflow.auth_service.dto.LoginRequest;
import com.hireflow.auth_service.dto.RegisterRequest;
import com.hireflow.auth_service.dto.ValidateResponse;
import com.hireflow.auth_service.exception.ConflictException;
import com.hireflow.auth_service.exception.UnauthorizedException;
import com.hireflow.auth_service.model.RefreshToken;
import com.hireflow.auth_service.model.User;
import com.hireflow.auth_service.repository.RefreshTokenRepository;
import com.hireflow.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    private final RefreshTokenRepository refreshTokenRepository;

    public AuthResponse register(RegisterRequest registerRequest){
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new ConflictException("Email already exists");
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest loginRequest){

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if(!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword())){
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse refreshAccessToken(String token) {

        RefreshToken verified = refreshTokenService.verifyRefreshToken(token);

        RefreshToken newRefreshToken = refreshTokenService.rotateToken(verified);

        String newAccessToken = jwtService.generateToken(verified.getUser());

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .userId(verified.getUser().getId())
                .name(verified.getUser().getName())
                .role(verified.getUser().getRole().name())
                .build();
    }

    @Transactional
    public void logout(String token) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }


    public ValidateResponse validate(String token) {

        if (!jwtService.isTokenValid(token)) {
            return ValidateResponse.builder()
                    .valid(false)
                    .build();

        }

        String userId = jwtService.extractUserId(token);
        String role = jwtService.extractRole(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return ValidateResponse.builder()
                .valid(true)
                .userId(userId)
                .role(role)
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

}
