package com.samavya.backend.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samavya.backend.GigStatus;
import com.samavya.backend.PricingType;
import com.samavya.backend.ProviderGig;
import com.samavya.backend.ProviderProfile;
import com.samavya.backend.ProviderServiceOffering;
import com.samavya.backend.community.CommunityAuthorizationService;
import com.samavya.backend.repository.ProviderManagementRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class ProviderManagementServiceTests {

    private ProviderManagementRepository repository;
    private CommunityAuthorizationService authorization;
    private ProviderManagementService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProviderManagementRepository.class);
        authorization = mock(CommunityAuthorizationService.class);
        service = new ProviderManagementService(repository, authorization);
    }

    @Test
    void createsIdempotentDefaultPendingProviderProfile() {
        ProviderProfile profile = profile("PENDING");
        when(repository.findProfileForUser(11)).thenReturn(Optional.empty());
        when(repository.createProfile(11, "Available for local work", true))
                .thenReturn(profile);

        ProviderProfileCreation creation = service.createProfile(
                11,
                new ProviderProfileRequest("Available for local work", null));

        assertThat(creation.created()).isTrue();
        assertThat(creation.profile().verificationStatus()).isEqualTo("PENDING");
        verify(repository).createProfile(11, "Available for local work", true);
    }

    @Test
    void existingProviderProfileIsReturnedInsteadOfDuplicated() {
        ProviderProfile profile = profile("PENDING");
        when(repository.findProfileForUser(11)).thenReturn(Optional.of(profile));

        ProviderProfileCreation creation = service.createProfile(
                11,
                new ProviderProfileRequest("Ignored description", false));

        assertThat(creation.created()).isFalse();
        assertThat(creation.profile()).isEqualTo(profile);
    }

    @Test
    void createsOfferingsOnlyForMembersWithProviderProfilesAndActiveCategories() {
        when(repository.hasProviderProfile(11)).thenReturn(true);
        when(repository.isActiveCategory(3)).thenReturn(true);
        ProviderServiceOffering offering = offering(true);
        when(repository.createOffering(11, 7, 3, new BigDecimal("25.00")))
                .thenReturn(offering);

        assertThat(service.createOffering(
                11, new ProviderServiceRequest(7L, 3L, new BigDecimal("25.00"))))
                .isEqualTo(offering);

        verify(authorization).requireActiveMembership(11, 7);
    }

    @Test
    void rejectsPublishingWithoutMembershipProfileOrActiveCategory() {
        when(repository.hasProviderProfile(11)).thenReturn(true);
        doThrow(new AccessDeniedException("Membership required"))
                .when(authorization).requireActiveMembership(11, 7);
        assertThatThrownBy(() -> service.createOffering(
                11, new ProviderServiceRequest(7L, 3L, BigDecimal.ZERO)))
                .isInstanceOf(AccessDeniedException.class);

        when(repository.hasProviderProfile(12)).thenReturn(false);
        assertThatThrownBy(() -> service.createOffering(
                12, new ProviderServiceRequest(7L, 3L, BigDecimal.ZERO)))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);

        when(repository.hasProviderProfile(13)).thenReturn(true);
        when(repository.isActiveCategory(3)).thenReturn(false);
        assertThatThrownBy(() -> service.createOffering(
                13, new ProviderServiceRequest(7L, 3L, BigDecimal.ZERO)))
                .isInstanceOf(InvalidProviderDataException.class);
    }

    @Test
    void offeringUpdateChecksOwnershipAndMembershipAndSupportsDeactivation() {
        when(repository.findOffering(11, 8)).thenReturn(Optional.of(offering(true)));
        when(repository.updateOffering(11, 8, new BigDecimal("30.00"), false))
                .thenReturn(Optional.of(offering(false)));

        ProviderServiceOffering updated = service.updateOffering(
                11,
                8,
                new UpdateProviderServiceRequest(new BigDecimal("30.00"), false));

        assertThat(updated.active()).isFalse();
        verify(authorization).requireActiveMembership(11, 7);
    }

    @Test
    void validatesPricingRulesAndGigOwnershipLifecycle() {
        when(repository.findOffering(11, 8)).thenReturn(Optional.of(offering(true)));
        when(repository.createGig(
                11, 8, "Tap repair", "Repair", PricingType.FIXED, new BigDecimal("45.00")))
                .thenReturn(gig(GigStatus.ACTIVE));
        ProviderGig created = service.createGig(
                11,
                8,
                new GigRequest(
                        "Tap repair",
                        "Repair",
                        PricingType.FIXED,
                        new BigDecimal("45.00")));
        assertThat(created.status()).isEqualTo(GigStatus.ACTIVE);

        assertThatThrownBy(() -> service.createGig(
                11, 8, new GigRequest("Quote", null, PricingType.QUOTE, BigDecimal.ONE)))
                .isInstanceOf(InvalidProviderDataException.class);
        assertThatThrownBy(() -> service.createGig(
                11, 8, new GigRequest("Fixed", null, PricingType.FIXED, null)))
                .isInstanceOf(InvalidProviderDataException.class);

        when(repository.findGig(11, 9)).thenReturn(Optional.of(gig(GigStatus.ACTIVE)));
        when(repository.findOffering(11, 8)).thenReturn(Optional.of(offering(true)));
        when(repository.transitionGig(11, 9, GigStatus.ACTIVE, GigStatus.PAUSED))
                .thenReturn(Optional.of(gig(GigStatus.PAUSED)));
        assertThat(service.pauseGig(11, 9).status()).isEqualTo(GigStatus.PAUSED);

        when(repository.findGig(11, 10)).thenReturn(Optional.of(gig(GigStatus.CLOSED)));
        when(repository.findOffering(11, 8)).thenReturn(Optional.of(offering(true)));
        assertThatThrownBy(() -> service.resumeGig(11, 10))
                .isInstanceOf(InvalidGigStateException.class);
    }

    private static ProviderProfile profile(String verification) {
        return new ProviderProfile(5, "Provider", "Description", true, verification);
    }

    private static ProviderServiceOffering offering(boolean active) {
        return new ProviderServiceOffering(
                8, 7, "Community", 3, "Plumber", new BigDecimal("25.00"), active);
    }

    private static ProviderGig gig(GigStatus status) {
        return new ProviderGig(
                9,
                8,
                "Tap repair",
                "Repair",
                PricingType.FIXED,
                new BigDecimal("45.00"),
                status,
                OffsetDateTime.parse("2026-10-21T10:00:00Z"));
    }
}
