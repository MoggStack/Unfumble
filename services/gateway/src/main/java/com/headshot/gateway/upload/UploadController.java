package com.headshot.gateway.upload;

import com.headshot.gateway.job.Job;
import com.headshot.gateway.job.JobService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles photo uploads.
 *
 * POST /api/v1/upload
 *   - Accepts 1..N photos + a useCase string
 *   - Stores each image in MinIO
 *   - Creates a PENDING job and returns its ID
 *
 * Multi-photo ranking (best-of-N via Spring AI) will be wired in a later step
 * once the OpenAI API key is configured.
 */
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/jpeg", "image/png", "image/webp");

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final StorageService storageService;
    private final JobService jobService;

    public UploadController(StorageService storageService, JobService jobService) {
        this.storageService = storageService;
        this.jobService = jobService;
    }

    /**
     * POST /api/v1/upload
     *
     * @param photos  one or more image files (JPEG / PNG / WebP, max 10 MB each)
     * @param useCase target use-case: PASSPORT | LINKEDIN | INSTAGRAM | JOB_APPLICATION
     * @param user    injected from the security context (must be authenticated)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(
            @RequestPart("photos") @NotNull List<MultipartFile> photos,
            @RequestParam("useCase") String useCase,
            @AuthenticationPrincipal UserDetails user
    ) {
        // ── Validate inputs ───────────────────────────────────────────────────
        if (photos == null || photos.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "At least one photo is required"));
        }

        Job.UseCase parsedUseCase;
        try {
            parsedUseCase = Job.UseCase.valueOf(useCase.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid useCase. Must be one of: PASSPORT, LINKEDIN, INSTAGRAM, JOB_APPLICATION"
            ));
        }

        for (MultipartFile file : photos) {
            if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Unsupported file type: " + file.getContentType() +
                                 ". Allowed: jpeg, png, webp"
                ));
            }
            if (file.getSize() > MAX_FILE_SIZE_BYTES) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "File too large. Maximum size is 10 MB"
                ));
            }
        }

        // ── Store photos ──────────────────────────────────────────────────────
        // For now: always pick the first photo. Multi-photo ranking via Spring AI
        // will be added in a future step (see ImageRankingService).
        MultipartFile chosen = photos.get(0);
        String imageKey = storageService.store(chosen);

        // ── Create job ────────────────────────────────────────────────────────
        // TODO: replace hardcoded UUID with real authenticated user ID once
        // JWT/session lookup is wired end-to-end.
        UUID userId = UUID.nameUUIDFromBytes(user.getUsername().getBytes());
        Job job = jobService.createJob(userId, parsedUseCase, imageKey);

        return ResponseEntity.ok(Map.of(
                "jobId", job.getId(),
                "status", job.getStatus().name()
        ));
    }
}
