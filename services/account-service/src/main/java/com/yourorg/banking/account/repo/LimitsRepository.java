package com.digitalbank.fintech.account.repo;

import com.digitalbank.fintech.account.model.AccountLimit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LimitsRepository {

    private final JdbcTemplate jdbcTemplate;

    public LimitsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AccountLimit> findByAccountId(UUID accountId) {
        String sql = "SELECT id, account_id, limit_type, daily_amount, per_txn_amount FROM account_limits WHERE account_id = ?";
        var list = jdbcTemplate.query(sql, new Object[]{accountId}, new AccountLimitRowMapper());
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public AccountLimit upsert(UUID accountId, String limitType, BigDecimal daily, BigDecimal perTxn) {
        var existing = findByAccountId(accountId);
        if (existing.isPresent()) {
            String upd = "UPDATE account_limits SET limit_type=?, daily_amount=?, per_txn_amount=? WHERE account_id=?";
            jdbcTemplate.update(upd, limitType, daily, perTxn, accountId);
            return new AccountLimit(existing.get().id(), accountId, limitType, daily, perTxn);
        } else {
            UUID id = UUID.randomUUID();
            String ins = "INSERT INTO account_limits (id, account_id, limit_type, daily_amount, per_txn_amount) VALUES (?,?,?,?,?)";
            jdbcTemplate.update(ins, id, accountId, limitType, daily, perTxn);
            return new AccountLimit(id, accountId, limitType, daily, perTxn);
        }
    }

    static class AccountLimitRowMapper implements RowMapper<AccountLimit> {
        @Override
        public AccountLimit mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AccountLimit(
                    (UUID) rs.getObject("id"),
                    (UUID) rs.getObject("account_id"),
                    rs.getString("limit_type"),
                    rs.getBigDecimal("daily_amount"),
                    rs.getBigDecimal("per_txn_amount")
            );
        }
    }
}
