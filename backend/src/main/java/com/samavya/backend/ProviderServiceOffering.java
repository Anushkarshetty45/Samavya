package com.samavya.backend;

import java.math.BigDecimal;

public record ProviderServiceOffering(
        long id,
        long communityId,
        String communityName,
        long categoryId,
        String categoryName,
        BigDecimal priceFrom,
        boolean active) {
}
