package com.samavya.backend.repository;

import com.samavya.backend.UserAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final String invalidPasswordHash;

    public UserRepository(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.invalidPasswordHash = passwordEncoder.encode(
                java.util.UUID.randomUUID().toString());
    }

    public Optional<UserAccount> findByEmail(String email) {
        List<UserAccount> users = jdbcTemplate.query(
                """
                        SELECT id, name, email FROM users
                        WHERE lower(email) = lower(?)
                        """,
                (resultSet, rowNumber) -> new UserAccount(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email")),
                email);
        return users.stream().findFirst();
    }

    public Optional<UserAccount> findAccountById(long userId) {
        List<UserAccount> users = jdbcTemplate.query(
                "SELECT id, name, email FROM users WHERE id = ?",
                (resultSet, rowNumber) -> new UserAccount(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email")),
                userId);
        return users.stream().findFirst();
    }

    public UserAccount create(String name, String email, String passwordHash) {
        return jdbcTemplate.queryForObject(
                """
                        INSERT INTO users (name, email, password_hash)
                        VALUES (?, ?, ?)
                        RETURNING id, name, email
                        """,
                (resultSet, rowNumber) -> new UserAccount(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email")),
                name,
                email,
                passwordHash);
    }

    public UserDetails loadActiveUserByEmail(String email) {
        List<UserDetails> users = jdbcTemplate.query(
                """
                        SELECT id, password_hash FROM users
                        WHERE lower(email) = lower(?) AND account_status = 'ACTIVE'
                        """,
                (resultSet, rowNumber) -> User.withUsername(
                                Long.toString(resultSet.getLong("id")))
                        .password(resultSet.getString("password_hash") == null
                                ? invalidPasswordHash
                                : resultSet.getString("password_hash"))
                        .authorities("ROLE_USER")
                        .build(),
                email);
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Invalid email or password");
        }
        return users.getFirst();
    }

    public boolean isActive(long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ? AND account_status = 'ACTIVE'",
                Integer.class,
                userId);
        return count != null && count == 1;
    }
}
