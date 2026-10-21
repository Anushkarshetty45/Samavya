package com.samavya.backend;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProviderGig(
        long id,
        long providerServiceId,
        String title,
        String description,
        PricingType pricingType,
        BigDecimal price,
        GigStatus status,
        OffsetDateTime createdAt) {
}
