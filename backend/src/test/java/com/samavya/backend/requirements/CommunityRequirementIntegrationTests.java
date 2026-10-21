package com.samavya.backend.requirements;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class CommunityRequirementIntegrationTests {

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
    private String memberToken;
    private long ownerId;
    private long communityId;
    private long serviceCategoryId;

    @BeforeEach
    void createCommunityAndMembers() throws Exception {
        ownerId = registerAccount("Requirement owner");
        ownerToken = login(ownerId);
        long memberId = registerAccount("Community member");
        memberToken = login(memberId);
        communityId = createCommunity(ownerToken);
        joinCommunity(memberToken, communityId);
        serviceCategoryId = jdbcTemplate.queryForObject(
                """
                        SELECT id FROM service_categories
                        WHERE name = 'Plumber' AND is_active = TRUE
                        """,
                Long.class);
    }

    @Test
    void createsAndRetrievesRequirementUsingAuthenticatedCreator() throws Exception {
        MvcResult created = createRequirement(
                ownerToken,
                """
                        {
                          "type":"SERVICE",
                          "serviceCategoryId":%d,
                          "title":"  Repair leaking tap  ",
                          "description":"Kitchen sink",
                          "quantity":1,
                          "quantityUnit":"job",
                          "budget":125.50,
                          "neededBy":"2026-11-01T10:00:00Z",
                          "createdBy":999999
                        }
                        """.formatted(serviceCategoryId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("SERVICE"))
                .andExpect(jsonPath("$.title").value("Repair leaking tap"))
                .andExpect(jsonPath("$.createdBy").value(ownerId))
                .andExpect(jsonPath("$.communityId").value(communityId))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.serviceCategoryId").value(serviceCategoryId))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();
        long requirementId = requirementId(created);

        mockMvc.perform(get("/api/requirements/{requirementId}", requirementId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requirementId))
                .andExpect(jsonPath("$.createdBy").value(ownerId))
                .andExpect(jsonPath("$.budget").value(125.5));
    }

    @Test
    void listsRequirementsByCommunityAndOptionalTypeFilter() throws Exception {
        createRequirement(ownerToken, productRequest("Need storage boxes"))
                .andExpect(status().isCreated());
        createRequirement(ownerToken, serviceRequest(serviceCategoryId, "Need electrician"))
                .andExpect(status().isCreated());
        createRequirement(memberToken, productRequest("Need moving boxes"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/communities/{communityId}/requirements", communityId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        mockMvc.perform(get("/api/communities/{communityId}/requirements", communityId)
                        .param("type", "PRODUCT")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].type").value(
                        org.hamcrest.Matchers.everyItem(
                                org.hamcrest.Matchers.is("PRODUCT"))));
    }

    @Test
    void rejectsInvalidValuesAndCategoryCombinations() throws Exception {
        createRequirement(ownerToken, productRequest("Missing service category"))
                .andExpect(status().isCreated());

        createRequirement(ownerToken, """
                {"type":"SERVICE","title":"Missing category"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_request"));
        createRequirement(ownerToken, """
                {"type":"PRODUCT","serviceCategoryId":%d,"title":"Unexpected category"}
                """.formatted(serviceCategoryId))
                .andExpect(status().isBadRequest());
        createRequirement(ownerToken, """
                {"type":"SERVICE","serviceCategoryId":999999999,"title":"Unknown category"}
                """)
                .andExpect(status().isBadRequest());
        createRequirement(ownerToken, """
                {"type":"PRODUCT","title":"Zero quantity","quantity":0}
                """)
                .andExpect(status().isBadRequest());
        createRequirement(ownerToken, """
                {"type":"PRODUCT","title":"Negative budget","budget":-1}
                """)
                .andExpect(status().isBadRequest());
        createRequirement(ownerToken, """
                {"type":"UNSUPPORTED","title":"Bad type"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_request"));
        mockMvc.perform(get("/api/communities/{communityId}/requirements", communityId)
                        .param("type", "TASK")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_request"));
        createRequirement(ownerToken, """
                {"type":"PRODUCT","title":"  "}
                """)
                .andExpect(status().isBadRequest());
    }

    @Test
    void everyCommunityScopedOperationRequiresActiveMembership() throws Exception {
        String outsiderToken = login(registerAccount("Community outsider"));
        MvcResult requirement = createRequirement(ownerToken, productRequest("Private request"))
                .andExpect(status().isCreated())
                .andReturn();
        long requirementId = requirementId(requirement);

        mockMvc.perform(get("/api/communities/{communityId}/requirements", communityId)
                        .header("Authorization", bearer(outsiderToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/communities/{communityId}/requirements", communityId)
                        .header("Authorization", bearer(outsiderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest("No membership")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/requirements/{requirementId}", requirementId)
                        .header("Authorization", bearer(outsiderToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/requirements/{requirementId}/cancel", requirementId)
                        .header("Authorization", bearer(outsiderToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequirementRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/communities/{communityId}/requirements", communityId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));
        mockMvc.perform(post("/api/communities/{communityId}/requirements", communityId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest("Unauthenticated")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void onlyCreatorCanCancelAndOnlyWhileRequirementIsOpen() throws Exception {
        MvcResult created = createRequirement(ownerToken, productRequest("Cancel me"))
                .andExpect(status().isCreated())
                .andReturn();
        long requirementId = requirementId(created);

        mockMvc.perform(post("/api/requirements/{requirementId}/cancel", requirementId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));

        mockMvc.perform(post("/api/requirements/{requirementId}/cancel", requirementId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(post("/api/requirements/{requirementId}/cancel", requirementId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("invalid_requirement_state"));
    }

    @Test
    void cannotCancelRequirementsThatHaveAlreadyMatched() throws Exception {
        MvcResult created = createRequirement(ownerToken, productRequest("Already matched"))
                .andExpect(status().isCreated())
                .andReturn();
        long requirementId = requirementId(created);
        jdbcTemplate.update(
                "UPDATE requirements SET status = 'MATCHED' WHERE id = ?",
                requirementId);

        mockMvc.perform(post("/api/requirements/{requirementId}/cancel", requirementId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("invalid_requirement_state"));
    }

    @Test
    void unknownAndInvalidRequirementIdsReturnConsistentStatuses() throws Exception {
        mockMvc.perform(get("/api/requirements/{requirementId}", 987654321L)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("not_found"));
        mockMvc.perform(post("/api/requirements/{requirementId}/cancel", 987654321L)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/requirements/{requirementId}", 0)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isBadRequest());
    }

    private long registerAccount(String name) throws Exception {
        String email = UUID.randomUUID() + "@example.test";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"correct-horse-battery"}
                                """.formatted(name, email)))
                .andExpect(status().isCreated());
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = ?",
                Long.class,
                email);
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
                        .content("{\"name\":\"Requirement community\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(
                result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private void joinCommunity(String token, long joinedCommunityId) throws Exception {
        mockMvc.perform(post(
                        "/api/communities/{communityId}/memberships", joinedCommunityId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    private org.springframework.test.web.servlet.ResultActions createRequirement(
            String token,
            String body) throws Exception {
        return mockMvc.perform(post(
                        "/api/communities/{communityId}/requirements", communityId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
    }

    private static long requirementId(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(
                result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String productRequest(String title) {
        return """
                {"type":"PRODUCT","title":"%s","quantity":2,"quantityUnit":"items","budget":50}
                """.formatted(title);
    }

    private static String serviceRequest(long categoryId, String title) {
        return """
                {"type":"SERVICE","serviceCategoryId":%d,"title":"%s"}
                """.formatted(categoryId, title);
    }
}
