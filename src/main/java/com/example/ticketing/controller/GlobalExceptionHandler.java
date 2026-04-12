package com.example.ticketing.controller;

import com.example.ticketing.dto.ErrorResponse;
import com.example.ticketing.exception.IdempotencyKeyInProgressException;
import com.example.ticketing.exception.IdempotentReplayException;
import com.example.ticketing.exception.MissingIdempotencyKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleMissingIdempotencyKey(MissingIdempotencyKeyException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IdempotencyKeyInProgressException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyInProgress(IdempotencyKeyInProgressException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(IdempotentReplayException.class)
    public ResponseEntity<String> handleIdempotentReplay(IdempotentReplayException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getHttpStatus());
        if (status == null) {
            status = HttpStatus.OK;
        }
        String body = ex.getResponseBody() != null ? ex.getResponseBody() : "{}";
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (a, b) -> a
                ));
        return ResponseEntity.badRequest().body(ErrorResponse.ofValidation(fieldErrors));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException ex) {
        return buildErrorResponse("Concurrent modification detected. Please try again.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return buildErrorResponse("Resource not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(ErrorResponse.of(message), status);
    }
}
