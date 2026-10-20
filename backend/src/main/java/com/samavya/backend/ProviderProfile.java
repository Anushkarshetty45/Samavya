package com.samavya.backend;

public record ProviderProfile(
        long id,
        String providerName,
        String description,
        boolean available,
        String verificationStatus) {
}
