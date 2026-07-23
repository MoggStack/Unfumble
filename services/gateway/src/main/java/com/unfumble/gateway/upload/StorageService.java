package com.unfumble.gateway.upload;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Wraps the MinIO Java SDK to provide simple object-store operations.
 *
 * Every uploaded file is stored under a UUID key so names never collide.
 */
@Service
public class StorageService {

    private final MinioClient minioClient;
    private final String bucket;

    public StorageService(
            @Value("${app.minio.endpoint}") String endpoint,
            @Value("${app.minio.access-key}") String accessKey,
            @Value("${app.minio.secret-key}") String secretKey,
            @Value("${app.minio.bucket}") String bucket
    ) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
    }

    /**
     * Uploads a multipart file to MinIO and returns its object key.
     *
     * @param file the uploaded file from the HTTP request
     * @return the MinIO object key (UUID-based, e.g. "uploads/550e8400-e29b.jpg")
     */
    public String store(MultipartFile file) {
        try {
            String key = "uploads/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return key;
        } catch (Exception e) {
            throw new StorageException("Failed to store file in MinIO", e);
        }
    }

    /** Strips path traversal and keeps only the filename. */
    private String sanitize(String filename) {
        if (filename == null) return "unknown";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    // ── Checked exception wrapper ─────────────────────────────────────────────

    public static class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
