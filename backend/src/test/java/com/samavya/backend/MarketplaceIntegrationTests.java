package com.samavya.backend;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class MarketplaceIntegrationTests {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    DockerImageName.parse("postgis/postgis:17-3.5")
                            .asCompatibleSubstituteFor("postgres"));

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    private long firstCommunityId;
    private long secondCommunityId;
    private long plumberCategoryId;
    private long electricianCategoryId;
    private long firstProviderId;
    private long firstProviderUserId;
    private long firstOfferingId;
    private long noGigOfferingId;

    @BeforeEach
    void insertMarketplaceFixtures() {
        String suffix = UUID.randomUUID().toString();
        firstCommunityId = insertCommunity("Community A " + suffix);
        secondCommunityId = insertCommunity("Community B " + suffix);
        plumberCategoryId = insertCategory("Plumber " + suffix);
        electricianCategoryId = insertCategory("Electrician " + suffix);

        firstProviderId = insertProvider("Provider A " + suffix, true, "VERIFIED");
        long secondProviderId = insertProvider("Provider B " + suffix, false, "PENDING");
        firstProviderUserId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM provider_profiles WHERE id = ?",
                Long.class,
                firstProviderId);
        long secondProviderUserId = jdbcTemplate.queryForObject(
                "SELECT user_id FROM provider_profiles WHERE id = ?",
                Long.class,
                secondProviderId);
        addMembership(firstProviderUserId, firstCommunityId);
        addMembership(firstProviderUserId, secondCommunityId);
        addMembership(secondProviderUserId, secondCommunityId);

        firstOfferingId = insertOffering(
                firstProviderId, firstCommunityId, plumberCategoryId, "100.00", true);
        noGigOfferingId = insertOffering(
                secondProviderId, secondCommunityId, plumberCategoryId, null, true);
        insertOffering(firstProviderId, firstCommunityId, electricianCategoryId, "50.00", false);

        insertGig(firstOfferingId, "Fixed job", "FIXED", "125.00", "ACTIVE");
        insertGig(firstOfferingId, "Quote job", "QUOTE", null, "ACTIVE");
        insertGig(firstOfferingId, "Hourly job", "HOURLY", "25.00", "ACTIVE");
        insertGig(firstOfferingId, "Paused job", "HOURLY", "25.00", "PAUSED");
    }

    @Test
    void discoversRealActiveOfferingsAndIncludesOnlyActiveGigs() throws Exception {
        mockMvc.perform(get("/api/service-discovery").with(firstProviderAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(firstOfferingId))
                .andExpect(jsonPath("$[0].categoryName").value(
                        org.hamcrest.Matchers.startsWith("Plumber ")))
                .andExpect(jsonPath("$[0].priceFrom").value(100.0))
                .andExpect(jsonPath("$[0].gigs.length()").value(3))
                .andExpect(jsonPath("$[0].gigs[0].pricingType").value("FIXED"))
                .andExpect(jsonPath("$[0].gigs[0].price").value(125.0))
                .andExpect(jsonPath("$[0].gigs[1].pricingType").value("QUOTE"))
                .andExpect(jsonPath("$[0].gigs[1].price").doesNotExist())
                .andExpect(jsonPath("$[0].gigs[2].pricingType").value("HOURLY"))
                .andExpect(jsonPath("$[1].id").value(noGigOfferingId))
                .andExpect(jsonPath("$[1].gigs.length()").value(0));
    }

    @Test
    void appliesCommunityAndCategoryFiltersTogether() throws Exception {
        mockMvc.perform(get("/api/service-discovery")
                        .param("communityId", Long.toString(secondCommunityId))
                        .param("categoryId", Long.toString(plumberCategoryId))
                        .with(firstProviderAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(noGigOfferingId))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void filtersByCommunityWithoutACombinedCategoryFilter() throws Exception {
        mockMvc.perform(get("/api/service-discovery")
                        .param("communityId", Long.toString(secondCommunityId))
                        .with(firstProviderAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(noGigOfferingId));
    }

    @Test
    void filtersByCategoryWithoutACombinedCommunityFilter() throws Exception {
        mockMvc.perform(get("/api/service-discovery")
                        .param("categoryId", Long.toString(plumberCategoryId))
                        .with(firstProviderAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void retrievesProviderProfileWithoutReturningPrivateUserFields() throws Exception {
        mockMvc.perform(get("/api/provider-profiles/{id}", firstProviderId)
                        .with(firstProviderAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.providerName").value(org.hamcrest.Matchers.startsWith(
                        "Provider A ")))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void categoryWithoutAnActiveProviderOfferingIsNotAServiceDiscoveryResult()
            throws Exception {
        mockMvc.perform(get("/api/service-discovery")
                        .param("categoryId", Long.toString(electricianCategoryId))
                        .with(firstProviderAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void existingUniqueConstraintRejectsDuplicateProviderOfferings() {
        assertThatThrownBy(() -> insertOffering(
                firstProviderId,
                firstCommunityId,
                plumberCategoryId,
                "100.00",
                true))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existingGigPriceCheckRejectsNegativePrices() {
        assertThatThrownBy(() -> insertGig(
                firstOfferingId,
                "Invalid price",
                "FIXED",
                "-1.00",
                "ACTIVE"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void existingGigStatusCheckRejectsUnknownStatuses() {
        assertThatThrownBy(() -> insertGig(
                firstOfferingId,
                "Invalid status",
                "FIXED",
                "10.00",
                "PUBLISHED"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private long insertCommunity(String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO communities (name) VALUES (?) RETURNING id",
                Long.class,
                name);
    }

    private void addMembership(long userId, long communityId) {
        jdbcTemplate.update(
                """
                        INSERT INTO community_memberships (user_id, community_id, role, status)
                        VALUES (?, ?, 'MEMBER', 'ACTIVE')
                        """,
                userId,
                communityId);
    }

    private RequestPostProcessor firstProviderAuthentication() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.tokenValue("test-token")
                        .subject(Long.toString(firstProviderUserId)));
    }

    private long insertCategory(String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO service_categories (name) VALUES (?) RETURNING id",
                Long.class,
                name);
    }

    private long insertProvider(String name, boolean available, String verificationStatus) {
        long userId = jdbcTemplate.queryForObject(
                "INSERT INTO users (name, email) VALUES (?, ?) RETURNING id",
                Long.class,
                name,
                UUID.randomUUID() + "@example.test");
        return jdbcTemplate.queryForObject(
                "INSERT INTO provider_profiles (user_id, description, is_available, "
                        + "verification_status) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class,
                userId,
                "Provider description",
                available,
                verificationStatus);
    }

    private long insertOffering(
            long providerId,
            long communityId,
            long categoryId,
            String priceFrom,
            boolean active) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO provider_services (provider_id, community_id, "
                        + "service_category_id, price_from, is_active) "
                        + "VALUES (?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                providerId,
                communityId,
                categoryId,
                priceFrom == null ? null : new BigDecimal(priceFrom),
                active);
    }

    private void insertGig(
            long providerServiceId,
            String title,
            String pricingType,
            String price,
            String status) {
        jdbcTemplate.update(
                "INSERT INTO gigs (provider_service_id, title, pricing_type, price, status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                providerServiceId,
                title,
                pricingType,
                price == null ? null : new BigDecimal(price),
                status);
    }
}
