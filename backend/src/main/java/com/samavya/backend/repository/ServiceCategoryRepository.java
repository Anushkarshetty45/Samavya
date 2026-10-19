package com.samavya.backend.repository;

import com.samavya.backend.ServiceCategory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ServiceCategoryRepository {

    private static final String FIND_ACTIVE =
            "SELECT id, name, description FROM service_categories "
                    + "WHERE is_active = TRUE ORDER BY name, id";
    private static final String FIND_ACTIVE_BY_ID =
            "SELECT id, name, description FROM service_categories "
                    + "WHERE is_active = TRUE AND id = ?";

    private static final RowMapper<ServiceCategory> ROW_MAPPER =
            (resultSet, rowNumber) -> mapCategory(resultSet);

    private final JdbcTemplate jdbcTemplate;

    public ServiceCategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ServiceCategory> findAllActive() {
        return jdbcTemplate.query(FIND_ACTIVE, ROW_MAPPER);
    }

    public Optional<ServiceCategory> findActiveById(long id) {
        List<ServiceCategory> categories = jdbcTemplate.query(
                FIND_ACTIVE_BY_ID,
                preparedStatement -> preparedStatement.setLong(1, id),
                ROW_MAPPER);
        return categories.stream().findFirst();
    }

    private static ServiceCategory mapCategory(ResultSet resultSet) throws SQLException {
        return new ServiceCategory(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"));
    }
}
