package com.samavya.backend.service;

import com.samavya.backend.ProviderProfile;
import com.samavya.backend.ProviderServiceListing;
import com.samavya.backend.community.CommunityAuthorizationService;
import com.samavya.backend.repository.MarketplaceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MarketplaceService {

    private final MarketplaceRepository repository;
    private final CommunityAuthorizationService communityAuthorization;

    public MarketplaceService(
            MarketplaceRepository repository,
            CommunityAuthorizationService communityAuthorization) {
        this.repository = repository;
        this.communityAuthorization = communityAuthorization;
    }

    public Optional<ProviderProfile> getProviderProfile(long providerId) {
        return repository.findProviderProfile(providerId);
    }

    public List<ProviderServiceListing> discoverProviderServices(
            long userId,
            Long communityId,
            Long categoryId) {
        if (communityId != null) {
            communityAuthorization.requireActiveMembership(userId, communityId);
        }
        return repository.findActiveProviderServices(userId, communityId, categoryId);
    }
}
