package com.samavya.backend.requirements;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.samavya.backend.CommunityRequirement;
import com.samavya.backend.RequirementType;
import com.samavya.backend.community.CommunityAuthorizationService;
import com.samavya.backend.repository.CommunityRequirementRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class CommunityRequirementServiceTests {

    private CommunityRequirementRepository repository;
    private CommunityAuthorizationService authorization;
    private CommunityRequirementService service;

    @BeforeEach
    void setUp() {
        repository = mock(CommunityRequirementRepository.class);
        authorization = mock(CommunityAuthorizationService.class);
        service = new CommunityRequirementService(repository, authorization);
    }

    @Test
    void requiresMembershipBeforeCreatingAndStoresAuthenticatedCreator() {
        CreateRequirementRequest request = new CreateRequirementRequest(
                RequirementType.PRODUCT,
                null,
                "  Need chairs  ",
                "For an event",
                new BigDecimal("2"),
                "pieces",
                new BigDecimal("50.00"),
                null);
        CommunityRequirement created = requirement(11, 7, 42, "OPEN");
        when(repository.create(
                7, 42, RequirementType.PRODUCT, null, "Need chairs", "For an event",
                new BigDecimal("2"), "pieces", new BigDecimal("50.00"), null))
                .thenReturn(created);

        assertThat(service.create(42, 7, request)).isEqualTo(created);

        verify(authorization).requireActiveMembership(42, 7);
        verify(repository).create(
                7, 42, RequirementType.PRODUCT, null, "Need chairs", "For an event",
                new BigDecimal("2"), "pieces", new BigDecimal("50.00"), null);
    }

    @Test
    void rejectsMissingOrInvalidServiceCategoryBeforeInsert() {
        CreateRequirementRequest request = new CreateRequirementRequest(
                RequirementType.SERVICE, null, "Need plumbing", null, null, null, null, null);

        assertThatThrownBy(() -> service.create(42, 7, request))
                .isInstanceOf(InvalidRequirementException.class);

        verify(authorization).requireActiveMembership(42, 7);
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInactiveServiceCategory() {
        CreateRequirementRequest request = new CreateRequirementRequest(
                RequirementType.SERVICE, 9L, "Need plumbing", null, null, null, null, null);
        when(repository.isActiveServiceCategory(9)).thenReturn(false);

        assertThatThrownBy(() -> service.create(42, 7, request))
                .isInstanceOf(InvalidRequirementException.class);

        verify(repository).isActiveServiceCategory(9);
    }

    @Test
    void requiresMembershipBeforeListing() {
        when(repository.findByCommunity(7, RequirementType.RENTAL)).thenReturn(java.util.List.of());

        assertThat(service.list(42, 7, RequirementType.RENTAL)).isEmpty();

        verify(authorization).requireActiveMembership(42, 7);
        verify(repository).findByCommunity(7, RequirementType.RENTAL);
    }

    @Test
    void onlyCreatorCanCancelAndCancellationMustStillBeOpen() {
        when(repository.findById(3)).thenReturn(Optional.of(requirement(3, 7, 42, "OPEN")));
        when(repository.cancelIfOpen(3, 42))
                .thenReturn(Optional.of(requirement(3, 7, 42, "CANCELLED")));

        CommunityRequirement cancelled = service.cancel(42, 3);

        assertThat(cancelled.status()).isEqualTo("CANCELLED");
        verify(authorization).requireActiveMembership(42, 7);
        verify(repository).cancelIfOpen(3, 42);
    }

    @Test
    void rejectsNonOwnerAndNonOpenRequirementCancellation() {
        when(repository.findById(3)).thenReturn(Optional.of(requirement(3, 7, 42, "OPEN")));
        assertThatThrownBy(() -> service.cancel(10, 3))
                .isInstanceOf(AccessDeniedException.class);

        when(repository.findById(4)).thenReturn(Optional.of(requirement(4, 7, 42, "MATCHED")));
        assertThatThrownBy(() -> service.cancel(42, 4))
                .isInstanceOf(InvalidRequirementStateException.class);

        doThrow(new AccessDeniedException("Not a member"))
                .when(authorization).requireActiveMembership(15, 7);
        assertThatThrownBy(() -> service.cancel(15, 3))
                .isInstanceOf(AccessDeniedException.class);

        verify(repository, never()).cancelIfOpen(3, 10);
        verify(repository, never()).cancelIfOpen(4, 42);
    }

    private static CommunityRequirement requirement(
            long id, long communityId, long creatorId, String status) {
        return new CommunityRequirement(
                id,
                communityId,
                creatorId,
                RequirementType.PRODUCT,
                null,
                "Need chairs",
                null,
                new BigDecimal("2"),
                "pieces",
                new BigDecimal("50.00"),
                null,
                status,
                OffsetDateTime.parse("2026-10-21T10:00:00Z"));
    }
}
