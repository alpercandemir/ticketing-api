package com.example.ticketing.exception;

public class IdempotencyKeyInProgressException extends RuntimeException {

    public IdempotencyKeyInProgressException() {
        super("Request is currently being processed");
    }
}
