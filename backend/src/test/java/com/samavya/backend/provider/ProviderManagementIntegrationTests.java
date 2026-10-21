package com.samavya.backend.provider;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = "security.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ProviderManagementIntegrationTests {

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
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String ownerToken;
    private String otherToken;
    private long ownerId;
    private long communityId;
    private long categoryId;
    private long providerId;
    private long offeringId;

    @BeforeEach
    void createMemberUsersAndCommunity() throws Exception {
        ownerId = registerUser("Provider owner");
        ownerToken = login(ownerId);
        long otherUserId = registerUser("Other user");
        otherToken = login(otherUserId);
        communityId = createCommunity(ownerToken);
        joinCommunity(otherToken, communityId);
        categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM service_categories WHERE name = 'Plumber' AND is_active = TRUE",
                Long.class);
        providerId = createProfile(ownerToken);
        offeringId = createOffering(
                ownerToken,
                """
                        {"communityId":%d,"categoryId":%d,"priceFrom":35.50}
                        """.formatted(communityId, categoryId));
    }

    @Test
    void createsAndRetrievesPendingProfileAndAllowsOnlyPermittedUpdates() throws Exception {
        mockMvc.perform(get("/api/provider-profile/me")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(providerId))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist());

        mockMvc.perform(post("/api/provider-profile/me")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Ignored duplicate\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(providerId))
                .andExpect(jsonPath("$.description").value("Provider profile"));

        mockMvc.perform(put("/api/provider-profile/me")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Updated bio","available":false,
                                 "verificationStatus":"VERIFIED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated bio"))
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));

        mockMvc.perform(put("/api/provider-profile/me")
                        .header("Authorization", bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Attack\",\"available\":true}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createsListsUpdatesAndDeactivatesOwnedOfferings() throws Exception {
        mockMvc.perform(get("/api/provider-services/me")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].priceFrom").value(35.5))
                .andExpect(jsonPath("$[0].active").value(true));

        mockMvc.perform(put("/api/provider-services/{id}", offeringId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priceFrom\":42.00,\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priceFrom").value(42.0))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(put("/api/provider-services/{id}", offeringId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priceFrom\":42.00,\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(put("/api/provider-services/{id}", offeringId)
                        .header("Authorization", bearer(otherToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"priceFrom\":42.00,\"active\":false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validatesMembershipCategoryAndDuplicateOfferingConstraints() throws Exception {
        mockMvc.perform(post("/api/provider-services")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"communityId":%d,"categoryId":%d}
                                """.formatted(communityId, categoryId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("duplicate_provider_offering"));

        mockMvc.perform(post("/api/provider-services")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"communityId":%d,"categoryId":999999999}
                                """.formatted(communityId)))
                .andExpect(status().isBadRequest());

        long outsiderId = registerUser("Outsider");
        String outsiderToken = login(outsiderId);
        mockMvc.perform(post("/api/provider-services")
                        .header("Authorization", bearer(outsiderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"communityId":%d,"categoryId":%d}
                                """.formatted(communityId, categoryId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/provider-services")
                        .header("Authorization", bearer(outsiderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"communityId":%d,"categoryId":%d,"priceFrom":-2}
                                """.formatted(communityId, categoryId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsGigsAndSupportsOwnedLifecycleWithDiscoveryVisibility() throws Exception {
        long gigId = createGig(
                ownerToken,
                offeringId,
                """
                        {"title":"Tap repair","description":"Repairs","pricingType":"FIXED","price":75}
                        """);

        mockMvc.perform(get("/api/service-discovery")
                        .param("communityId", Long.toString(communityId))
                        .param("categoryId", Long.toString(categoryId))
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(offeringId))
                .andExpect(jsonPath("$[0].gigs.length()").value(1))
                .andExpect(jsonPath("$[0].gigs[0].id").value(gigId))
                .andExpect(jsonPath("$[0].gigs[0].price").value(75));

        mockMvc.perform(post("/api/gigs/{id}/pause", gigId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAUSED"));
        mockMvc.perform(post("/api/gigs/{id}/pause", gigId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("invalid_gig_state"));

        mockMvc.perform(post("/api/gigs/{id}/resume", gigId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(put("/api/gigs/{id}", gigId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated tap repair","pricingType":"HOURLY","price":20}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pricingType").value("HOURLY"));

        mockMvc.perform(post("/api/gigs/{id}/close", gigId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
        mockMvc.perform(post("/api/gigs/{id}/resume", gigId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict());
        mockMvc.perform(put("/api/gigs/{id}", gigId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Cannot edit","pricingType":"FIXED","price":10}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidGigPricingAndPreventsCrossOwnerAccess() throws Exception {
        mockMvc.perform(post("/api/provider-services/{id}/gigs", offeringId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"No price","pricingType":"FIXED"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/provider-services/{id}/gigs", offeringId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Negative","pricingType":"HOURLY","price":-1}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/provider-services/{id}/gigs", offeringId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Quote","pricingType":"QUOTE","price":5}
                                """))
                .andExpect(status().isBadRequest());

        long gigId = createGig(ownerToken, offeringId,
                """
                        {"title":"Owner gig","pricingType":"QUOTE"}
                        """);
        mockMvc.perform(get("/api/gigs/{id}", gigId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/gigs/{id}/close", gigId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void offeringWritesAndGigCreationRequireMembershipAndProfile() throws Exception {
        long outsiderId = registerUser("Nonmember provider");
        String outsiderToken = login(outsiderId);
        createProfile(outsiderToken);
        mockMvc.perform(post("/api/provider-services")
                        .header("Authorization", bearer(outsiderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"communityId":%d,"categoryId":%d}
                                """.formatted(communityId, categoryId)))
                .andExpect(status().isForbidden());

        long noProfileId = registerUser("No profile");
        String noProfileToken = login(noProfileId);
        mockMvc.perform(post("/api/provider-services")
                        .header("Authorization", bearer(noProfileToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"communityId":%d,"categoryId":%d}
                                """.formatted(communityId, categoryId)))
                .andExpect(status().isConflict());
    }

    @Test
    void unauthenticatedProviderManagementIsRejected() throws Exception {
        mockMvc.perform(get("/api/provider-profile/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/provider-services"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/gigs/me"))
                .andExpect(status().isUnauthorized());
    }

    private long registerUser(String name) throws Exception {
        String email = UUID.randomUUID() + "@example.test";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"correct-horse-battery"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated());
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }

    private String login(long userId) throws Exception {
        String email = jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE id = ?",
                String.class,
                userId);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"correct-horse-battery"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private long createCommunity(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/communities")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Provider community\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id"))
                .longValue();
    }

    private void joinCommunity(String token, long id) throws Exception {
        mockMvc.perform(post("/api/communities/{id}/memberships", id)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    private long createProfile(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/provider-profile/me")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Provider profile\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id"))
                .longValue();
    }

    private long createOffering(String token, String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/provider-services")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id"))
                .longValue();
    }

    private long createGig(String token, long serviceId, String body) throws Exception {
        MvcResult result = mockMvc.perform(post(
                        "/api/provider-services/{id}/gigs", serviceId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id"))
                .longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
