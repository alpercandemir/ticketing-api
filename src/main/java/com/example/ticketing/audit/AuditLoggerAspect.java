package com.example.ticketing.audit;

import com.example.ticketing.domain.AuditLog;
import com.example.ticketing.repository.AuditLogRepository;
import com.example.ticketing.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditLoggerAspect {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuditLoggerAspect.class);

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

            // Attempt to get resourceId from the result if it's an entity with an ID
            String resourceId = null;
            if (result != null) {
                try {
                    Method getIdMethod = result.getClass().getMethod("getId");
                    Object idValue = getIdMethod.invoke(result);
                    if (idValue != null) {
                        resourceId = idValue.toString();
                    }
                } catch (Exception ignored) {
                    // Result doesn't have a standard getId method
                }
            }

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }
        return null;
    }
}
