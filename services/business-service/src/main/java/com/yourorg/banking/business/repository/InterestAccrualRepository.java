package com.yourorg.banking.business.repository;

import com.yourorg.banking.business.model.InterestAccrual;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class InterestAccrualRepository {

    private final JdbcTemplate jdbcTemplate;

    public InterestAccrualRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(InterestAccrual accrual) {
        jdbcTemplate.update(
            "INSERT INTO interest_accruals (id, account_id, amount, rate, posting_date) VALUES (?, ?, ?, ?, ?)",
            accrual.getId(), accrual.getAccountId(), accrual.getAmount(), accrual.getRate(), accrual.getPostingDate()
        );
    }
}
