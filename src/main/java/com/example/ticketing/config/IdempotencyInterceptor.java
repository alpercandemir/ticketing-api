package com.example.ticketing.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.ticketing.domain.IdempotencyKey;
import com.example.ticketing.domain.IdempotencyStatus;
import com.example.ticketing.exception.IdempotencyKeyInProgressException;
import com.example.ticketing.exception.IdempotentReplayException;
import com.example.ticketing.exception.MissingIdempotencyKeyException;
import com.example.ticketing.repository.IdempotencyKeyRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyInterceptor.class);

    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";


    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public IdempotencyInterceptor(IdempotencyKeyRepository idempotencyKeyRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        var key = request.getHeader(IDEMPOTENCY_KEY);
        if (key == null || key.isEmpty()) {
            throw new MissingIdempotencyKeyException();
        }

        var existingKey = idempotencyKeyRepository.findByIdempotencyKey(key);
        if (existingKey == null) {
            var newKey = IdempotencyKey.builder()
                    .idempotencyKey(key)
                    .endpoint(request.getRequestURI())
                    .status(IdempotencyStatus.IN_PROGRESS)
                    .ttl(LocalDateTime.now().plusHours(24))
                    .build();
            idempotencyKeyRepository.save(newKey);
            request.setAttribute(IDEMPOTENCY_KEY, newKey);
            return true;
        }

        if (existingKey.getTtl().isBefore(LocalDateTime.now())) {
            log.warn("Idempotency key expired: {}", key);
            return true;
        }

        if (existingKey.getStatus() == IdempotencyStatus.COMPLETED) {
            log.info("Returning cached response for key: {}", key);
            var storedStatus = existingKey.getResponseStatus();
            var replayStatus = storedStatus != null ? storedStatus : 200;
            throw new IdempotentReplayException(replayStatus, existingKey.getResponseBody());
        }

        if (existingKey.getStatus() == IdempotencyStatus.IN_PROGRESS) {
            throw new IdempotencyKeyInProgressException();
        }

        return true;
    }
}
