package com.yourorg.banking.customer.repo;

import com.yourorg.banking.customer.model.MfaSecret;
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
public class MfaSecretRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public MfaSecretRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(MfaSecret secret) {
        String sql = """
            INSERT INTO mfa_secrets (id, customer_id, secret_key, backup_codes, created_at, updated_at)
            VALUES (:id, :customerId, :secretKey, :backupCodes, :createdAt, :updatedAt)
            ON CONFLICT (id) DO UPDATE SET
                secret_key = :secretKey,
                backup_codes = :backupCodes,
                updated_at = :updatedAt
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", secret.id())
                .addValue("customerId", secret.customerId())
                .addValue("secretKey", secret.secretKey())
                .addValue("backupCodes", secret.backupCodes().toArray(new String[0]))
                .addValue("createdAt", secret.createdAt())
                .addValue("updatedAt", secret.updatedAt());
        
        jdbcTemplate.update(sql, params);
    }

    public Optional<MfaSecret> findByCustomerId(UUID customerId) {
        String sql = "SELECT * FROM mfa_secrets WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        
        List<MfaSecret> secrets = jdbcTemplate.query(sql, params, new MfaSecretRowMapper());
        return secrets.isEmpty() ? Optional.empty() : Optional.of(secrets.get(0));
    }

    public void deleteByCustomerId(UUID customerId) {
        String sql = "DELETE FROM mfa_secrets WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        jdbcTemplate.update(sql, params);
    }

    private static class MfaSecretRowMapper implements RowMapper<MfaSecret> {
        @Override
        public MfaSecret mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new MfaSecret(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("customer_id")),
                    rs.getString("secret_key"),
                    List.of((String[]) rs.getArray("backup_codes").getArray()),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()
            );
        }
    }
}

