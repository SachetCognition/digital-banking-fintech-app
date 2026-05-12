package com.yourorg.banking.file.api;

import com.yourorg.banking.file.model.FileMetadata;
import com.yourorg.banking.file.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("customerId") UUID customerId) {
        try {
            FileMetadata metadata = fileStorageService.upload(customerId, file);
            return ResponseEntity.ok(Map.of(
                "fileId", metadata.getId(),
                "storageKey", metadata.getStorageKey(),
                "status", metadata.getUploadStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<?> getMetadata(@PathVariable UUID fileId) {
        return fileStorageService.getMetadata(fileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<?> download(@PathVariable UUID fileId) {
        try {
            String url = fileStorageService.getDownloadUrl(fileId);
            return ResponseEntity.ok(Map.of("downloadUrl", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> delete(@PathVariable UUID fileId) {
        try {
            fileStorageService.delete(fileId);
            return ResponseEntity.ok(Map.of("status", "DELETED"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
