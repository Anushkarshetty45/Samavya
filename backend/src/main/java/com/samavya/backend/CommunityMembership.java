package com.samavya.backend;

import java.time.OffsetDateTime;

public record CommunityMembership(
        long communityId,
        String communityName,
        String role,
        String status,
        OffsetDateTime joinedAt) {
}
