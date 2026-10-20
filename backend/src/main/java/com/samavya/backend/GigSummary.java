package com.samavya.backend;

import java.math.BigDecimal;

public record GigSummary(
        long id,
        String title,
        String description,
        String pricingType,
        BigDecimal price,
        String status) {
}
