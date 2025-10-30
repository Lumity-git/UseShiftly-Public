package com.useshiftly.scheduler.repository;

import com.useshiftly.scheduler.model.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    
    /**
     * Check if an email already exists in the waitlist.
     */
    boolean existsByEmail(String email);
    
    /**
     * Find waitlist entry by email.
     */
    Optional<Waitlist> findByEmail(String email);
}
