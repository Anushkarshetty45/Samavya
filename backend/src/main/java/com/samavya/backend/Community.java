package com.samavya.backend;

import java.time.OffsetDateTime;

public record Community(
        long id,
        String name,
        String description,
        String address,
        OffsetDateTime createdAt) {
}
