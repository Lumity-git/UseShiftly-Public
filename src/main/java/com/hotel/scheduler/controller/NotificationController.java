package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.Employee;
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
    
    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal Employee currentUser) {
        try {
            // For now, return mock notifications since notification system isn't fully implemented
            List<Map<String, Object>> notifications = List.of(
                Map.of(
                    "id", 1L,
                    "title", "Shift Assignment",
                    "message", "You have been assigned a new shift for tomorrow.",
                    "type", "SHIFT_ASSIGNMENT",
                    "read", false,
                    "timestamp", "2025-07-11T09:00:00"
                ),
                Map.of(
                    "id", 2L,
                    "title", "Shift Trade Request",
                    "message", "Jane Doe wants to pick up your shift on July 15th.",
                    "type", "TRADE_REQUEST",
                    "read", false,
                    "timestamp", "2025-07-11T08:30:00"
                ),
                Map.of(
                    "id", 3L,
                    "title", "Schedule Updated",
                    "message", "Your schedule for next week has been updated.",
                    "type", "SCHEDULE_UPDATE",
                    "read", true,
                    "timestamp", "2025-07-10T16:45:00"
                )
            );
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            // Mock implementation - in real app this would update notification status
            return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal Employee currentUser) {
        try {
            // Mock implementation - in real app this would mark all notifications as read
            return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, @AuthenticationPrincipal Employee currentUser) {
        try {
            // Mock implementation - in real app this would delete the notification
            return ResponseEntity.ok(new MessageResponse("Notification deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/settings")
    public ResponseEntity<?> getNotificationSettings(@AuthenticationPrincipal Employee currentUser) {
        try {
            Map<String, Object> settings = Map.of(
                "emailNotifications", true,
                "pushNotifications", true,
                "shiftReminders", true,
                "tradeNotifications", true,
                "scheduleUpdates", true
            );
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/settings")
    public ResponseEntity<?> updateNotificationSettings(@RequestBody Map<String, Object> settings,
                                                        @AuthenticationPrincipal Employee currentUser) {
        try {
            // Mock implementation - in real app this would save user notification preferences
            return ResponseEntity.ok(new MessageResponse("Notification settings updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
    
    // Helper class for response messages
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
