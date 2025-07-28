package com.hotel.scheduler.service;

import org.springframework.stereotype.Service;
import com.hotel.scheduler.dto.AdminAccessRequest;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;
import java.time.Duration;

@Service
public class AdminAccessService {
    // Store codes with email as key, value is [code, timestamp]
    private final ConcurrentHashMap<String, CodeEntry> codeMap = new ConcurrentHashMap<>();

    private static final long CODE_VALIDITY_SECONDS = 15 * 60; // 15 minutes

    private static class CodeEntry {
        String code;
        Instant timestamp;
        CodeEntry(String code, Instant timestamp) {
            this.code = code;
            this.timestamp = timestamp;
        }
    }
    @Autowired
    private NotificationService notificationService;

    public boolean sendVerificationCode(AdminAccessRequest request) {
        // Generate a simple verification code
        String code = String.valueOf((int)(Math.random() * 900000) + 100000); // 6-digit code
        String subject = "Shiftly Scheduler Admin Access Verification";
        String body = "Hello " + request.getFirstName() + ",\n\n" +
            "Your verification code for admin access is: " + code + "\n\n" +
            "Business Name: " + request.getBusinessName() + "\n" +
            "This code will be valid for only 15 minutes.\n\n" +
            "If you did not request this, please ignore this email.";
        try {
            notificationService.sendEmail(request.getEmail(), subject, body);
            codeMap.put(request.getEmail(), new CodeEntry(code, Instant.now()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Verifies the code for the given email, returns true if valid and not expired
    public boolean verifyCode(String email, String code) {
        CodeEntry entry = codeMap.get(email);
        if (entry == null) return false;
        if (!entry.code.equals(code)) return false;
        if (Duration.between(entry.timestamp, Instant.now()).getSeconds() > CODE_VALIDITY_SECONDS) {
            codeMap.remove(email); // Expired, remove
            return false;
        }
        codeMap.remove(email); // Valid, remove after use
        return true;
    }
}
