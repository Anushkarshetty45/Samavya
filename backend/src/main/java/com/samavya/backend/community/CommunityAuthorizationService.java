package com.samavya.backend.community;

import com.samavya.backend.repository.CommunityRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CommunityAuthorizationService {

    private final CommunityRepository repository;

    public CommunityAuthorizationService(CommunityRepository repository) {
        this.repository = repository;
    }

    public void requireActiveMembership(long userId, long communityId) {
        if (!repository.hasActiveMembership(userId, communityId)) {
            throw new AccessDeniedException("Active community membership is required");
        }
    }

    public boolean hasActiveMembership(long userId, long communityId) {
        return repository.hasActiveMembership(userId, communityId);
    }
}
