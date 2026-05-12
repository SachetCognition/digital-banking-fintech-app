package com.yourorg.banking.file.repo;

import com.yourorg.banking.file.model.FileMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileMetadataRepository {

    private final JdbcTemplate jdbcTemplate;

    public FileMetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FileMetadata> rowMapper = (ResultSet rs, int rowNum) -> {
        FileMetadata fm = new FileMetadata();
        fm.setId(UUID.fromString(rs.getString("id")));
        fm.setCustomerId(UUID.fromString(rs.getString("customer_id")));
        fm.setFileName(rs.getString("file_name"));
        fm.setContentType(rs.getString("content_type"));
        fm.setFileSize(rs.getLong("file_size"));
        fm.setFileHash(rs.getString("file_hash"));
        fm.setStorageKey(rs.getString("storage_key"));
        fm.setBucketName(rs.getString("bucket_name"));
        fm.setUploadStatus(rs.getString("upload_status"));
        fm.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        fm.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return fm;
    };

    public void save(FileMetadata metadata) {
        jdbcTemplate.update(
            "INSERT INTO file_metadata (id, customer_id, file_name, content_type, file_size, file_hash, storage_key, bucket_name, upload_status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())",
            metadata.getId(), metadata.getCustomerId(), metadata.getFileName(),
            metadata.getContentType(), metadata.getFileSize(), metadata.getFileHash(),
            metadata.getStorageKey(), metadata.getBucketName(), metadata.getUploadStatus()
        );
    }

    public Optional<FileMetadata> findById(UUID id) {
        List<FileMetadata> results = jdbcTemplate.query(
            "SELECT * FROM file_metadata WHERE id = ?", rowMapper, id
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<FileMetadata> findByCustomerId(UUID customerId) {
        return jdbcTemplate.query(
            "SELECT * FROM file_metadata WHERE customer_id = ?", rowMapper, customerId
        );
    }

    public void updateStatus(UUID id, String status) {
        jdbcTemplate.update(
            "UPDATE file_metadata SET upload_status = ?, updated_at = NOW() WHERE id = ?",
            status, id
        );
    }
}
