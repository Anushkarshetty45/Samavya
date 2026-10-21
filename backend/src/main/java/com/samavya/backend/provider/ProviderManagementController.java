package com.samavya.backend.provider;

import com.samavya.backend.ProviderGig;
import com.samavya.backend.ProviderProfile;
import com.samavya.backend.ProviderServiceOffering;
import com.samavya.backend.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderManagementController {

    private final ProviderManagementService service;

    public ProviderManagementController(ProviderManagementService service) {
        this.service = service;
    }

    @PostMapping("/api/provider-profile/me")
    public ResponseEntity<ProviderProfile> createProfile(
            @RequestBody ProviderProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        ProviderProfileCreation creation = service.createProfile(
                AuthenticatedUser.id(jwt),
                request);
        return ResponseEntity.status(
                        creation.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(creation.profile());
    }

    @GetMapping("/api/provider-profile/me")
    public ProviderProfile getProfile(@AuthenticationPrincipal Jwt jwt) {
        return service.getProfile(AuthenticatedUser.id(jwt));
    }

    @PutMapping("/api/provider-profile/me")
    public ProviderProfile updateProfile(
            @Valid @RequestBody ProviderProfileRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return service.updateProfile(AuthenticatedUser.id(jwt), request);
    }

    @PostMapping("/api/provider-services")
    public ResponseEntity<ProviderServiceOffering> createOffering(
            @Valid @RequestBody ProviderServiceRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createOffering(AuthenticatedUser.id(jwt), request));
    }

    @GetMapping("/api/provider-services/me")
    public List<ProviderServiceOffering> getOfferings(@AuthenticationPrincipal Jwt jwt) {
        return service.getOfferings(AuthenticatedUser.id(jwt));
    }

    @PutMapping("/api/provider-services/{offeringId}")
    public ProviderServiceOffering updateOffering(
            @PathVariable long offeringId,
            @Valid @RequestBody UpdateProviderServiceRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return service.updateOffering(
                AuthenticatedUser.id(jwt),
                offeringId,
                request);
    }

    @PostMapping("/api/provider-services/{offeringId}/gigs")
    public ResponseEntity<ProviderGig> createGig(
            @PathVariable long offeringId,
            @Valid @RequestBody GigRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createGig(AuthenticatedUser.id(jwt), offeringId, request));
    }

    @GetMapping("/api/gigs/me")
    public List<ProviderGig> getGigs(@AuthenticationPrincipal Jwt jwt) {
        return service.getGigs(AuthenticatedUser.id(jwt));
    }

    @GetMapping("/api/gigs/{gigId}")
    public ProviderGig getGig(
            @PathVariable long gigId,
            @AuthenticationPrincipal Jwt jwt) {
        return service.getGig(AuthenticatedUser.id(jwt), gigId);
    }

    @PutMapping("/api/gigs/{gigId}")
    public ProviderGig updateGig(
            @PathVariable long gigId,
            @Valid @RequestBody GigRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return service.updateGig(AuthenticatedUser.id(jwt), gigId, request);
    }

    @PostMapping("/api/gigs/{gigId}/pause")
    public ProviderGig pauseGig(
            @PathVariable long gigId,
            @AuthenticationPrincipal Jwt jwt) {
        return service.pauseGig(AuthenticatedUser.id(jwt), gigId);
    }

    @PostMapping("/api/gigs/{gigId}/resume")
    public ProviderGig resumeGig(
            @PathVariable long gigId,
            @AuthenticationPrincipal Jwt jwt) {
        return service.resumeGig(AuthenticatedUser.id(jwt), gigId);
    }

    @PostMapping("/api/gigs/{gigId}/close")
    public ProviderGig closeGig(
            @PathVariable long gigId,
            @AuthenticationPrincipal Jwt jwt) {
        return service.closeGig(AuthenticatedUser.id(jwt), gigId);
    }
}
