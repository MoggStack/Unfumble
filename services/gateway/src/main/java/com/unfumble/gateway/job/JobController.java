package com.unfumble.gateway.job;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for job status polling.
 *
 * The frontend calls GET /api/v1/jobs/{id} repeatedly until status == DONE or FAILED.
 */
@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    /**
     * GET /api/v1/jobs/{id}
     * Returns the current status and output image key (once ready).
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID id) {
        Job job = jobService.getJob(id);
        return ResponseEntity.ok(JobResponse.from(job));
    }

    // ── Response DTO ─────────────────────────────────────────────────────────

    public record JobResponse(
            UUID id,
            String status,
            String useCase,
            String outputImageKey
    ) {
        static JobResponse from(Job job) {
            return new JobResponse(
                    job.getId(),
                    job.getStatus().name(),
                    job.getUseCase().name(),
                    job.getOutputImageKey()
            );
        }
    }
}
