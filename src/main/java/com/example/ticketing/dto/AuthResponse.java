package com.example.ticketing.dto;



public record AuthResponse(String accessToken, String refreshToken, String email) {
}
