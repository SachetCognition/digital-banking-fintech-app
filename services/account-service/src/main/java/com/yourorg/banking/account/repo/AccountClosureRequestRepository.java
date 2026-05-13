package com.yourorg.banking.account.repo;

import com.yourorg.banking.account.model.AccountClosureRequest;
import com.yourorg.banking.account.model.AccountClosureStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountClosureRequestRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AccountClosureRequestRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(AccountClosureRequest request) {
        String sql = """
            INSERT INTO account_closure_requests (id, account_id, requested_by, closure_reason, transfer_account_id,
                                                comments, status, approved_by, approved_at, rejection_reason,
                                                final_balance, transfer_amount, final_statement_generated,
                                                final_statement_sent, created_at, updated_at, completed_at)
            VALUES (:id, :accountId, :requestedBy, :closureReason, :transferAccountId, :comments, :status,
                    :approvedBy, :approvedAt, :rejectionReason, :finalBalance, :transferAmount,
                    :finalStatementGenerated, :finalStatementSent, :createdAt, :updatedAt, :completedAt)
            ON CONFLICT (id) DO UPDATE SET
                status = :status,
                approved_by = :approvedBy,
                approved_at = :approvedAt,
                rejection_reason = :rejectionReason,
                final_balance = :finalBalance,
                transfer_amount = :transferAmount,
                final_statement_generated = :finalStatementGenerated,
                final_statement_sent = :finalStatementSent,
                updated_at = :updatedAt,
                completed_at = :completedAt
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", request.id())
                .addValue("accountId", request.accountId())
                .addValue("requestedBy", request.requestedBy())
                .addValue("closureReason", request.closureReason())
                .addValue("transferAccountId", request.transferAccountId())
                .addValue("comments", request.comments())
                .addValue("status", request.status().name())
                .addValue("approvedBy", request.approvedBy())
                .addValue("approvedAt", request.approvedAt())
                .addValue("rejectionReason", request.rejectionReason())
                .addValue("finalBalance", request.finalBalance())
                .addValue("transferAmount", request.transferAmount())
                .addValue("finalStatementGenerated", request.finalStatementGenerated())
                .addValue("finalStatementSent", request.finalStatementSent())
                .addValue("createdAt", request.createdAt())
                .addValue("updatedAt", request.updatedAt())
                .addValue("completedAt", request.completedAt());
        
        jdbcTemplate.update(sql, params);
    }

    public Optional<AccountClosureRequest> findById(UUID requestId) {
        String sql = "SELECT * FROM account_closure_requests WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", requestId);
        
        List<AccountClosureRequest> requests = jdbcTemplate.query(sql, params, new AccountClosureRequestRowMapper());
        return requests.isEmpty() ? Optional.empty() : Optional.of(requests.get(0));
    }

    public List<AccountClosureRequest> findByRequestedBy(UUID customerId) {
        String sql = "SELECT * FROM account_closure_requests WHERE requested_by = :customerId ORDER BY created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        
        return jdbcTemplate.query(sql, params, new AccountClosureRequestRowMapper());
    }

    public List<AccountClosureRequest> findByStatus(AccountClosureStatus status) {
        String sql = "SELECT * FROM account_closure_requests WHERE status = :status ORDER BY created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("status", status.name());
        
        return jdbcTemplate.query(sql, params, new AccountClosureRequestRowMapper());
    }

    public List<AccountClosureRequest> findByAccountId(UUID accountId) {
        String sql = "SELECT * FROM account_closure_requests WHERE account_id = :accountId ORDER BY created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", accountId);
        
        return jdbcTemplate.query(sql, params, new AccountClosureRequestRowMapper());
    }

    private static class AccountClosureRequestRowMapper implements RowMapper<AccountClosureRequest> {
        @Override
        public AccountClosureRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AccountClosureRequest(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("account_id")),
                    UUID.fromString(rs.getString("requested_by")),
                    rs.getString("closure_reason"),
                    rs.getString("transfer_account_id") != null ? UUID.fromString(rs.getString("transfer_account_id")) : null,
                    rs.getString("comments"),
                    AccountClosureStatus.valueOf(rs.getString("status")),
                    rs.getString("approved_by") != null ? UUID.fromString(rs.getString("approved_by")) : null,
                    rs.getTimestamp("approved_at") != null ? rs.getTimestamp("approved_at").toInstant() : null,
                    rs.getString("rejection_reason"),
                    rs.getBigDecimal("final_balance"),
                    rs.getBigDecimal("transfer_amount"),
                    rs.getBoolean("final_statement_generated"),
                    rs.getBoolean("final_statement_sent"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant(),
                    rs.getTimestamp("completed_at") != null ? rs.getTimestamp("completed_at").toInstant() : null
            );
        }
    }
}

