package com.samavya.backend.community;

import com.samavya.backend.Community;
import com.samavya.backend.CommunityMembership;
import com.samavya.backend.repository.CommunityRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityService {

    private final CommunityRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public CommunityService(CommunityRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Community> getCommunities() {
        return repository.findAll();
    }

    public Community getCommunity(long communityId) {
        return repository.findById(communityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Community createCommunity(
            long userId,
            String name,
            String description,
            String address) {
        Community community = repository.create(name.trim(), description, address);
        jdbcTemplate.update(
                """
                        INSERT INTO community_memberships (user_id, community_id, role, status)
                        VALUES (?, ?, 'MEMBER', 'ACTIVE')
                        """,
                userId,
                community.id());
        return community;
    }

    @Transactional
    public CommunityMembership joinCommunity(long userId, long communityId) {
        if (!repository.exists(communityId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return repository.joinAsMember(userId, communityId);
    }

    public List<CommunityMembership> getMemberships(long authenticatedUserId) {
        return repository.findMembershipsForUser(authenticatedUserId);
    }
}
