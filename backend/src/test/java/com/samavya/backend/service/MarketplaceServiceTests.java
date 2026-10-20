package com.samavya.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samavya.backend.ProviderServiceListing;
import com.samavya.backend.community.CommunityAuthorizationService;
import com.samavya.backend.repository.MarketplaceRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

class MarketplaceServiceTests {

    private MarketplaceRepository repository;
    private CommunityAuthorizationService communityAuthorization;
    private MarketplaceService service;

    @BeforeEach
    void setUp() {
        repository = mock(MarketplaceRepository.class);
        communityAuthorization = mock(CommunityAuthorizationService.class);
        service = new MarketplaceService(repository, communityAuthorization);
    }

    @Test
    void forwardsDiscoveryFiltersToTheRepository() {
        when(repository.findActiveProviderServices(1L, 2L, 4L)).thenReturn(List.of());

        assertThat(service.discoverProviderServices(1L, 2L, 4L)).isEmpty();

        verify(communityAuthorization).requireActiveMembership(1L, 2L);
        verify(repository).findActiveProviderServices(1L, 2L, 4L);
    }

    @Test
    void requiresMembershipBeforeFilteredDiscovery() {
        org.springframework.security.access.AccessDeniedException failure =
                new org.springframework.security.access.AccessDeniedException("Not a member");
        org.mockito.Mockito.doThrow(failure)
                .when(communityAuthorization).requireActiveMembership(1L, 2L);

        assertThatThrownBy(() -> service.discoverProviderServices(1L, 2L, null))
                .isSameAs(failure);
        org.mockito.Mockito.verifyNoInteractions(repository);
    }

    @Test
    void returnsProfileLookupResultFromTheRepository() {
        when(repository.findProviderProfile(5L)).thenReturn(Optional.empty());

        assertThat(service.getProviderProfile(5L)).isEmpty();

        verify(repository).findProviderProfile(5L);
    }

    @Test
    void doesNotHideDatabaseErrors() {
        DataAccessResourceFailureException failure =
                new DataAccessResourceFailureException("Database unavailable");
        when(repository.findActiveProviderServices(1L, null, null)).thenThrow(failure);

        assertThatThrownBy(() -> service.discoverProviderServices(1L, null, null))
                .isSameAs(failure);
    }
}
