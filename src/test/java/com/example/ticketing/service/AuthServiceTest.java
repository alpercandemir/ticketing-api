package com.example.ticketing.service;

import com.example.ticketing.domain.User;
import com.example.ticketing.dto.AuthResponse;
import com.example.ticketing.dto.LoginRequest;
import com.example.ticketing.dto.RefreshTokenRequest;
import com.example.ticketing.dto.RegisterRequest;
import com.example.ticketing.repository.UserRepository;
import com.example.ticketing.security.AuthenticatedUser;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("new@test.com", "password123");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(tokenProvider.generateToken(any(AuthenticatedUser.class))).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(AuthenticatedUser.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("new@test.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_AlwaysAssignsCustomerRole() {
        RegisterRequest request = new RegisterRequest("new@test.com", "password123");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            assertEquals("ROLE_CUSTOMER", u.getRoles());
            u.setId(1L);
            return u;
        });
        when(tokenProvider.generateToken(any(AuthenticatedUser.class))).thenReturn("t");
        when(tokenProvider.generateRefreshToken(any(AuthenticatedUser.class))).thenReturn("r");

        authService.register(request);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_Throws() {
        RegisterRequest request = new RegisterRequest("existing@test.com", "password");
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));
        assertEquals("Email already in use", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("user@test.com", "password");

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(1L, "user@test.com", "hash", "ROLE_CUSTOMER");
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(authenticatedUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);

        User user = User.builder().id(1L).email("user@test.com").roles("ROLE_CUSTOMER").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateToken(authenticatedUser)).thenReturn("access");
        when(tokenProvider.generateRefreshToken(authenticatedUser)).thenReturn("refresh");

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
        when(tokenProvider.validateToken("old-refresh")).thenReturn(true);
        when(tokenProvider.isRefreshToken("old-refresh")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("old-refresh")).thenReturn("user@test.com");

        User user = User.builder().id(1L).email("user@test.com").passwordHash("hash").roles("ROLE_CUSTOMER").build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(any(AuthenticatedUser.class))).thenReturn("new-access");
        when(tokenProvider.generateRefreshToken(any(AuthenticatedUser.class))).thenReturn("new-refresh");

        AuthResponse response = authService.refresh(new RefreshTokenRequest("old-refresh"));

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
    }

    @Test
    void refresh_InvalidToken_Throws() {
        when(tokenProvider.validateToken("bad-token")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.refresh(new RefreshTokenRequest("bad-token")));
    }

    @Test
    void refresh_AccessTokenRejected() {
        when(tokenProvider.validateToken("access-token")).thenReturn(true);
        when(tokenProvider.isRefreshToken("access-token")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.refresh(new RefreshTokenRequest("access-token")));
    }
}
