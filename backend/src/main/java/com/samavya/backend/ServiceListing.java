package com.samavya.backend;

import java.math.BigDecimal;

public record ServiceListing(
        Long id,
        String providerName,
        String serviceNAme,
        BigDecimal price,
        boolean available) {
}
