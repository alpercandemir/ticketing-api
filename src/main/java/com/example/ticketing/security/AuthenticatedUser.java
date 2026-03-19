package com.example.ticketing.security;

import com.example.ticketing.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthenticatedUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String roles;
    private final Set<String> roleSet;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(Long id, String email, String password, String roles) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.roleSet = Arrays.stream(roles.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
        this.authorities = roleSet.stream()
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
        return roleSet.contains(role.name());
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
