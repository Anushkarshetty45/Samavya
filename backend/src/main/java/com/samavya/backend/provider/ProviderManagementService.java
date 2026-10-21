package com.samavya.backend.provider;

import com.samavya.backend.GigStatus;
import com.samavya.backend.PricingType;
import com.samavya.backend.ProviderGig;
import com.samavya.backend.ProviderProfile;
import com.samavya.backend.ProviderServiceOffering;
import com.samavya.backend.community.CommunityAuthorizationService;
import com.samavya.backend.repository.ProviderManagementRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProviderManagementService {

    private final ProviderManagementRepository repository;
    private final CommunityAuthorizationService communityAuthorization;

    public ProviderManagementService(
            ProviderManagementRepository repository,
            CommunityAuthorizationService communityAuthorization) {
        this.repository = repository;
        this.communityAuthorization = communityAuthorization;
    }

    @Transactional
    public ProviderProfileCreation createProfile(long userId, ProviderProfileRequest request) {
        var existing = repository.findProfileForUser(userId);
        if (existing.isPresent()) {
            return new ProviderProfileCreation(existing.get(), false);
        }
        ProviderProfile profile = repository.createProfile(
                userId,
                request.description(),
                request.available() == null || request.available());
        return new ProviderProfileCreation(profile, true);
    }

    @Transactional(readOnly = true)
    public ProviderProfile getProfile(long userId) {
        return repository.findProfileForUser(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ProviderProfile updateProfile(long userId, ProviderProfileRequest request) {
        if (request.available() == null) {
            throw new InvalidProviderDataException(
                    "The available field is required when updating a profile");
        }
        return repository.updateProfile(
                        userId,
                        request.description(),
                        request.available())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ProviderServiceOffering createOffering(
            long userId,
            ProviderServiceRequest request) {
        requireProviderProfile(userId);
        communityAuthorization.requireActiveMembership(userId, request.communityId());
        if (!repository.isActiveCategory(request.categoryId())) {
            throw new InvalidProviderDataException("The service category is not active");
        }
        return repository.createOffering(
                userId,
                request.communityId(),
                request.categoryId(),
                request.priceFrom());
    }

    @Transactional(readOnly = true)
    public List<ProviderServiceOffering> getOfferings(long userId) {
        return repository.findOfferingsForUser(userId);
    }

    @Transactional
    public ProviderServiceOffering updateOffering(
            long userId,
            long offeringId,
            UpdateProviderServiceRequest request) {
        if (offeringId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ProviderServiceOffering existing = repository.findOffering(userId, offeringId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        communityAuthorization.requireActiveMembership(userId, existing.communityId());
        boolean nextActive = request.active();
        if (nextActive && !repository.isActiveCategory(existing.categoryId())) {
            throw new InvalidProviderDataException(
                    "An offering cannot be activated for an inactive service category");
        }
        return repository.updateOffering(
                        userId,
                        offeringId,
                        request.priceFrom(),
                        nextActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ProviderGig createGig(long userId, long offeringId, GigRequest request) {
        if (offeringId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        ProviderServiceOffering offering = repository.findOffering(userId, offeringId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        communityAuthorization.requireActiveMembership(userId, offering.communityId());
        if (!offering.active()) {
            throw new InvalidGigStateException(
                    "Gigs can only be created for active service offerings");
        }
        validatePricing(request.pricingType(), request.price());
        return repository.createGig(
                userId,
                offeringId,
                request.title().trim(),
                request.description(),
                request.pricingType(),
                request.price());
    }

    @Transactional(readOnly = true)
    public List<ProviderGig> getGigs(long userId) {
        return repository.findGigsForUser(userId);
    }

    @Transactional(readOnly = true)
    public ProviderGig getGig(long userId, long gigId) {
        return findOwnedGig(userId, gigId);
    }

    @Transactional
    public ProviderGig updateGig(long userId, long gigId, GigRequest request) {
        ProviderGig existing = findOwnedGig(userId, gigId);
        requireGigCommunityMembership(userId, existing);
        if (existing.status() == GigStatus.CLOSED) {
            throw new InvalidGigStateException("Closed gigs cannot be updated");
        }
        validatePricing(request.pricingType(), request.price());
        return repository.updateGig(
                        userId,
                        gigId,
                        request.title().trim(),
                        request.description(),
                        request.pricingType(),
                        request.price())
                .orElseThrow(() -> new InvalidGigStateException(
                        "The gig changed state and can no longer be updated"));
    }

    @Transactional
    public ProviderGig pauseGig(long userId, long gigId) {
        return transitionGig(userId, gigId, GigStatus.ACTIVE, GigStatus.PAUSED);
    }

    @Transactional
    public ProviderGig resumeGig(long userId, long gigId) {
        return transitionGig(userId, gigId, GigStatus.PAUSED, GigStatus.ACTIVE);
    }

    @Transactional
    public ProviderGig closeGig(long userId, long gigId) {
        ProviderGig existing = findOwnedGig(userId, gigId);
        requireGigCommunityMembership(userId, existing);
        if (existing.status() != GigStatus.ACTIVE && existing.status() != GigStatus.PAUSED) {
            throw new InvalidGigStateException("Only active or paused gigs can be closed");
        }
        return repository.transitionGig(
                        userId,
                        gigId,
                        existing.status(),
                        GigStatus.CLOSED)
                .orElseThrow(() -> new InvalidGigStateException(
                        "The gig changed state and can no longer be closed"));
    }

    private ProviderGig transitionGig(
            long userId,
            long gigId,
            GigStatus expected,
            GigStatus next) {
        ProviderGig existing = findOwnedGig(userId, gigId);
        requireGigCommunityMembership(userId, existing);
        if (existing.status() != expected) {
            throw new InvalidGigStateException(
                    "Gig must be " + expected.name() + " to transition to " + next.name());
        }
        return repository.transitionGig(userId, gigId, expected, next)
                .orElseThrow(() -> new InvalidGigStateException(
                        "The gig changed state and can no longer be updated"));
    }

    private ProviderGig findOwnedGig(long userId, long gigId) {
        if (gigId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return repository.findGig(userId, gigId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void requireGigCommunityMembership(long userId, ProviderGig gig) {
        ProviderServiceOffering offering = repository.findOffering(userId, gig.providerServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        communityAuthorization.requireActiveMembership(userId, offering.communityId());
    }

    private void requireProviderProfile(long userId) {
        if (!repository.hasProviderProfile(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Create a provider profile before publishing an offering");
        }
    }

    private static void validatePricing(PricingType pricingType, BigDecimal price) {
        if (pricingType == PricingType.QUOTE) {
            if (price != null) {
                throw new InvalidProviderDataException(
                        "QUOTE gigs must not include a fixed price");
            }
            return;
        }
        if (price == null || price.signum() < 0) {
            throw new InvalidProviderDataException(
                    "FIXED and HOURLY gigs require a nonnegative price");
        }
    }
}
