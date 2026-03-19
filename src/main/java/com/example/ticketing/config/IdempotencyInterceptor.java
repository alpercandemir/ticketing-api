package com.example.ticketing.config;

import com.example.ticketing.domain.IdempotencyKey;
import com.example.ticketing.domain.IdempotencyStatus;
import com.example.ticketing.repository.IdempotencyKeyRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyInterceptor.class);

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public IdempotencyInterceptor(IdempotencyKeyRepository idempotencyKeyRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isEmpty()) {
            response.setStatus(400);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Idempotency-Key header is required\",\"status\":400}");
            return false;
        }

        Optional<IdempotencyKey> existingKeyOpt = idempotencyKeyRepository.findByIdempotencyKey(key);

        if (existingKeyOpt.isPresent()) {
            IdempotencyKey existingKey = existingKeyOpt.get();

            if (existingKey.getTtl().isBefore(LocalDateTime.now())) {
                log.warn("Idempotency key expired: {}", key);
                return true;
            }

            if (existingKey.getStatus() == IdempotencyStatus.COMPLETED) {
                log.info("Returning cached response for key: {}", key);
                response.setStatus(existingKey.getResponseStatus());
                response.setContentType("application/json");
                response.getWriter().write(existingKey.getResponseBody());
                return false;
            } else if (existingKey.getStatus() == IdempotencyStatus.IN_PROGRESS) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Request is currently being processed\",\"status\":429}");
                return false;
            }
        } else {
            IdempotencyKey newKey = IdempotencyKey.builder()
                    .idempotencyKey(key)
                    .endpoint(request.getRequestURI())
                    .status(IdempotencyStatus.IN_PROGRESS)
                    .ttl(LocalDateTime.now().plusHours(24))
                    .build();
            idempotencyKeyRepository.save(newKey);
            request.setAttribute("IDEMPOTENCY_KEY", newKey);
        }

        return true;
    }
}
