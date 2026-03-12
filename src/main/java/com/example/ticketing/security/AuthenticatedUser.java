package com.example.ticketing.security;

import com.example.ticketing.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

public class AuthenticatedUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String roles;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(Long id, String email, String password, String roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.authorities = Arrays.stream(roles.split(","))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getRoles() {
        return roles;
    }

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role.name());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}

