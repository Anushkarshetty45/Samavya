package com.samavya.backend.auth;

import org.springframework.security.oauth2.jwt.Jwt;

public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static long id(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
