package com.hotel.scheduler.service;

import com.hotel.scheduler.model.Waitlist;
import com.hotel.scheduler.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing waitlist entries.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;

    /**
     * Add an email to the waitlist.
     * @param email the email to add
     * @return the saved Waitlist entry
     * @throws RuntimeException if email already exists
     */
    public Waitlist addToWaitlist(String email) {
        if (waitlistRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists in waitlist");
        }
        
        Waitlist waitlistEntry = new Waitlist();
        waitlistEntry.setEmail(email);
        return waitlistRepository.save(waitlistEntry);
    }

    /**
     * Check if email exists in waitlist.
     */
    public boolean isEmailInWaitlist(String email) {
        return waitlistRepository.existsByEmail(email);
    }

    /**
     * Get all waitlist entries (for admin purposes).
     */
    @Transactional(readOnly = true)
    public List<Waitlist> getAllWaitlistEntries() {
        return waitlistRepository.findAll();
    }

    /**
     * Get total count of waitlist entries.
     */
    @Transactional(readOnly = true)
    public long getWaitlistCount() {
        return waitlistRepository.count();
    }
}
