package com.yourorg.banking.customer.repo;

import com.yourorg.banking.customer.model.UserSession;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserSessionRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserSessionRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(UserSession session) {
        String sql = """
            INSERT INTO user_sessions (id, customer_id, session_token, device_fingerprint, ip_address, user_agent, expires_at, last_activity_at, created_at)
            VALUES (:id, :customerId, :sessionToken, :deviceFingerprint, :ipAddress, :userAgent, :expiresAt, :lastActivityAt, :createdAt)
            ON CONFLICT (id) DO UPDATE SET
                last_activity_at = :lastActivityAt
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", session.id())
                .addValue("customerId", session.customerId())
                .addValue("sessionToken", session.sessionToken())
                .addValue("deviceFingerprint", session.deviceFingerprint())
                .addValue("ipAddress", session.ipAddress() != null ? session.ipAddress().getHostAddress() : null)
                .addValue("userAgent", session.userAgent())
                .addValue("expiresAt", session.expiresAt())
                .addValue("lastActivityAt", session.lastActivityAt())
                .addValue("createdAt", session.createdAt());
        
        jdbcTemplate.update(sql, params);
    }

    public Optional<UserSession> findBySessionToken(String sessionToken) {
        String sql = "SELECT * FROM user_sessions WHERE session_token = :sessionToken";
        MapSqlParameterSource params = new MapSqlParameterSource("sessionToken", sessionToken);
        
        List<UserSession> sessions = jdbcTemplate.query(sql, params, new UserSessionRowMapper());
        return sessions.isEmpty() ? Optional.empty() : Optional.of(sessions.get(0));
    }

    public List<UserSession> findByCustomerId(UUID customerId) {
        String sql = "SELECT * FROM user_sessions WHERE customer_id = :customerId ORDER BY last_activity_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        
        return jdbcTemplate.query(sql, params, new UserSessionRowMapper());
    }

    public void deleteBySessionToken(String sessionToken) {
        String sql = "DELETE FROM user_sessions WHERE session_token = :sessionToken";
        MapSqlParameterSource params = new MapSqlParameterSource("sessionToken", sessionToken);
        jdbcTemplate.update(sql, params);
    }

    public void deleteByCustomerId(UUID customerId) {
        String sql = "DELETE FROM user_sessions WHERE customer_id = :customerId";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        jdbcTemplate.update(sql, params);
    }

    public void deleteExpiredSessions() {
        String sql = "DELETE FROM user_sessions WHERE expires_at < NOW()";
        jdbcTemplate.update(sql, new MapSqlParameterSource());
    }

    public void updateLastActivity(String sessionToken) {
        String sql = "UPDATE user_sessions SET last_activity_at = NOW() WHERE session_token = :sessionToken";
        MapSqlParameterSource params = new MapSqlParameterSource("sessionToken", sessionToken);
        jdbcTemplate.update(sql, params);
    }

    private static class UserSessionRowMapper implements RowMapper<UserSession> {
        @Override
        public UserSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            InetAddress ipAddress = null;
            String ipStr = rs.getString("ip_address");
            if (ipStr != null) {
                try {
                    ipAddress = InetAddress.getByName(ipStr);
                } catch (Exception e) {
                    // Log error but continue
                }
            }
            
            return new UserSession(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("customer_id")),
                    rs.getString("session_token"),
                    rs.getString("device_fingerprint"),
                    ipAddress,
                    rs.getString("user_agent"),
                    rs.getTimestamp("expires_at").toInstant(),
                    rs.getTimestamp("last_activity_at").toInstant(),
                    rs.getTimestamp("created_at").toInstant()
            );
        }
    }
}

