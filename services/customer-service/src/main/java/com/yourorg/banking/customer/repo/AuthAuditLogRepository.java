package com.yourorg.banking.customer.repo;

import com.yourorg.banking.customer.model.AuthAuditLog;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class AuthAuditLogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AuthAuditLogRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(AuthAuditLog log) {
        String sql = """
            INSERT INTO auth_audit_log (id, customer_id, event_type, event_data, ip_address, user_agent, success, created_at)
            VALUES (:id, :customerId, :eventType, :eventData, :ipAddress, :userAgent, :success, :createdAt)
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", log.id())
                .addValue("customerId", log.customerId())
                .addValue("eventType", log.eventType())
                .addValue("eventData", log.eventData())
                .addValue("ipAddress", log.ipAddress() != null ? log.ipAddress().getHostAddress() : null)
                .addValue("userAgent", log.userAgent())
                .addValue("success", log.success())
                .addValue("createdAt", log.createdAt());
        
        jdbcTemplate.update(sql, params);
    }

    public List<AuthAuditLog> findByCustomerId(UUID customerId, int limit) {
        String sql = """
            SELECT * FROM auth_audit_log 
            WHERE customer_id = :customerId 
            ORDER BY created_at DESC 
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("limit", limit);
        
        return jdbcTemplate.query(sql, params, new AuthAuditLogRowMapper());
    }

    public List<AuthAuditLog> findByEventType(String eventType, int limit) {
        String sql = """
            SELECT * FROM auth_audit_log 
            WHERE event_type = :eventType 
            ORDER BY created_at DESC 
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("eventType", eventType)
                .addValue("limit", limit);
        
        return jdbcTemplate.query(sql, params, new AuthAuditLogRowMapper());
    }

    private static class AuthAuditLogRowMapper implements RowMapper<AuthAuditLog> {
        @Override
        public AuthAuditLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            InetAddress ipAddress = null;
            String ipStr = rs.getString("ip_address");
            if (ipStr != null) {
                try {
                    ipAddress = InetAddress.getByName(ipStr);
                } catch (Exception e) {
                    // Log error but continue
                }
            }
            
            return new AuthAuditLog(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("customer_id") != null ? UUID.fromString(rs.getString("customer_id")) : null,
                    rs.getString("event_type"),
                    rs.getString("event_data"),
                    ipAddress,
                    rs.getString("user_agent"),
                    rs.getBoolean("success"),
                    rs.getTimestamp("created_at").toInstant()
            );
        }
    }
}

