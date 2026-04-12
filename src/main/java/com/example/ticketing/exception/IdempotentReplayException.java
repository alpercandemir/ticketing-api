package com.example.ticketing.exception;

public class IdempotentReplayException extends RuntimeException {

    private final int httpStatus;
    private final String responseBody;

    public IdempotentReplayException(int httpStatus, String responseBody) {
        super("Idempotent replay");
        this.httpStatus = httpStatus;
        this.responseBody = responseBody;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
