package com.example.ticketing.audit;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.ticketing.domain.AuditLog;
import com.example.ticketing.repository.AuditLogRepository;
import com.example.ticketing.security.SecurityContextHelper;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditLoggerAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggerAspect.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLoggerAspect(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @AfterReturning(pointcut = "@annotation(com.example.ticketing.audit.AuditLoggable)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AuditLoggable auditAnnotation = method.getAnnotation(AuditLoggable.class);

            HttpServletRequest request = getRequest();
            Long actorId = getActorId();

            String ipAddress = null;
            String userAgent = null;

            if (request != null) {
                ipAddress = getClientIP(request);
                userAgent = request.getHeader("User-Agent");
            }

            String resourceId = extractResourceId(result);

            AuditLog logEntry = AuditLog.builder()
                    .actorId(actorId)
                    .action(auditAnnotation.action())
                    .resourceType(auditAnnotation.resourceType())
                    .resourceId(resourceId)
                    .ip(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }

    private String extractResourceId(Object result) {
        if (result == null) {
            return null;
        }
        try {
            Method getIdMethod = result.getClass().getMethod("getId");
            Object idValue = getIdMethod.invoke(result);
            return idValue != null ? idValue.toString() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private Long getActorId() {
        try {
            return SecurityContextHelper.getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }
}
