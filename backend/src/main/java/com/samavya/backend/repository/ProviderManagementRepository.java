package com.samavya.backend.repository;

import com.samavya.backend.GigStatus;
import com.samavya.backend.PricingType;
import com.samavya.backend.ProviderGig;
import com.samavya.backend.ProviderProfile;
import com.samavya.backend.ProviderServiceOffering;
import com.samavya.backend.provider.DuplicateProviderOfferingException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProviderManagementRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProviderManagementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ProviderProfile> findProfileForUser(long userId) {
        List<ProviderProfile> profiles = jdbcTemplate.query(
                """
                        SELECT pp.id, u.name AS provider_name, pp.description,
                               pp.is_available, pp.verification_status
                        FROM provider_profiles pp
                        JOIN users u ON u.id = pp.user_id
                        WHERE pp.user_id = ?
                        """,
                (rs, rowNumber) -> mapProfile(rs),
                userId);
        return profiles.stream().findFirst();
    }

    public ProviderProfile createProfile(
            long userId,
            String description,
            boolean available) {
        List<Long> createdIds = jdbcTemplate.query(
                """
                        INSERT INTO provider_profiles (user_id, description, is_available)
                        VALUES (?, ?, ?)
                        ON CONFLICT (user_id) DO NOTHING
                        RETURNING id
                        """,
                (rs, rowNumber) -> rs.getLong("id"),
                userId,
                description,
                available);
        return findProfileForUser(userId)
                .orElseThrow(() -> new IllegalStateException(
                        createdIds.isEmpty()
                                ? "Profile was not created and no existing profile was found"
                                : "Created provider profile could not be retrieved"));
    }

    public Optional<ProviderProfile> updateProfile(
            long userId,
            String description,
            boolean available) {
        List<ProviderProfile> profiles = jdbcTemplate.query(
                """
                        UPDATE provider_profiles pp
                        SET description = ?, is_available = ?
                        FROM users u
                        WHERE pp.user_id = ? AND u.id = pp.user_id
                        RETURNING pp.id, u.name AS provider_name, pp.description,
                                  pp.is_available, pp.verification_status
                        """,
                (rs, rowNumber) -> mapProfile(rs),
                description,
                available,
                userId);
        return profiles.stream().findFirst();
    }

    public Optional<ProviderServiceOffering> findOffering(long userId, long offeringId) {
        List<ProviderServiceOffering> offerings = jdbcTemplate.query(
                offeringSelect() + """
                        WHERE ps.id = ? AND u.id = ?
                        """,
                (rs, rowNumber) -> mapOffering(rs),
                offeringId,
                userId);
        return offerings.stream().findFirst();
    }

    public List<ProviderServiceOffering> findOfferingsForUser(long userId) {
        return jdbcTemplate.query(
                offeringSelect() + "WHERE u.id = ? ORDER BY c.name, sc.name, ps.id",
                (rs, rowNumber) -> mapOffering(rs),
                userId);
    }

    public boolean isActiveCategory(long categoryId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM service_categories WHERE id = ? AND is_active = TRUE",
                Integer.class,
                categoryId);
        return count != null && count > 0;
    }

    public boolean hasProviderProfile(long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM provider_profiles WHERE user_id = ?",
                Integer.class,
                userId);
        return count != null && count > 0;
    }

    public boolean hasOffering(long userId, long offeringId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM provider_services ps
                        JOIN provider_profiles pp ON pp.id = ps.provider_id
                        WHERE pp.user_id = ? AND ps.id = ?
                        """,
                Integer.class,
                userId,
                offeringId);
        return count != null && count > 0;
    }

    public ProviderServiceOffering createOffering(
            long userId,
            long communityId,
            long categoryId,
            BigDecimal priceFrom) {
        List<Long> createdIds = jdbcTemplate.query(
                """
                        INSERT INTO provider_services (
                            provider_id, community_id, service_category_id, price_from
                        )
                        SELECT pp.id, ?, ?, ?
                        FROM provider_profiles pp
                        WHERE pp.user_id = ?
                        ON CONFLICT (provider_id, community_id, service_category_id)
                        DO NOTHING
                        RETURNING id
                        """,
                (rs, rowNumber) -> rs.getLong("id"),
                communityId,
                categoryId,
                priceFrom,
                userId);
        if (createdIds.isEmpty()) {
            throw new DuplicateProviderOfferingException();
        }
        return findOffering(userId, createdIds.getFirst())
                .orElseThrow(() -> new IllegalStateException(
                        "Created provider offering could not be retrieved"));
    }

    public Optional<ProviderServiceOffering> findOfferingByCommunityCategory(
            long userId,
            long communityId,
            long categoryId) {
        List<ProviderServiceOffering> offerings = jdbcTemplate.query(
                offeringSelect() + """
                        WHERE u.id = ? AND ps.community_id = ? AND ps.service_category_id = ?
                        """,
                (rs, rowNumber) -> mapOffering(rs),
                userId,
                communityId,
                categoryId);
        return offerings.stream().findFirst();
    }

    public Optional<ProviderServiceOffering> updateOffering(
            long userId,
            long offeringId,
            BigDecimal priceFrom,
            boolean active) {
        List<ProviderServiceOffering> offerings = jdbcTemplate.query(
                """
                        UPDATE provider_services ps
                        SET price_from = ?, is_active = ?
                        FROM provider_profiles pp, users u, communities c,
                             service_categories sc
                        WHERE ps.id = ? AND pp.id = ps.provider_id AND u.id = pp.user_id
                          AND c.id = ps.community_id AND sc.id = ps.service_category_id
                          AND u.id = ?
                        RETURNING ps.id, ps.community_id, c.name AS community_name,
                                  ps.service_category_id AS category_id,
                                  sc.name AS category_name, ps.price_from, ps.is_active
                        """,
                (rs, rowNumber) -> mapOffering(rs),
                priceFrom,
                active,
                offeringId,
                userId);
        return offerings.stream().findFirst();
    }

    public ProviderGig createGig(
            long userId,
            long offeringId,
            String title,
            String description,
            PricingType pricingType,
            BigDecimal price) {
        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO gigs (
                            provider_service_id, title, description, pricing_type, price
                        )
                        SELECT ps.id, ?, ?, ?, ?
                        FROM provider_services ps
                        JOIN provider_profiles pp ON pp.id = ps.provider_id
                        WHERE ps.id = ? AND pp.user_id = ?
                        RETURNING id, provider_service_id, title, description, pricing_type,
                                  price, status, created_at
                        """,
                (rs, rowNumber) -> mapGig(rs),
                title,
                description,
                pricingType.name(),
                price,
                offeringId,
                userId);
    }

    public List<ProviderGig> findGigsForUser(long userId) {
        return jdbcTemplate.query(
                gigSelect() + "WHERE pp.user_id = ? ORDER BY g.created_at DESC, g.id DESC",
                (rs, rowNumber) -> mapGig(rs),
                userId);
    }

    public Optional<ProviderGig> findGig(long userId, long gigId) {
        List<ProviderGig> gigs = jdbcTemplate.query(
                gigSelect() + "WHERE pp.user_id = ? AND g.id = ?",
                (rs, rowNumber) -> mapGig(rs),
                userId,
                gigId);
        return gigs.stream().findFirst();
    }

    public boolean gigExists(long gigId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gigs WHERE id = ?",
                Integer.class,
                gigId);
        return count != null && count > 0;
    }

    public boolean offeringIsActive(long userId, long offeringId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM provider_services ps
                        JOIN provider_profiles pp ON pp.id = ps.provider_id
                        WHERE pp.user_id = ? AND ps.id = ? AND ps.is_active = TRUE
                        """,
                Integer.class,
                userId,
                offeringId);
        return count != null && count > 0;
    }

    public Optional<ProviderGig> updateGig(
            long userId,
            long gigId,
            String title,
            String description,
            PricingType pricingType,
            BigDecimal price) {
        List<ProviderGig> gigs = jdbcTemplate.query(
                """
                        UPDATE gigs g
                        SET title = ?, description = ?, pricing_type = ?, price = ?
                        FROM provider_services ps, provider_profiles pp
                        WHERE g.id = ? AND g.provider_service_id = ps.id
                          AND ps.provider_id = pp.id AND pp.user_id = ?
                          AND g.status <> 'CLOSED'
                        RETURNING g.id, g.provider_service_id, g.title, g.description,
                                  g.pricing_type, g.price, g.status, g.created_at
                        """,
                (rs, rowNumber) -> mapGig(rs),
                title,
                description,
                pricingType.name(),
                price,
                gigId,
                userId);
        return gigs.stream().findFirst();
    }

    public Optional<ProviderGig> transitionGig(
            long userId,
            long gigId,
            GigStatus expectedStatus,
            GigStatus newStatus) {
        List<ProviderGig> gigs = jdbcTemplate.query(
                """
                        UPDATE gigs g
                        SET status = ?
                        FROM provider_services ps, provider_profiles pp
                        WHERE g.id = ? AND g.provider_service_id = ps.id
                          AND ps.provider_id = pp.id AND pp.user_id = ?
                          AND g.status = ?
                        RETURNING g.id, g.provider_service_id, g.title, g.description,
                                  g.pricing_type, g.price, g.status, g.created_at
                        """,
                (rs, rowNumber) -> mapGig(rs),
                newStatus.name(),
                gigId,
                userId,
                expectedStatus.name());
        return gigs.stream().findFirst();
    }

    private static String offeringSelect() {
        return """
                SELECT ps.id, ps.community_id, c.name AS community_name,
                       ps.service_category_id AS category_id, sc.name AS category_name,
                       ps.price_from, ps.is_active
                FROM provider_services ps
                JOIN provider_profiles pp ON pp.id = ps.provider_id
                JOIN users u ON u.id = pp.user_id
                JOIN communities c ON c.id = ps.community_id
                JOIN service_categories sc ON sc.id = ps.service_category_id
                """;
    }

    private static String gigSelect() {
        return """
                SELECT g.id, g.provider_service_id, g.title, g.description,
                       g.pricing_type, g.price, g.status, g.created_at
                FROM gigs g
                JOIN provider_services ps ON ps.id = g.provider_service_id
                JOIN provider_profiles pp ON pp.id = ps.provider_id
                """;
    }

    private static ProviderProfile mapProfile(ResultSet rs) throws SQLException {
        return new ProviderProfile(
                rs.getLong("id"),
                rs.getString("provider_name"),
                rs.getString("description"),
                rs.getBoolean("is_available"),
                rs.getString("verification_status"));
    }

    private static ProviderServiceOffering mapOffering(ResultSet rs) throws SQLException {
        return new ProviderServiceOffering(
                rs.getLong("id"),
                rs.getLong("community_id"),
                rs.getString("community_name"),
                rs.getLong("category_id"),
                rs.getString("category_name"),
                rs.getBigDecimal("price_from"),
                rs.getBoolean("is_active"));
    }

    private static ProviderGig mapGig(ResultSet rs) throws SQLException {
        return new ProviderGig(
                rs.getLong("id"),
                rs.getLong("provider_service_id"),
                rs.getString("title"),
                rs.getString("description"),
                PricingType.valueOf(rs.getString("pricing_type")),
                rs.getBigDecimal("price"),
                GigStatus.valueOf(rs.getString("status")),
                rs.getObject("created_at", java.time.OffsetDateTime.class));
    }

}
