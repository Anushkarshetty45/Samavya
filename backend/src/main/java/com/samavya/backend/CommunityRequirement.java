package com.samavya.backend;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CommunityRequirement(
        long id,
        long communityId,
        long createdBy,
        RequirementType type,
        Long serviceCategoryId,
        String title,
        String description,
        BigDecimal quantity,
        String quantityUnit,
        BigDecimal budget,
        OffsetDateTime neededBy,
        String status,
        OffsetDateTime createdAt) {
}
