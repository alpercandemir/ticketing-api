package com.example.ticketing.service;

import com.example.ticketing.domain.User;
import com.example.ticketing.dto.AuthResponse;
import com.example.ticketing.dto.LoginRequest;
import com.example.ticketing.dto.RegisterRequest;
import com.example.ticketing.repository.UserRepository;
import com.example.ticketing.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("new@test.com", "password123", null);

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserDetails mockDetails = new org.springframework.security.core.userdetails.User(
                "new@test.com", "$2a$encoded", List.of());
        when(userDetailsService.loadUserByUsername("new@test.com")).thenReturn(mockDetails);
        when(tokenProvider.generateToken(mockDetails)).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(mockDetails)).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("new@test.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DefaultsToCustomerRole() {
        RegisterRequest request = new RegisterRequest("new@test.com", "password123", null);

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            assertEquals("ROLE_CUSTOMER", u.getRoles());
            u.setId(1L);
            return u;
        });

        UserDetails mockDetails = new org.springframework.security.core.userdetails.User(
                "new@test.com", "encoded", List.of());
        when(userDetailsService.loadUserByUsername("new@test.com")).thenReturn(mockDetails);
        when(tokenProvider.generateToken(any())).thenReturn("t");
        when(tokenProvider.generateRefreshToken(any())).thenReturn("r");

        authService.register(request);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_Throws() {
        RegisterRequest request = new RegisterRequest("existing@test.com", "pass", null);
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));
        assertEquals("Email already in use", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("user@test.com", "password");

        UserDetails mockDetails = new org.springframework.security.core.userdetails.User(
                "user@test.com", "hash", List.of());
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(mockDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        User user = User.builder().id(1L).email("user@test.com").roles("ROLE_CUSTOMER").build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateToken(mockDetails)).thenReturn("access");
        when(tokenProvider.generateRefreshToken(mockDetails)).thenReturn("refresh");

        AuthResponse response = authService.login(request);

        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        assertNotNull(user.getLastLoginAt());
    }

    @Test
    void login_BadCredentials_Throws() {
        LoginRequest request = new LoginRequest("user@test.com", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void refresh_Success() {
        UserDetails mockDetails = new org.springframework.security.core.userdetails.User(
                "user@test.com", "hash", List.of());
        when(tokenProvider.getUsernameFromToken("old-refresh")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(mockDetails);
        when(tokenProvider.validateToken("old-refresh", mockDetails)).thenReturn(true);
        when(tokenProvider.generateToken(mockDetails)).thenReturn("new-access");
        when(tokenProvider.generateRefreshToken(mockDetails)).thenReturn("new-refresh");

        AuthResponse response = authService.refresh(Map.of("refreshToken", "old-refresh"));

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
    }

    @Test
    void refresh_MissingToken_Throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.refresh(Map.of()));
        assertEquals("refreshToken is required", ex.getMessage());
    }

    @Test
    void refresh_InvalidToken_Throws() {
        UserDetails mockDetails = new org.springframework.security.core.userdetails.User(
                "user@test.com", "hash", List.of());
        when(tokenProvider.getUsernameFromToken("bad-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(mockDetails);
        when(tokenProvider.validateToken("bad-token", mockDetails)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.refresh(Map.of("refreshToken", "bad-token")));
    }
}
