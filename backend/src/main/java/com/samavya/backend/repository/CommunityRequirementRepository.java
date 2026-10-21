package com.samavya.backend.repository;

import com.samavya.backend.CommunityRequirement;
import com.samavya.backend.RequirementType;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityRequirementRepository {

    private static final String SELECT_COLUMNS = """
            SELECT id, community_id, created_by, type, service_category_id, title,
                   description, quantity, quantity_unit, budget, needed_by, status, created_at
            FROM requirements
            """;

    private final JdbcTemplate jdbcTemplate;

    public CommunityRequirementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CommunityRequirement create(
            long communityId,
            long createdBy,
            RequirementType type,
            Long serviceCategoryId,
            String title,
            String description,
            java.math.BigDecimal quantity,
            String quantityUnit,
            java.math.BigDecimal budget,
            java.time.OffsetDateTime neededBy) {
        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO requirements (
                            community_id, created_by, type, service_category_id, title,
                            description, quantity, quantity_unit, budget, needed_by
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        RETURNING id, community_id, created_by, type, service_category_id, title,
                                  description, quantity, quantity_unit, budget, needed_by,
                                  status, created_at
                        """,
                (resultSet, rowNumber) -> mapRequirement(resultSet),
                communityId,
                createdBy,
                type.name(),
                serviceCategoryId,
                title,
                description,
                quantity,
                quantityUnit,
                budget,
                neededBy);
    }

    public List<CommunityRequirement> findByCommunity(
            long communityId,
            RequirementType type) {
        String sql = SELECT_COLUMNS + " WHERE community_id = ?";
        if (type != null) {
            sql += " AND type = ?";
        }
        sql += " ORDER BY created_at DESC, id DESC";
        if (type == null) {
            return jdbcTemplate.query(
                    sql,
                    (resultSet, rowNumber) -> mapRequirement(resultSet),
                    communityId);
        }
        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> mapRequirement(resultSet),
                communityId,
                type.name());
    }

    public Optional<CommunityRequirement> findById(long requirementId) {
        List<CommunityRequirement> requirements = jdbcTemplate.query(
                SELECT_COLUMNS + " WHERE id = ?",
                (resultSet, rowNumber) -> mapRequirement(resultSet),
                requirementId);
        return requirements.stream().findFirst();
    }

    public Optional<CommunityRequirement> cancelIfOpen(long requirementId, long createdBy) {
        List<CommunityRequirement> requirements = jdbcTemplate.query(
                """
                        UPDATE requirements
                        SET status = 'CANCELLED'
                        WHERE id = ? AND created_by = ? AND status = 'OPEN'
                        RETURNING id, community_id, created_by, type, service_category_id, title,
                                  description, quantity, quantity_unit, budget, needed_by,
                                  status, created_at
                        """,
                (resultSet, rowNumber) -> mapRequirement(resultSet),
                requirementId,
                createdBy);
        return requirements.stream().findFirst();
    }

    public boolean isActiveServiceCategory(long categoryId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*) FROM service_categories
                        WHERE id = ? AND is_active = TRUE
                        """,
                Integer.class,
                categoryId);
        return count != null && count > 0;
    }

    private static CommunityRequirement mapRequirement(
            java.sql.ResultSet resultSet) throws java.sql.SQLException {
        long categoryId = resultSet.getLong("service_category_id");
        Long nullableCategoryId = resultSet.wasNull() ? null : categoryId;
        return new CommunityRequirement(
                resultSet.getLong("id"),
                resultSet.getLong("community_id"),
                resultSet.getLong("created_by"),
                RequirementType.valueOf(resultSet.getString("type")),
                nullableCategoryId,
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getBigDecimal("quantity"),
                resultSet.getString("quantity_unit"),
                resultSet.getBigDecimal("budget"),
                resultSet.getObject("needed_by", java.time.OffsetDateTime.class),
                resultSet.getString("status"),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class));
    }
}
