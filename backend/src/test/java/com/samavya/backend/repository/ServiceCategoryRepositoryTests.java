package com.samavya.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.samavya.backend.ServiceCategory;
import java.sql.PreparedStatement;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

class ServiceCategoryRepositoryTests {

    private JdbcTemplate jdbcTemplate;
    private ServiceCategoryRepository repository;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        repository = new ServiceCategoryRepository(jdbcTemplate);
    }

    @Test
    void listsOnlyActiveCategoriesInStableOrder() {
        List<ServiceCategory> categories = List.of(
                new ServiceCategory(1, "Electrician", "Electrical repairs"),
                new ServiceCategory(2, "Plumber", "Pipes and leaks"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class))).thenReturn(categories);

        assertThat(repository.findAllActive()).isEqualTo(categories);

        ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(query.capture(), any(RowMapper.class));
        assertThat(query.getValue())
                .contains("FROM service_categories")
                .contains("WHERE is_active = TRUE")
                .contains("ORDER BY name, id");
    }

    @Test
    void retrievesAnActiveCategoryWithAParameterizedId() throws Exception {
        ServiceCategory category = new ServiceCategory(42, "Plumber", "Pipes and leaks");
        when(jdbcTemplate.query(
                anyString(),
                any(PreparedStatementSetter.class),
                any(RowMapper.class)))
                .thenReturn(List.of(category));

        assertThat(repository.findActiveById(42)).contains(category);

        ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PreparedStatementSetter> setter =
                ArgumentCaptor.forClass(PreparedStatementSetter.class);
        verify(jdbcTemplate).query(
                query.capture(),
                setter.capture(),
                any(RowMapper.class));
        assertThat(query.getValue())
                .contains("WHERE is_active = TRUE AND id = ?");

        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        setter.getValue().setValues(preparedStatement);
        verify(preparedStatement).setLong(1, 42);
    }

    @Test
    void propagatesDatabaseFailures() {
        DataAccessResourceFailureException failure =
                new DataAccessResourceFailureException("Database unavailable");
        when(jdbcTemplate.query(
                eq("SELECT id, name, description FROM service_categories "
                        + "WHERE is_active = TRUE ORDER BY name, id"),
                any(RowMapper.class)))
                .thenThrow(failure);

        assertThatThrownBy(repository::findAllActive)
                .isSameAs(failure);
    }
}
