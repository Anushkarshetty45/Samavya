package com.samavya.backend.repository;

import com.samavya.backend.Community;
import com.samavya.backend.CommunityMembership;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommunityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Community> findAll() {
        return jdbcTemplate.query(
                """
                        SELECT id, name, description, address, created_at
                        FROM communities ORDER BY name, id
                        """,
                (resultSet, rowNumber) -> new Community(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("address"),
                        resultSet.getObject("created_at", java.time.OffsetDateTime.class)));
    }

    public Optional<Community> findById(long communityId) {
        List<Community> communities = jdbcTemplate.query(
                """
                        SELECT id, name, description, address, created_at
                        FROM communities WHERE id = ?
                        """,
                (resultSet, rowNumber) -> new Community(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("address"),
                        resultSet.getObject("created_at", java.time.OffsetDateTime.class)),
                communityId);
        return communities.stream().findFirst();
    }

    public Community create(String name, String description, String address) {
        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO communities (name, description, address)
                        VALUES (?, ?, ?)
                        RETURNING id, name, description, address, created_at
                        """,
                (resultSet, rowNumber) -> new Community(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("address"),
                        resultSet.getObject("created_at", java.time.OffsetDateTime.class)),
                name,
                description,
                address);
    }

    public boolean exists(long communityId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM communities WHERE id = ?",
                Integer.class,
                communityId);
        return count != null && count > 0;
    }

    public CommunityMembership joinAsMember(long userId, long communityId) {
        jdbcTemplate.update(
                """
                        INSERT INTO community_memberships (user_id, community_id, role, status)
                        VALUES (?, ?, 'MEMBER', 'ACTIVE')
                        ON CONFLICT (user_id, community_id, role)
                        DO UPDATE SET status = 'ACTIVE', joined_at = NOW()
                        """,
                userId,
                communityId);
        return jdbcTemplate.queryForObject(
                """
                        SELECT c.id AS community_id, c.name AS community_name,
                               cm.role, cm.status, cm.joined_at
                        FROM community_memberships cm
                        JOIN communities c ON c.id = cm.community_id
                        WHERE cm.user_id = ? AND cm.community_id = ? AND cm.role = 'MEMBER'
                        """,
                (resultSet, rowNumber) -> new CommunityMembership(
                        resultSet.getLong("community_id"),
                        resultSet.getString("community_name"),
                        resultSet.getString("role"),
                        resultSet.getString("status"),
                        resultSet.getObject("joined_at", java.time.OffsetDateTime.class)),
                userId,
                communityId);
    }

    public List<CommunityMembership> findMembershipsForUser(long userId) {
        return jdbcTemplate.query(
                """
                        SELECT c.id AS community_id, c.name AS community_name,
                               cm.role, cm.status, cm.joined_at
                        FROM community_memberships cm
                        JOIN communities c ON c.id = cm.community_id
                        WHERE cm.user_id = ?
                        ORDER BY c.name, c.id, cm.role
                        """,
                (resultSet, rowNumber) -> new CommunityMembership(
                        resultSet.getLong("community_id"),
                        resultSet.getString("community_name"),
                        resultSet.getString("role"),
                        resultSet.getString("status"),
                        resultSet.getObject("joined_at", java.time.OffsetDateTime.class)),
                userId);
    }

    public boolean hasActiveMembership(long userId, long communityId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM community_memberships
                        WHERE user_id = ? AND community_id = ? AND status = 'ACTIVE'
                        """,
                Integer.class,
                userId,
                communityId);
        return count != null && count > 0;
    }
}
