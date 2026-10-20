package com.samavya.backend;

import java.math.BigDecimal;
import java.util.List;

public record ProviderServiceListing(
        long id,
        long providerId,
        String providerName,
        String providerDescription,
        boolean providerAvailable,
        String verificationStatus,
        long communityId,
        long categoryId,
        String categoryName,
        String categoryDescription,
        BigDecimal priceFrom,
        List<GigSummary> gigs) {
}
