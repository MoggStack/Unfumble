package com.unfumble.gateway.job;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Business logic for creating and managing image-processing jobs.
 */
@Service
public class JobService {

    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    /** Creates a new job in PENDING state. */
    @Transactional
    public Job createJob(UUID userId, Job.UseCase useCase, String inputImageKey) {
        return jobRepository.save(new Job(userId, useCase, inputImageKey));
    }

    /** Returns a job by ID — throws if not found. */
    @Transactional(readOnly = true)
    public Job getJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException("Job not found: " + jobId));
    }

    /** Returns all jobs for a given user. */
    @Transactional(readOnly = true)
    public List<Job> getJobsForUser(UUID userId) {
        return jobRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Transitions the job to a new status (called by the Python AI engine callback or polling). */
    @Transactional
    public Job updateStatus(UUID jobId, Job.Status newStatus) {
        Job job = getJob(jobId);
        job.setStatus(newStatus);
        return jobRepository.save(job);
    }

    /** Marks the job as DONE and records the output image key. */
    @Transactional
    public Job markDone(UUID jobId, String outputImageKey) {
        Job job = getJob(jobId);
        job.setOutputImageKey(outputImageKey);
        job.setStatus(Job.Status.DONE);
        return jobRepository.save(job);
    }
}
