package com.yourorg.banking.customer.repo;

import com.yourorg.banking.customer.model.KycApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class KycRepository {

    private final JdbcTemplate jdbc;

    public KycRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<KycApplication> findByCustomerId(UUID customerId) {
        String sql = "SELECT id, customer_id, status, provider_ref, rejection_reason, submitted_at, updated_at FROM kyc_applications WHERE customer_id = ? ORDER BY submitted_at DESC LIMIT 1";
        var list = jdbc.query(sql, new Object[]{customerId}, new Mapper());
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public void insert(KycApplication app) {
        String sql = "INSERT INTO kyc_applications (id, customer_id, status, provider_ref, rejection_reason, submitted_at, updated_at) VALUES (?,?,?,?,?,?,?)";
        jdbc.update(sql, app.id(), app.customerId(), app.status(), app.providerRef(), app.rejectionReason(), app.submittedAt(), app.updatedAt());
    }

    public void update(KycApplication app) {
        String sql = "UPDATE kyc_applications SET status=?, provider_ref=?, rejection_reason=?, updated_at=? WHERE id=?";
        jdbc.update(sql, app.status(), app.providerRef(), app.rejectionReason(), app.updatedAt(), app.id());
    }

    static class Mapper implements RowMapper<KycApplication> {
        @Override
        public KycApplication mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new KycApplication(
                    (UUID) rs.getObject("id"),
                    (UUID) rs.getObject("customer_id"),
                    rs.getString("status"),
                    rs.getString("provider_ref"),
                    rs.getString("rejection_reason"),
                    rs.getTimestamp("submitted_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()
            );
        }
    }
}
