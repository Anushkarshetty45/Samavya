package com.samavya.backend;

import com.samavya.backend.service.MarketplaceService;
import com.samavya.backend.auth.AuthenticatedUser;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class MarketplaceController {

    private final MarketplaceService service;

    public MarketplaceController(MarketplaceService service) {
        this.service = service;
    }

    @GetMapping("/provider-profiles/{providerId}")
    public ProviderProfile getProviderProfile(
            @PathVariable long providerId) {
        if (providerId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return service.getProviderProfile(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/service-discovery")
    public List<ProviderServiceListing> discoverProviderServices(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Long communityId,
            @RequestParam(required = false) Long categoryId) {
        if ((communityId != null && communityId <= 0)
                || (categoryId != null && categoryId <= 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return service.discoverProviderServices(
                AuthenticatedUser.id(jwt),
                communityId,
                categoryId);
    }
}
