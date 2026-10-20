package com.samavya.backend;

public record AuthenticationResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserAccount user) {
}
