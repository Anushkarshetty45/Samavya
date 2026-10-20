package com.samavya.backend.repository;

import com.samavya.backend.GigSummary;
import com.samavya.backend.ProviderProfile;
import com.samavya.backend.ProviderServiceListing;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

@Repository
public class MarketplaceRepository {

    private static final String PROVIDER_PROFILE_BY_ID = """
            SELECT pp.id, pp.user_id, u.name AS provider_name, pp.description,
                   pp.is_available, pp.verification_status
            FROM provider_profiles pp
            JOIN users u ON u.id = pp.user_id
            WHERE pp.id = ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public MarketplaceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ProviderProfile> findProviderProfile(long providerId) {
        List<ProviderProfile> profiles = jdbcTemplate.query(
                PROVIDER_PROFILE_BY_ID,
                (resultSet, rowNumber) -> mapProviderProfile(resultSet),
                providerId);
        return profiles.stream().findFirst();
    }

    public List<ProviderServiceListing> findActiveProviderServices(
            long userId,
            Long communityId,
            Long categoryId) {
        StringBuilder sql = new StringBuilder("""
                SELECT ps.id AS provider_service_id, ps.provider_id, u.name AS provider_name,
                       pp.description AS provider_description, pp.is_available,
                       pp.verification_status, ps.community_id,
                       sc.id AS category_id, sc.name AS category_name,
                       sc.description AS category_description, ps.price_from,
                       g.id AS gig_id, g.title AS gig_title,
                       g.description AS gig_description, g.pricing_type,
                       g.price AS gig_price, g.status AS gig_status
                FROM provider_services ps
                JOIN provider_profiles pp ON pp.id = ps.provider_id
                JOIN users u ON u.id = pp.user_id
                JOIN communities c ON c.id = ps.community_id
                JOIN service_categories sc ON sc.id = ps.service_category_id
                LEFT JOIN gigs g
                    ON g.provider_service_id = ps.id AND g.status = 'ACTIVE'
                WHERE ps.is_active = TRUE AND sc.is_active = TRUE
                  AND EXISTS (
                      SELECT 1 FROM community_memberships cm
                      WHERE cm.user_id = ? AND cm.community_id = ps.community_id
                        AND cm.status = 'ACTIVE'
                  )
                """);
        List<Object> parameters = new ArrayList<>();
        parameters.add(userId);
        if (communityId != null) {
            sql.append(" AND ps.community_id = ?");
            parameters.add(communityId);
        }
        if (categoryId != null) {
            sql.append(" AND ps.service_category_id = ?");
            parameters.add(categoryId);
        }
        sql.append(" ORDER BY sc.name, u.name, ps.id, g.id");

        return jdbcTemplate.query(
                sql.toString(),
                (ResultSetExtractor<List<ProviderServiceListing>>) this::mapListings,
                parameters.toArray());
    }

    private List<ProviderServiceListing> mapListings(ResultSet resultSet) throws SQLException {
        Map<Long, ListingBuilder> listings = new LinkedHashMap<>();
        while (resultSet.next()) {
            long serviceId = resultSet.getLong("provider_service_id");
            ListingBuilder listing = listings.get(serviceId);
            if (listing == null) {
                listing = new ListingBuilder(resultSet);
                listings.put(serviceId, listing);
            }
            if (resultSet.getObject("gig_id") != null) {
                listing.addGig(resultSet);
            }
        }
        return listings.values().stream().map(ListingBuilder::build).toList();
    }

    private static ProviderProfile mapProviderProfile(ResultSet resultSet) throws SQLException {
        return new ProviderProfile(
                resultSet.getLong("id"),
                resultSet.getString("provider_name"),
                resultSet.getString("description"),
                resultSet.getBoolean("is_available"),
                resultSet.getString("verification_status"));
    }

    private static final class ListingBuilder {

        private final long id;
        private final long providerId;
        private final String providerName;
        private final String providerDescription;
        private final boolean providerAvailable;
        private final String verificationStatus;
        private final long communityId;
        private final long categoryId;
        private final String categoryName;
        private final String categoryDescription;
        private final java.math.BigDecimal priceFrom;
        private final List<GigSummary> gigs = new ArrayList<>();

        private ListingBuilder(ResultSet resultSet) throws SQLException {
            id = resultSet.getLong("provider_service_id");
            providerId = resultSet.getLong("provider_id");
            providerName = resultSet.getString("provider_name");
            providerDescription = resultSet.getString("provider_description");
            providerAvailable = resultSet.getBoolean("is_available");
            verificationStatus = resultSet.getString("verification_status");
            communityId = resultSet.getLong("community_id");
            categoryId = resultSet.getLong("category_id");
            categoryName = resultSet.getString("category_name");
            categoryDescription = resultSet.getString("category_description");
            priceFrom = resultSet.getBigDecimal("price_from");
        }

        private void addGig(ResultSet resultSet) throws SQLException {
            gigs.add(new GigSummary(
                    resultSet.getLong("gig_id"),
                    resultSet.getString("gig_title"),
                    resultSet.getString("gig_description"),
                    resultSet.getString("pricing_type"),
                    resultSet.getBigDecimal("gig_price"),
                    resultSet.getString("gig_status")));
        }

        private ProviderServiceListing build() {
            return new ProviderServiceListing(
                    id,
                    providerId,
                    providerName,
                    providerDescription,
                    providerAvailable,
                    verificationStatus,
                    communityId,
                    categoryId,
                    categoryName,
                    categoryDescription,
                    priceFrom,
                    List.copyOf(gigs));
        }
    }

}
