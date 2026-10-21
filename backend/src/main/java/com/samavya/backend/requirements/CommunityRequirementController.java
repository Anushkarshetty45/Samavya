package com.samavya.backend.requirements;

import com.samavya.backend.CommunityRequirement;
import com.samavya.backend.RequirementType;
import com.samavya.backend.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommunityRequirementController {

    private final CommunityRequirementService service;

    public CommunityRequirementController(CommunityRequirementService service) {
        this.service = service;
    }

    @PostMapping("/api/communities/{communityId}/requirements")
    @ResponseStatus(HttpStatus.CREATED)
    public CommunityRequirement create(
            @PathVariable long communityId,
            @Valid @RequestBody CreateRequirementRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        if (communityId <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST);
        }
        return service.create(AuthenticatedUser.id(jwt), communityId, request);
    }

    @GetMapping("/api/communities/{communityId}/requirements")
    public List<CommunityRequirement> list(
            @PathVariable long communityId,
            @RequestParam(required = false) RequirementType type,
            @AuthenticationPrincipal Jwt jwt) {
        if (communityId <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST);
        }
        return service.list(AuthenticatedUser.id(jwt), communityId, type);
    }

    @GetMapping("/api/requirements/{requirementId}")
    public CommunityRequirement get(
            @PathVariable long requirementId,
            @AuthenticationPrincipal Jwt jwt) {
        return service.get(AuthenticatedUser.id(jwt), requirementId);
    }

    @PostMapping("/api/requirements/{requirementId}/cancel")
    public CommunityRequirement cancel(
            @PathVariable long requirementId,
            @AuthenticationPrincipal Jwt jwt) {
        return service.cancel(AuthenticatedUser.id(jwt), requirementId);
    }
}
