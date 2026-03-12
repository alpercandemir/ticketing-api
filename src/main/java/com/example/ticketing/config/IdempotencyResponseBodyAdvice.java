package com.example.ticketing.config;

import com.example.ticketing.domain.IdempotencyKey;
import com.example.ticketing.repository.IdempotencyKeyRepository;
import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class IdempotencyResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdempotencyResponseBodyAdvice.class);

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final JsonMapper jsonMapper;

    public IdempotencyResponseBodyAdvice(IdempotencyKeyRepository idempotencyKeyRepository, JsonMapper jsonMapper) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (request instanceof ServletServerHttpRequest servletRequest && response instanceof ServletServerHttpResponse servletResponse) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            Object keyAttr = httpServletRequest.getAttribute("IDEMPOTENCY_KEY");

            if (keyAttr instanceof IdempotencyKey idempotencyKey) {
                try {
                    String responseBodyStr = jsonMapper.writeValueAsString(body);
                    idempotencyKey.setResponseBody(responseBodyStr);
                    idempotencyKey.setResponseStatus(servletResponse.getServletResponse().getStatus());
                    idempotencyKey.setStatus("COMPLETED");

                    idempotencyKeyRepository.save(idempotencyKey);
                } catch (Exception e) {
                    log.error("Failed to serialize and save idempotent response", e);
                }
            }
        }

        return body;
    }
}
