package com.yourorg.banking.customer.repo;

import com.yourorg.banking.customer.model.EmailVerificationToken;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EmailVerificationTokenRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EmailVerificationTokenRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(EmailVerificationToken token) {
        String sql = """
            INSERT INTO email_verification_tokens (id, customer_id, token, type, expires_at, used_at, created_at)
            VALUES (:id, :customerId, :token, :type, :expiresAt, :usedAt, :createdAt)
            ON CONFLICT (id) DO UPDATE SET
                used_at = :usedAt
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", token.id())
                .addValue("customerId", token.customerId())
                .addValue("token", token.token())
                .addValue("type", token.type().name())
                .addValue("expiresAt", token.expiresAt())
                .addValue("usedAt", token.usedAt())
                .addValue("createdAt", token.createdAt());
        
        jdbcTemplate.update(sql, params);
    }

    public Optional<EmailVerificationToken> findByToken(String token) {
        String sql = "SELECT * FROM email_verification_tokens WHERE token = :token";
        MapSqlParameterSource params = new MapSqlParameterSource("token", token);
        
        List<EmailVerificationToken> tokens = jdbcTemplate.query(sql, params, new EmailVerificationTokenRowMapper());
        return tokens.isEmpty() ? Optional.empty() : Optional.of(tokens.get(0));
    }

    public List<EmailVerificationToken> findByCustomerIdAndType(UUID customerId, EmailVerificationToken.TokenType type) {
        String sql = "SELECT * FROM email_verification_tokens WHERE customer_id = :customerId AND type = :type ORDER BY created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("type", type.name());
        
        return jdbcTemplate.query(sql, params, new EmailVerificationTokenRowMapper());
    }

    public void deleteExpiredTokens() {
        String sql = "DELETE FROM email_verification_tokens WHERE expires_at < NOW()";
        jdbcTemplate.update(sql, new MapSqlParameterSource());
    }

    private static class EmailVerificationTokenRowMapper implements RowMapper<EmailVerificationToken> {
        @Override
        public EmailVerificationToken mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new EmailVerificationToken(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("customer_id")),
                    rs.getString("token"),
                    EmailVerificationToken.TokenType.valueOf(rs.getString("type")),
                    rs.getTimestamp("expires_at").toInstant(),
                    rs.getTimestamp("used_at") != null ? rs.getTimestamp("used_at").toInstant() : null,
                    rs.getTimestamp("created_at").toInstant()
            );
        }
    }
}

