package com.yourorg.banking.account.repo;

import com.yourorg.banking.account.model.JointAccountHolder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class JointAccountHolderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JointAccountHolderRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(JointAccountHolder holder) {
        String sql = """
            INSERT INTO joint_account_holders (id, account_id, customer_id, role, is_primary, added_at, 
                                             approved_at, approved_by, status, created_at, updated_at)
            VALUES (:id, :accountId, :customerId, :role, :isPrimary, :addedAt, :approvedAt, :approvedBy, 
                    :status, :createdAt, :updatedAt)
            ON CONFLICT (id) DO UPDATE SET
                role = :role,
                is_primary = :isPrimary,
                approved_at = :approvedAt,
                approved_by = :approvedBy,
                status = :status,
                updated_at = :updatedAt
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", holder.id())
                .addValue("accountId", holder.accountId())
                .addValue("customerId", holder.customerId())
                .addValue("role", holder.role().name())
                .addValue("isPrimary", holder.isPrimary())
                .addValue("addedAt", holder.addedAt())
                .addValue("approvedAt", holder.approvedAt())
                .addValue("approvedBy", holder.approvedBy())
                .addValue("status", holder.status())
                .addValue("createdAt", holder.createdAt())
                .addValue("updatedAt", holder.updatedAt());
        
        jdbcTemplate.update(sql, params);
    }

    public List<JointAccountHolder> findByAccountId(UUID accountId) {
        String sql = "SELECT * FROM joint_account_holders WHERE account_id = :accountId ORDER BY added_at ASC";
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", accountId);
        
        return jdbcTemplate.query(sql, params, new JointAccountHolderRowMapper());
    }

    public List<JointAccountHolder> findByCustomerId(UUID customerId) {
        String sql = "SELECT * FROM joint_account_holders WHERE customer_id = :customerId ORDER BY added_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        
        return jdbcTemplate.query(sql, params, new JointAccountHolderRowMapper());
    }

    public void deleteByAccountId(UUID accountId) {
        String sql = "DELETE FROM joint_account_holders WHERE account_id = :accountId";
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", accountId);
        jdbcTemplate.update(sql, params);
    }

    private static class JointAccountHolderRowMapper implements RowMapper<JointAccountHolder> {
        @Override
        public JointAccountHolder mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new JointAccountHolder(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("account_id")),
                    UUID.fromString(rs.getString("customer_id")),
                    JointAccountHolder.JointAccountRole.valueOf(rs.getString("role")),
                    rs.getBoolean("is_primary"),
                    rs.getTimestamp("added_at").toInstant(),
                    rs.getTimestamp("approved_at") != null ? rs.getTimestamp("approved_at").toInstant() : null,
                    rs.getString("approved_by") != null ? UUID.fromString(rs.getString("approved_by")) : null,
                    rs.getString("status"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()
            );
        }
    }
}

