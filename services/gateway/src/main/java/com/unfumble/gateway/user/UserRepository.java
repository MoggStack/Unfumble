package com.unfumble.gateway.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Used by auth to look up a user by their login email. */
    Optional<User> findByEmail(String email);

    /** Existence check to prevent duplicate registrations. */
    boolean existsByEmail(String email);
}
