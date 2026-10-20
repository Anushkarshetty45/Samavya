package com.samavya.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
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
class AuthenticationCommunityIntegrationTests {

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

    @Test
    void registrationLoginAndCurrentIdentityNeverExposePasswordHash() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password_hash").doesNotExist());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login(email, "correct-horse-battery")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn();
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void duplicateEmailIsCaseInsensitiveAndInvalidCredentialsAreGeneric() throws Exception {
        String email = uniqueEmail();
        register(email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration(email.toUpperCase(java.util.Locale.ROOT))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("email_already_exists"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login(email, "not-the-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
    }

    @Test
    void rejectsInvalidRegistrationAndOverlongUtf8PasswordsWithoutServerError()
            throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_request"));

        String oversizedUtf8Password = "😀".repeat(19);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration(uniqueEmail(), oversizedUtf8Password)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login(uniqueEmail(), oversizedUtf8Password)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedRoutesRequireValidBearerIdentityButCategoriesRemainPublic()
            throws Exception {
        mockMvc.perform(get("/api/communities"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthorized"));

        mockMvc.perform(get("/api/communities")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/service-categories"))
                .andExpect(status().isOk());
    }

    @Test
    void communityCreationJoinAndMembershipReadsAreBoundToAuthenticatedUser()
            throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail());
        String otherToken = registerAndLogin(uniqueEmail());

        mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest());

        MvcResult created = mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Neighborhood","description":"Local group","address":"Main St"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        long communityId = ((Number) JsonPath.read(
                created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(post("/api/communities/{id}/memberships", 999999999999L)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/communities/me/memberships")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("MEMBER"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        mockMvc.perform(get("/api/communities/me/memberships")
                        .header("Authorization", "Bearer " + otherToken)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/communities/{id}", communityId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(communityId));

        for (int attempt = 0; attempt < 2; attempt++) {
            mockMvc.perform(post("/api/communities/{id}/memberships", communityId)
                            .header("Authorization", "Bearer " + otherToken)
                            .param("userId", "1")
                            .param("role", "PROVIDER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("MEMBER"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        mockMvc.perform(get("/api/communities/me/memberships")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("MEMBER"));
    }

    @Test
    void communityScopedDiscoveryRequiresActiveMembership() throws Exception {
        String ownerToken = registerAndLogin(uniqueEmail());
        String visitorToken = registerAndLogin(uniqueEmail());
        MvcResult created = mockMvc.perform(post("/api/communities")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Private discovery scope\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        long communityId = ((Number) JsonPath.read(
                created.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/service-discovery")
                        .header("Authorization", "Bearer " + visitorToken)
                        .param("communityId", Long.toString(communityId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));

        mockMvc.perform(post("/api/communities/{id}/memberships", communityId)
                        .header("Authorization", "Bearer " + visitorToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/service-discovery")
                        .header("Authorization", "Bearer " + visitorToken)
                        .param("communityId", Long.toString(communityId)))
                .andExpect(status().isOk());
    }

    @Test
    void suspendedUsersCannotLogInOrUsePreviouslyIssuedTokens() throws Exception {
        String email = uniqueEmail();
        register(email);
        String token = loginAndGetToken(email, "correct-horse-battery");
        jdbcTemplate.update(
                "UPDATE users SET account_status = 'SUSPENDED' WHERE email = ?",
                email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login(email, "correct-horse-battery")))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/communities")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void legacyAccountsWithoutPasswordHashesCannotUseKnownPlaceholderPasswords()
            throws Exception {
        String email = uniqueEmail();
        jdbcTemplate.update(
                "INSERT INTO users (name, email, password_hash) VALUES (?, ?, NULL)",
                "Legacy account",
                email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login(email, "password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration(email)))
                .andExpect(status().isCreated());
    }

    private String registerAndLogin(String email) throws Exception {
        register(email);
        return loginAndGetToken(email, "correct-horse-battery");
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private static String uniqueEmail() {
        return UUID.randomUUID() + "@example.test";
    }

    private static String registration(String email) {
        return registration(email, "correct-horse-battery");
    }

    private static String registration(String email, String password) {
        return """
                {"name":"Samavya User","email":"%s","password":"%s"}
                """.formatted(email, password);
    }

    private static String login(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }
}
