package com.example.ticketing.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank String title,
        @NotBlank String venue,
        @NotNull @Future LocalDateTime startsAt,
        @NotNull LocalDateTime endsAt,
        @NotNull @Min(1) Integer capacity
) {}
