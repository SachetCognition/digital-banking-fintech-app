package com.yourorg.banking.account.repo;

import com.yourorg.banking.account.model.AccountOpeningCase;
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
public class AccountOpeningCaseRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AccountOpeningCaseRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(AccountOpeningCase openingCase) {
        String sql = """
            INSERT INTO account_opening_cases (id, account_id, customer_id, case_type, status, priority, 
                                             assigned_to, kyc_case_id, required_documents, submitted_documents, 
                                             review_notes, approval_notes, rejection_reason, created_at, updated_at, completed_at)
            VALUES (:id, :accountId, :customerId, :caseType, :status, :priority, :assignedTo, :kycCaseId, 
                    :requiredDocuments, :submittedDocuments, :reviewNotes, :approvalNotes, :rejectionReason, 
                    :createdAt, :updatedAt, :completedAt)
            ON CONFLICT (id) DO UPDATE SET
                status = :status,
                priority = :priority,
                assigned_to = :assignedTo,
                kyc_case_id = :kycCaseId,
                required_documents = :requiredDocuments,
                submitted_documents = :submittedDocuments,
                review_notes = :reviewNotes,
                approval_notes = :approvalNotes,
                rejection_reason = :rejectionReason,
                updated_at = :updatedAt,
                completed_at = :completedAt
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", openingCase.id())
                .addValue("accountId", openingCase.accountId())
                .addValue("customerId", openingCase.customerId())
                .addValue("caseType", openingCase.caseType().name())
                .addValue("status", openingCase.status().name())
                .addValue("priority", openingCase.priority().name())
                .addValue("assignedTo", openingCase.assignedTo())
                .addValue("kycCaseId", openingCase.kycCaseId())
                .addValue("requiredDocuments", openingCase.requiredDocuments().toArray(new String[0]))
                .addValue("submittedDocuments", openingCase.submittedDocuments().toArray(new String[0]))
                .addValue("reviewNotes", openingCase.reviewNotes())
                .addValue("approvalNotes", openingCase.approvalNotes())
                .addValue("rejectionReason", openingCase.rejectionReason())
                .addValue("createdAt", openingCase.createdAt())
                .addValue("updatedAt", openingCase.updatedAt())
                .addValue("completedAt", openingCase.completedAt());
        
        jdbcTemplate.update(sql, params);
    }

    public Optional<AccountOpeningCase> findByAccountId(UUID accountId) {
        String sql = "SELECT * FROM account_opening_cases WHERE account_id = :accountId";
        MapSqlParameterSource params = new MapSqlParameterSource("accountId", accountId);
        
        List<AccountOpeningCase> cases = jdbcTemplate.query(sql, params, new AccountOpeningCaseRowMapper());
        return cases.isEmpty() ? Optional.empty() : Optional.of(cases.get(0));
    }

    public List<AccountOpeningCase> findByCustomerId(UUID customerId) {
        String sql = "SELECT * FROM account_opening_cases WHERE customer_id = :customerId ORDER BY created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        
        return jdbcTemplate.query(sql, params, new AccountOpeningCaseRowMapper());
    }

    public List<AccountOpeningCase> findByStatus(AccountOpeningCase.AccountOpeningStatus status) {
        String sql = "SELECT * FROM account_opening_cases WHERE status = :status ORDER BY created_at DESC";
        MapSqlParameterSource params = new MapSqlParameterSource("status", status.name());
        
        return jdbcTemplate.query(sql, params, new AccountOpeningCaseRowMapper());
    }

    private static class AccountOpeningCaseRowMapper implements RowMapper<AccountOpeningCase> {
        @Override
        public AccountOpeningCase mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AccountOpeningCase(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("account_id")),
                    UUID.fromString(rs.getString("customer_id")),
                    AccountOpeningCase.AccountOpeningCaseType.valueOf(rs.getString("case_type")),
                    AccountOpeningCase.AccountOpeningStatus.valueOf(rs.getString("status")),
                    AccountOpeningCase.AccountOpeningPriority.valueOf(rs.getString("priority")),
                    rs.getString("assigned_to") != null ? UUID.fromString(rs.getString("assigned_to")) : null,
                    rs.getString("kyc_case_id") != null ? UUID.fromString(rs.getString("kyc_case_id")) : null,
                    List.of((String[]) rs.getArray("required_documents").getArray()),
                    List.of((String[]) rs.getArray("submitted_documents").getArray()),
                    rs.getString("review_notes"),
                    rs.getString("approval_notes"),
                    rs.getString("rejection_reason"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant(),
                    rs.getTimestamp("completed_at") != null ? rs.getTimestamp("completed_at").toInstant() : null
            );
        }
    }
}

