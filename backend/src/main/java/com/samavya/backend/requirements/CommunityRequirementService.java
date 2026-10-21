package com.samavya.backend.requirements;

import com.samavya.backend.CommunityRequirement;
import com.samavya.backend.RequirementType;
import com.samavya.backend.community.CommunityAuthorizationService;
import com.samavya.backend.repository.CommunityRequirementRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityRequirementService {

    private final CommunityRequirementRepository repository;
    private final CommunityAuthorizationService communityAuthorization;

    public CommunityRequirementService(
            CommunityRequirementRepository repository,
            CommunityAuthorizationService communityAuthorization) {
        this.repository = repository;
        this.communityAuthorization = communityAuthorization;
    }

    @Transactional
    public CommunityRequirement create(
            long userId,
            long communityId,
            CreateRequirementRequest request) {
        communityAuthorization.requireActiveMembership(userId, communityId);
        validateServiceCategory(request.type(), request.serviceCategoryId());
        return repository.create(
                communityId,
                userId,
                request.type(),
                request.serviceCategoryId(),
                request.title().trim(),
                request.description(),
                request.quantity(),
                request.quantityUnit(),
                request.budget(),
                request.neededBy());
    }

    @Transactional(readOnly = true)
    public List<CommunityRequirement> list(
            long userId,
            long communityId,
            RequirementType type) {
        communityAuthorization.requireActiveMembership(userId, communityId);
        return repository.findByCommunity(communityId, type);
    }

    @Transactional(readOnly = true)
    public CommunityRequirement get(long userId, long requirementId) {
        CommunityRequirement requirement = findRequirement(requirementId);
        communityAuthorization.requireActiveMembership(userId, requirement.communityId());
        return requirement;
    }

    @Transactional
    public CommunityRequirement cancel(long userId, long requirementId) {
        CommunityRequirement requirement = findRequirement(requirementId);
        communityAuthorization.requireActiveMembership(userId, requirement.communityId());
        if (requirement.createdBy() != userId) {
            throw new AccessDeniedException("Only the requirement creator can cancel it");
        }
        if (!"OPEN".equals(requirement.status())) {
            throw new InvalidRequirementStateException();
        }
        return repository.cancelIfOpen(requirementId, userId)
                .orElseThrow(InvalidRequirementStateException::new);
    }

    private CommunityRequirement findRequirement(long requirementId) {
        if (requirementId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return repository.findById(requirementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void validateServiceCategory(RequirementType type, Long serviceCategoryId) {
        if (type == RequirementType.SERVICE) {
            if (serviceCategoryId == null
                    || !repository.isActiveServiceCategory(serviceCategoryId)) {
                throw new InvalidRequirementException(
                        "Service requirements need an active service category");
            }
        } else if (serviceCategoryId != null) {
            throw new InvalidRequirementException(
                    "Only service requirements can include a service category");
        }
    }
}
