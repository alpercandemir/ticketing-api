package com.example.ticketing.support;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = TestAuthenticatedUserSecurityContextFactory.class)
public @interface TestAuthenticatedUser {
    long id() default 1L;
    String email() default "test@example.com";
    String roles() default "ROLE_CUSTOMER";
    String password() default "password";
}
