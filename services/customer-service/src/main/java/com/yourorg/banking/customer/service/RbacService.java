package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.Permission;
import com.yourorg.banking.customer.model.Role;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RbacService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public RbacService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Role> getCustomerRoles(UUID customerId) {
        String sql = """
            SELECT r.id, r.name, r.description, r.created_at, r.updated_at
            FROM roles r
            JOIN customer_roles cr ON r.id = cr.role_id
            WHERE cr.customer_id = :customerId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        return jdbcTemplate.query(sql, params, new RoleRowMapper());
    }

    public List<Permission> getCustomerPermissions(UUID customerId) {
        String sql = """
            SELECT DISTINCT p.id, p.name, p.resource, p.action, p.created_at
            FROM permissions p
            JOIN role_permissions rp ON p.id = rp.permission_id
            JOIN customer_roles cr ON rp.role_id = cr.role_id
            WHERE cr.customer_id = :customerId
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource("customerId", customerId);
        return jdbcTemplate.query(sql, params, new PermissionRowMapper());
    }

    public boolean hasPermission(UUID customerId, String resource, String action) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM permissions p
            JOIN role_permissions rp ON p.id = rp.permission_id
            JOIN customer_roles cr ON rp.role_id = cr.role_id
            WHERE cr.customer_id = :customerId
            AND p.resource = :resource
            AND p.action = :action
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("resource", resource)
                .addValue("action", action);
        
        return jdbcTemplate.queryForObject(sql, params, Boolean.class);
    }

    public boolean hasRole(UUID customerId, String roleName) {
        String sql = """
            SELECT COUNT(*) > 0
            FROM roles r
            JOIN customer_roles cr ON r.id = cr.role_id
            WHERE cr.customer_id = :customerId
            AND r.name = :roleName
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("roleName", roleName);
        
        return jdbcTemplate.queryForObject(sql, params, Boolean.class);
    }

    public void assignRole(UUID customerId, UUID roleId, UUID assignedBy) {
        String sql = """
            INSERT INTO customer_roles (customer_id, role_id, assigned_at, assigned_by)
            VALUES (:customerId, :roleId, NOW(), :assignedBy)
            ON CONFLICT (customer_id, role_id) DO NOTHING
            """;
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("roleId", roleId)
                .addValue("assignedBy", assignedBy);
        
        jdbcTemplate.update(sql, params);
    }

    public void removeRole(UUID customerId, UUID roleId) {
        String sql = "DELETE FROM customer_roles WHERE customer_id = :customerId AND role_id = :roleId";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("customerId", customerId)
                .addValue("roleId", roleId);
        
        jdbcTemplate.update(sql, params);
    }

    private static class RoleRowMapper implements RowMapper<Role> {
        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Role(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()
            );
        }
    }

    private static class PermissionRowMapper implements RowMapper<Permission> {
        @Override
        public Permission mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Permission(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("name"),
                    rs.getString("resource"),
                    rs.getString("action"),
                    rs.getTimestamp("created_at").toInstant()
            );
        }
    }
}

