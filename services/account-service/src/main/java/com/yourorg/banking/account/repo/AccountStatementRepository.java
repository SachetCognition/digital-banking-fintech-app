package com.yourorg.banking.account.repo;

import com.yourorg.banking.account.model.AccountStatement;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AccountStatementRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AccountStatementRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(AccountStatement statement) {
        String sql = """
            INSERT INTO account_statements (id, account_id, statement_id, statement_date, period_start, period_end,
                                          opening_balance, closing_balance, total_debits, total_credits, currency,
                                          generated_at, generated_by, file_path, email_sent, email_sent_at, created_at)
            VALUES (:id, :accountId, :statementId, :statementDate, :periodStart, :periodEnd, :openingBalance,
                    :closingBalance, :totalDebits, :totalCredits, :currency, :generatedAt, :generatedBy,
                    :filePath, :emailSent, :emailSentAt, :createdAt)
            ON CONFLICT (id) DO UPDATE SET
                file_path = :filePath,
                email_sent = :emailSent,
                email_sent_at = :emailSentAt
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", statement.id())
                .addValue("accountId", statement.accountId())
                .addValue("statementId", statement.statementId())
                .addValue("statementDate", statement.statementDate())
                .addValue("periodStart", statement.periodStart())
                .addValue("periodEnd", statement.periodEnd())
                .addValue("openingBalance", statement.openingBalance())
                .addValue("closingBalance", statement.closingBalance())
                .addValue("totalDebits", statement.totalDebits())
                .addValue("totalCredits", statement.totalCredits())
                .addValue("currency", statement.currency())
                .addValue("generatedAt", statement.generatedAt())
                .addValue("generatedBy", statement.generatedBy())
                .addValue("filePath", statement.filePath())
                .addValue("emailSent", statement.emailSent())
                .addValue("emailSentAt", statement.emailSentAt())
                .addValue("createdAt", statement.createdAt());
        
        jdbcTemplate.update(sql, params);
    }

    public Optional<AccountStatement> findById(UUID statementId) {
        String sql = "SELECT * FROM account_statements WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", statementId);
        
        List<AccountStatement> statements = jdbcTemplate.query(sql, params, new AccountStatementRowMapper());
        return statements.isEmpty() ? Optional.empty() : Optional.of(statements.get(0));
    }

    public Optional<AccountStatement> findByStatementId(String statementId) {
        String sql = "SELECT * FROM account_statements WHERE statement_id = :statementId";
        MapSqlParameterSource params = new MapSqlParameterSource("statementId", statementId);
        
        List<AccountStatement> statements = jdbcTemplate.query(sql, params, new AccountStatementRowMapper());
        return statements.isEmpty() ? Optional.empty() : Optional.of(statements.get(0));
    }

    public List<AccountStatement> findByAccountId(UUID accountId, int limit) {
        String sql = """
            SELECT * FROM account_statements 
            WHERE account_id = :accountId 
            ORDER BY statement_date DESC 
            LIMIT :limit
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountId", accountId)
                .addValue("limit", limit);
        
        return jdbcTemplate.query(sql, params, new AccountStatementRowMapper());
    }

    public List<AccountStatement> findByAccountIdAndPeriod(UUID accountId, LocalDate fromDate, LocalDate toDate) {
        String sql = """
            SELECT * FROM account_statements 
            WHERE account_id = :accountId 
            AND statement_date >= :fromDate 
            AND statement_date <= :toDate
            ORDER BY statement_date DESC
            """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("accountId", accountId)
                .addValue("fromDate", fromDate)
                .addValue("toDate", toDate);
        
        return jdbcTemplate.query(sql, params, new AccountStatementRowMapper());
    }

    private static class AccountStatementRowMapper implements RowMapper<AccountStatement> {
        @Override
        public AccountStatement mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AccountStatement(
                    UUID.fromString(rs.getString("id")),
                    UUID.fromString(rs.getString("account_id")),
                    rs.getString("statement_id"),
                    rs.getDate("statement_date").toLocalDate(),
                    rs.getDate("period_start").toLocalDate(),
                    rs.getDate("period_end").toLocalDate(),
                    rs.getBigDecimal("opening_balance"),
                    rs.getBigDecimal("closing_balance"),
                    rs.getBigDecimal("total_debits"),
                    rs.getBigDecimal("total_credits"),
                    List.of(), // transactions - would need separate query
                    rs.getString("currency"),
                    rs.getTimestamp("generated_at").toInstant(),
                    rs.getString("generated_by") != null ? UUID.fromString(rs.getString("generated_by")) : null,
                    rs.getString("file_path"),
                    rs.getBoolean("email_sent"),
                    rs.getTimestamp("email_sent_at") != null ? rs.getTimestamp("email_sent_at").toInstant() : null,
                    rs.getTimestamp("created_at").toInstant()
            );
        }
    }
}

