package com.example.ticketing.service;

import com.example.ticketing.domain.Role;
import com.example.ticketing.domain.User;
import com.example.ticketing.dto.AuthResponse;
import com.example.ticketing.dto.LoginRequest;
import com.example.ticketing.dto.RefreshTokenRequest;
import com.example.ticketing.dto.RegisterRequest;
import com.example.ticketing.repository.UserRepository;
import com.example.ticketing.security.AuthenticatedUser;
import com.example.ticketing.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .roles(Role.ROLE_CUSTOMER.name())
                .build();

        user = userRepository.save(user);

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(), user.getEmail(), user.getPasswordHash(), user.getRoles());

        return buildAuthResponse(authenticatedUser);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        userRepository.findById(authenticatedUser.getId()).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });

        return buildAuthResponse(authenticatedUser);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String email = tokenProvider.getUsernameFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.getId(), user.getEmail(), user.getPasswordHash(), user.getRoles());

        return buildAuthResponse(authenticatedUser);
    }

    private AuthResponse buildAuthResponse(AuthenticatedUser authenticatedUser) {
        String accessToken = tokenProvider.generateToken(authenticatedUser);
        String refreshToken = tokenProvider.generateRefreshToken(authenticatedUser);
        return new AuthResponse(accessToken, refreshToken, authenticatedUser.getUsername());
    }
}
