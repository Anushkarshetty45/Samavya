package com.samavya.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ServiceCategoryIntegrationTests {

    private static final String INACTIVE_CATEGORY_NAME =
            "inactive-test-" + UUID.randomUUID();

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

    private long inactiveCategoryId;

    @BeforeEach
    void insertInactiveCategory() {
        inactiveCategoryId = jdbcTemplate.queryForObject(
                "INSERT INTO service_categories (name, is_active) "
                        + "VALUES (?, FALSE) RETURNING id",
                Long.class,
                INACTIVE_CATEGORY_NAME);
    }

    @Test
    void listsActiveCategoriesFromTheSeededDatabaseOnly() throws Exception {
        String response = mockMvc.perform(get("/api/service-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].name").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("Plumber").doesNotContain(INACTIVE_CATEGORY_NAME);
    }

    @Test
    void retrievesAnActiveCategoryById() throws Exception {
        long categoryId = jdbcTemplate.queryForObject(
                "SELECT id FROM service_categories WHERE name = 'Plumber'",
                Long.class);

        mockMvc.perform(get("/api/service-categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Plumber"))
                .andExpect(jsonPath("$.description").isNotEmpty());
    }

    @Test
    void returnsNotFoundForInactiveAndUnknownCategories() throws Exception {
        mockMvc.perform(get("/api/service-categories/{id}", inactiveCategoryId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/service-categories/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }
}
