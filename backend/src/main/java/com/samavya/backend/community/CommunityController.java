package com.samavya.backend.community;

import com.samavya.backend.Community;
import com.samavya.backend.CommunityMembership;
import com.samavya.backend.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService service;

    public CommunityController(CommunityService service) {
        this.service = service;
    }

    @GetMapping
    public List<Community> getCommunities() {
        return service.getCommunities();
    }

    @GetMapping("/{communityId}")
    public Community getCommunity(@PathVariable long communityId) {
        return service.getCommunity(communityId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Community createCommunity(
            @Valid @RequestBody CreateCommunityRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return service.createCommunity(
                AuthenticatedUser.id(jwt),
                request.name(),
                request.description(),
                request.address());
    }

    @PostMapping("/{communityId}/memberships")
    public CommunityMembership joinCommunity(
            @PathVariable long communityId,
            @AuthenticationPrincipal Jwt jwt) {
        if (communityId <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST);
        }
        return service.joinCommunity(AuthenticatedUser.id(jwt), communityId);
    }

    @GetMapping("/me/memberships")
    public List<CommunityMembership> getMyMemberships(@AuthenticationPrincipal Jwt jwt) {
        return service.getMemberships(AuthenticatedUser.id(jwt));
    }
}
