package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {
    /**
     * NotificationController: Handles notification-related REST API endpoints for hotel employees.
     *
     * Usage:
     * - Use this controller to fetch, mark as read, delete, and manage notification settings for employees.
     * - All endpoints require authentication; current user is injected via @AuthenticationPrincipal.
     * - Most methods are currently mock implementations; replace with real notification logic as needed.
     *
     * Key Endpoints:
     * - GET /api/notifications: Get all notifications for current user
     * - POST /api/notifications/{id}/read: Mark a notification as read
     * - POST /api/notifications/read-all: Mark all notifications as read
     * - DELETE /api/notifications/{id}: Delete a notification
     * - GET /api/notifications/settings: Get notification settings for current user
     * - POST /api/notifications/settings: Update notification settings for current user
     *
     * Dependencies:
     * - Employee: Used for user context
     * - MessageResponse: Helper class for response messages
     */
    
    // Inject your NotificationService (assumed to exist)
    private final NotificationService notificationService;

    /**
     * Returns all notifications for the current user.
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal Employee currentUser) {
        try {
            // Fetch notifications from the service/database for the current user
            List<?> notifications = notificationService.getNotificationsForUser(currentUser.getId());
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Marks a specific notification as read for the current user.
     * POST /api/notifications/{id}/read
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            notificationService.markNotificationAsRead(currentUser.getId(), id);
            return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Marks all notifications as read for the current user.
     * POST /api/notifications/read-all
     */
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal Employee currentUser) {
        try {
            notificationService.markAllNotificationsAsRead(currentUser.getId());
            return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Deletes a specific notification for the current user.
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            notificationService.deleteNotification(currentUser.getId(), id);
            return ResponseEntity.ok(new MessageResponse("Notification deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Returns notification settings for the current user.
     * GET /api/notifications/settings
     */
    @GetMapping("/settings")
    public ResponseEntity<?> getNotificationSettings(@AuthenticationPrincipal Employee currentUser) {
        try {
            Map<String, Object> settings = notificationService.getNotificationSettings(currentUser.getId());
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Updates notification settings for the current user.
     * POST /api/notifications/settings
     */
    @PostMapping("/settings")
    public ResponseEntity<?> updateNotificationSettings(@RequestBody Map<String, Object> settings,
                                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            notificationService.updateNotificationSettings(currentUser.getId(), settings);
            return ResponseEntity.ok(new MessageResponse("Notification settings updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Helper class for response messages (used for error/success responses).
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
