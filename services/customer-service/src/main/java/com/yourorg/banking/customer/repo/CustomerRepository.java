package com.yourorg.banking.customer.repo;

import com.yourorg.banking.customer.model.Customer;
import com.yourorg.banking.customer.model.CustomerStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Repository
public class CustomerRepository {

    private final JdbcTemplate jdbcTemplate;

    public CustomerRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String generateUserNo() {
        // Simple human-readable user number for MVP: CUST-XXXXXX
        int n = new Random().nextInt(900000) + 100000;
        return "CUST-" + n;
    }

    public void save(Customer c) {
        String sql = """
            INSERT INTO customers (id, user_no, email, phone, full_name, dob, country, status, password_hash, 
                                 email_verified, phone_verified, mfa_enabled, last_login_at, failed_login_attempts, 
                                 locked_until, created_at, updated_at) 
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT (id) DO UPDATE SET
                user_no = EXCLUDED.user_no,
                email = EXCLUDED.email,
                phone = EXCLUDED.phone,
                full_name = EXCLUDED.full_name,
                dob = EXCLUDED.dob,
                country = EXCLUDED.country,
                status = EXCLUDED.status,
                password_hash = EXCLUDED.password_hash,
                email_verified = EXCLUDED.email_verified,
                phone_verified = EXCLUDED.phone_verified,
                mfa_enabled = EXCLUDED.mfa_enabled,
                last_login_at = EXCLUDED.last_login_at,
                failed_login_attempts = EXCLUDED.failed_login_attempts,
                locked_until = EXCLUDED.locked_until,
                updated_at = EXCLUDED.updated_at
            """;
        
        jdbcTemplate.update(sql,
                c.id(),
                c.userNo(),
                c.email(),
                c.phone(),
                c.fullName(),
                c.dob(),
                c.country(),
                c.status().name(),
                c.passwordHash(),
                c.emailVerified(),
                c.phoneVerified(),
                c.mfaEnabled(),
                c.lastLoginAt(),
                c.failedLoginAttempts(),
                c.lockedUntil(),
                c.createdAt(),
                c.updatedAt()
        );
    }

    public Optional<Customer> findById(UUID id) {
        String sql = """
            SELECT id, user_no, email, phone, full_name, dob, country, status, password_hash, 
                   email_verified, phone_verified, mfa_enabled, last_login_at, failed_login_attempts, 
                   locked_until, created_at, updated_at 
            FROM customers WHERE id = ?
            """;
        var list = jdbcTemplate.query(sql, new Object[]{id}, new CustomerRowMapper());
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public Optional<Customer> findByEmail(String email) {
        String sql = """
            SELECT id, user_no, email, phone, full_name, dob, country, status, password_hash, 
                   email_verified, phone_verified, mfa_enabled, last_login_at, failed_login_attempts, 
                   locked_until, created_at, updated_at 
            FROM customers WHERE email = ?
            """;
        var list = jdbcTemplate.query(sql, new Object[]{email}, new CustomerRowMapper());
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    public Optional<Customer> findByPhone(String phone) {
        String sql = """
            SELECT id, user_no, email, phone, full_name, dob, country, status, password_hash, 
                   email_verified, phone_verified, mfa_enabled, last_login_at, failed_login_attempts, 
                   locked_until, created_at, updated_at 
            FROM customers WHERE phone = ?
            """;
        var list = jdbcTemplate.query(sql, new Object[]{phone}, new CustomerRowMapper());
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    static class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Customer(
                    (UUID) rs.getObject("id"),
                    rs.getString("user_no"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("full_name"),
                    rs.getDate("dob") != null ? rs.getDate("dob").toLocalDate() : null,
                    rs.getString("country"),
                    CustomerStatus.valueOf(rs.getString("status")),
                    rs.getString("password_hash"),
                    rs.getBoolean("email_verified"),
                    rs.getBoolean("phone_verified"),
                    rs.getBoolean("mfa_enabled"),
                    rs.getTimestamp("last_login_at") != null ? rs.getTimestamp("last_login_at").toInstant() : null,
                    rs.getInt("failed_login_attempts"),
                    rs.getTimestamp("locked_until") != null ? rs.getTimestamp("locked_until").toInstant() : null,
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()
            );
        }
    }
}
