package com.useshiftly.scheduler.controller;

import com.useshiftly.scheduler.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for waitlist operations.
 */
@RestController
@RequestMapping("/api/public/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    /**
     * Add email to waitlist.
     * Endpoint: POST /api/public/waitlist/join
     * Body: { "email": "user@example.com" }
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinWaitlist(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Email is required"));
        }
        
        // Basic email validation
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Please provide a valid email address"));
        }
        
        try {
            waitlistService.addToWaitlist(email.trim().toLowerCase());
            return ResponseEntity.ok(new MessageResponse("Successfully joined the waitlist! We'll notify you when Phase 2 is ready."));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity.ok(new MessageResponse("You're already on the waitlist! We'll notify you when Phase 2 is ready."));
            }
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Failed to join waitlist. Please try again."));
        }
    }

    /**
     * Get waitlist count (public endpoint for showing interest).
     */
    @GetMapping("/count")
    public ResponseEntity<?> getWaitlistCount() {
        long count = waitlistService.getWaitlistCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Simple message response class for API responses.
     */
    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
