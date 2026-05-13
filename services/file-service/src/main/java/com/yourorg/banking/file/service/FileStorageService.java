package com.yourorg.banking.file.service;

import com.yourorg.banking.file.model.FileMetadata;
import com.yourorg.banking.file.repo.FileMetadataRepository;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FileStorageService {

    private final MinioClient minioClient;
    private final FileMetadataRepository repository;

    @Value("${minio.bucket}")
    private String defaultBucket;

    public FileStorageService(MinioClient minioClient, FileMetadataRepository repository) {
        this.minioClient = minioClient;
        this.repository = repository;
    }

    public FileMetadata upload(UUID customerId, MultipartFile file) throws Exception {
        ensureBucketExists(defaultBucket);

        UUID fileId = UUID.randomUUID();
        String storageKey = customerId + "/" + fileId + "/" + file.getOriginalFilename();

        String hash = computeSha256(file.getInputStream());

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(defaultBucket)
                .object(storageKey)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        FileMetadata metadata = new FileMetadata();
        metadata.setId(fileId);
        metadata.setCustomerId(customerId);
        metadata.setFileName(file.getOriginalFilename());
        metadata.setContentType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setFileHash(hash);
        metadata.setStorageKey(storageKey);
        metadata.setBucketName(defaultBucket);
        metadata.setUploadStatus("COMPLETED");

        repository.save(metadata);
        return metadata;
    }

    public Optional<FileMetadata> getMetadata(UUID fileId) {
        return repository.findById(fileId);
    }

    public String getDownloadUrl(UUID fileId) throws Exception {
        Optional<FileMetadata> opt = repository.findById(fileId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("File not found: " + fileId);
        }
        FileMetadata metadata = opt.get();
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(metadata.getBucketName())
                .object(metadata.getStorageKey())
                .method(Method.GET)
                .expiry(1, TimeUnit.HOURS)
                .build());
    }

    public void delete(UUID fileId) throws Exception {
        Optional<FileMetadata> opt = repository.findById(fileId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("File not found: " + fileId);
        }
        repository.updateStatus(fileId, "DELETED");
    }

    private void ensureBucketExists(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    private String computeSha256(InputStream input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
