package com.headshot.gateway.job;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a single image-processing job from upload through AI generation.
 *
 * Status lifecycle:  PENDING → PROCESSING → DONE
 *                                         ↘ FAILED
 */
@Entity
@Table(name = "jobs")
public class Job {

    // ── Status Enum ──────────────────────────────────────────────────────────

    public enum Status { PENDING, PROCESSING, DONE, FAILED }

    // ── Use-Case Enum ────────────────────────────────────────────────────────

    public enum UseCase { PASSPORT, LINKEDIN, INSTAGRAM, JOB_APPLICATION }

    // ── Fields ───────────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** The user who submitted this job. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UseCase useCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    /** MinIO object key for the chosen input image. */
    @Column(name = "input_image_key")
    private String inputImageKey;

    /** MinIO object key for the AI-generated output image — null until DONE. */
    @Column(name = "output_image_key")
    private String outputImageKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // ── Constructors ─────────────────────────────────────────────────────────

    protected Job() {}

    public Job(UUID userId, UseCase useCase, String inputImageKey) {
        this.userId = userId;
        this.useCase = useCase;
        this.inputImageKey = inputImageKey;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public UUID getId() { return id; }

    public UUID getUserId() { return userId; }

    public UseCase getUseCase() { return useCase; }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    public String getInputImageKey() { return inputImageKey; }

    public String getOutputImageKey() { return outputImageKey; }

    public void setOutputImageKey(String outputImageKey) { this.outputImageKey = outputImageKey; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}
