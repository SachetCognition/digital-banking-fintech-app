package com.yourorg.banking.payments.repository;

import com.yourorg.banking.payments.model.Dispute;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DisputeRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Dispute> ROW_MAPPER = (rs, rowNum) -> {
        Dispute d = new Dispute();
        d.setId(UUID.fromString(rs.getString("id")));
        d.setTransactionId(UUID.fromString(rs.getString("transaction_id")));
        d.setCustomerId(UUID.fromString(rs.getString("customer_id")));
        d.setReason(rs.getString("reason"));
        d.setStatus(Dispute.DisputeStatus.valueOf(rs.getString("status")));
        d.setAmount(rs.getBigDecimal("amount"));
        d.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        d.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return d;
    };

    public DisputeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Dispute save(Dispute dispute) {
        jdbcTemplate.update(
            "INSERT INTO disputes (id, transaction_id, customer_id, reason, status, amount, created_at, updated_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status, updated_at = EXCLUDED.updated_at",
            dispute.getId(), dispute.getTransactionId(), dispute.getCustomerId(),
            dispute.getReason(), dispute.getStatus().name(), dispute.getAmount(),
            java.sql.Timestamp.from(dispute.getCreatedAt()), java.sql.Timestamp.from(dispute.getUpdatedAt())
        );
        return dispute;
    }

    public Optional<Dispute> findById(UUID id) {
        List<Dispute> results = jdbcTemplate.query("SELECT * FROM disputes WHERE id = ?", ROW_MAPPER, id);
        return results.stream().findFirst();
    }

    public List<Dispute> findByCustomerId(UUID customerId) {
        return jdbcTemplate.query("SELECT * FROM disputes WHERE customer_id = ? ORDER BY created_at DESC", ROW_MAPPER, customerId);
    }
}
