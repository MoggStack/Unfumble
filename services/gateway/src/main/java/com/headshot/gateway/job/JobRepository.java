package com.headshot.gateway.job;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    /** Returns all jobs belonging to a user, most recent first. */
    List<Job> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
