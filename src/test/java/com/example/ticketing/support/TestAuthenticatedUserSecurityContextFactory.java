package com.example.ticketing.support;

import com.example.ticketing.security.AuthenticatedUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class TestAuthenticatedUserSecurityContextFactory implements WithSecurityContextFactory<TestAuthenticatedUser> {

    @Override
    public SecurityContext createSecurityContext(TestAuthenticatedUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        AuthenticatedUser principal = new AuthenticatedUser(
                annotation.id(), annotation.email(), annotation.password(), annotation.roles());

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        context.setAuthentication(auth);
        return context;
    }
}
