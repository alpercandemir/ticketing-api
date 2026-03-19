package com.example.ticketing.dto;

import java.util.Map;

public record ErrorResponse(
        String error,
        Map<String, String> fieldErrors
) {

    public static ErrorResponse of(String error) {
        return new ErrorResponse(error, null);
    }

    public static ErrorResponse ofValidation(Map<String, String> fieldErrors) {
        return new ErrorResponse("Validation failed", fieldErrors);
    }
}
