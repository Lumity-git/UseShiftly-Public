package com.hotel.scheduler.service;

import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.model.Shift;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for sending notifications to employees and managers about shift assignments, trades, and updates.
 * <p>
 * Handles both email notifications (via {@link org.springframework.mail.javamail.JavaMailSender}) and in-app notifications (future implementation).
 * <ul>
 *   <li>Sends notifications for shift trades, assignments, updates, cancellations, and pickups.</li>
 *   <li>Notifies managers/admins for trade approvals.</li>
 *   <li>Supports async email sending and logs notification events.</li>
 * </ul>
 * <b>Usage:</b> Injected into controllers and services to trigger notifications for scheduling events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    /**
     * Sends a general email to the specified recipient.
     * @param to recipient email address
     * @param subject email subject
     * @param body email body
     */
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("General email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send general email to {}: {}", to, e.getMessage());
        }
    }
    /**
     * Notifies the requesting employee that their shift trade offer was declined.
     * Sends both email and in-app notification.
     * @param trade the shift trade that was declined
     */
    public void sendTradeDeclinedNotification(com.hotel.scheduler.model.ShiftTrade trade) {
        if (trade.getRequestingEmployee() != null) {
            // Save notification entity
            com.hotel.scheduler.model.Notification notification = com.hotel.scheduler.model.Notification.builder()
                .userId(trade.getRequestingEmployee().getId())
                .title("Shift Trade Declined")
                .message(String.format("Your shift trade offer was declined by %s %s. Shift: %s Date & Time: %s - %s Department: %s",
                    trade.getPickupEmployee() != null ? trade.getPickupEmployee().getFirstName() : "",
                    trade.getPickupEmployee() != null ? trade.getPickupEmployee().getLastName() : "",
                    trade.getShift() != null ? trade.getShift().getId() : "",
                    trade.getShift() != null ? trade.getShift().getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    trade.getShift() != null ? trade.getShift().getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "",
                    trade.getShift() != null && trade.getShift().getDepartment() != null ? trade.getShift().getDepartment().getName() : ""
                ))
                .type("TRADE_DECLINED")
                .read(false)
                .timestamp(java.time.LocalDateTime.now())
                .build();
            notificationRepository.save(notification);
            // Send email
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                mail.setFrom(fromEmail);
                mail.setTo(trade.getRequestingEmployee().getEmail());
                mail.setSubject("Shift Trade Declined");
                mail.setText(notification.getMessage());
                mailSender.send(mail);
                log.info("Trade declined email sent to requesting employee {}", trade.getRequestingEmployee().getEmail());
            } catch (Exception e) {
                log.error("Failed to send trade declined email to requesting employee {}: {}", trade.getRequestingEmployee().getEmail(), e.getMessage());
            }
        }
    }
        
    /**
     * Notify manager/admin that a trade was accepted by the employee (for approval).
     */
    private final com.hotel.scheduler.repository.NotificationRepository notificationRepository;

    /**
     * Returns all notifications for the given user ID.
     */
    public List<com.hotel.scheduler.model.Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * Marks a specific notification as read for the user.
     */
    public void markNotificationAsRead(Long userId, Long notificationId) {
        com.hotel.scheduler.model.Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Marks all notifications as read for the user.
     */
    public void markAllNotificationsAsRead(Long userId) {
        List<com.hotel.scheduler.model.Notification> notifications = notificationRepository.findByUserIdOrderByTimestampDesc(userId);
        for (com.hotel.scheduler.model.Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        }
    }

    /**
     * Deletes a specific notification for the user.
     */
    public void deleteNotification(Long userId, Long notificationId) {
        com.hotel.scheduler.model.Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notificationRepository.delete(notification);
    }

    /**
     * Returns notification settings for the user.
     * (This example uses a simple in-memory map; replace with persistent storage as needed.)
     */
    public java.util.Map<String, Object> getNotificationSettings(Long userId) {
        // TODO: Replace with real settings storage
        java.util.Map<String, Object> settings = new java.util.HashMap<>();
        settings.put("emailNotifications", true);
        settings.put("pushNotifications", true);
        settings.put("shiftReminders", true);
        settings.put("tradeNotifications", true);
        settings.put("scheduleUpdates", true);
        return settings;
    }

    /**
     * Updates notification settings for the user.
     * (This example does not persist settings; implement persistence as needed.)
     */
    public void updateNotificationSettings(Long userId, java.util.Map<String, Object> settings) {
        // TODO: Persist settings for user
    }
    /**
     * Notify the requesting employee that they are still responsible for the shift until accepted by the target employee.
     */
    /**
     * Notifies the requesting employee that they are still responsible for the shift until accepted by the target employee.
     *
     * @param requestingEmployee the employee offering the shift
     * @param targetEmployee     the employee being offered the shift
     * @param shift              the shift being traded
     */
    @Async
    public void sendShiftTradeResponsibilityNotification(Employee requestingEmployee, Employee targetEmployee, Shift shift) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(requestingEmployee.getEmail());
            message.setSubject("Shift Trade Offer - Responsibility Reminder");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "You have offered your shift to %s %s. However, you are still responsible for this shift until it is accepted and confirmed.\n\n" +
                "Date & Time: %s - %s\n" +
                "Department: %s\n" +
                "Notes: %s\n\n" +
                "If the offer is not accepted in time, you are expected to show up for your scheduled shift.\n\n" +
                "You can log in at: " + notificationBaseUrl + "/\n\n" +
                "Best regards,\nShiftly Team",
                requestingEmployee.getFirstName(),
                targetEmployee.getFirstName(),
                targetEmployee.getLastName(),
                shift.getStartTime().format(formatter),
                shift.getEndTime().format(formatter),
                shift.getDepartment().getName(),
                shift.getNotes() != null ? shift.getNotes() : "None"
            ));
            mailSender.send(message);
            log.info("Shift trade responsibility notification sent to {}", requestingEmployee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send shift trade responsibility notification to {}: {}", requestingEmployee.getEmail(), e.getMessage());
        }
    }

    /**
     * Notify the requesting employee that they are still responsible for the shift until someone picks it up.
     */
    /**
     * Notifies the requesting employee that they are still responsible for the shift until someone picks it up.
     *
     * @param requestingEmployee the employee who posted the shift
     * @param shift              the shift being posted
     */
    @Async
    public void sendShiftPostedResponsibilityNotification(Employee requestingEmployee, Shift shift) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(requestingEmployee.getEmail());
            message.setSubject("Shift Posted - Responsibility Reminder");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "You have posted your shift for pickup by other employees. However, you are still responsible for this shift until someone picks it up and it is confirmed.\n\n" +
                "Date & Time: %s - %s\n" +
                "Department: %s\n" +
                "Notes: %s\n\n" +
                "If no one picks up the shift in time, you are expected to show up for your scheduled shift.\n\n" +
                "You can log in at: " + notificationBaseUrl + "/\n\n" +
                "Best regards,\nShiftly Team",
                requestingEmployee.getFirstName(),
                shift.getStartTime().format(formatter),
                shift.getEndTime().format(formatter),
                shift.getDepartment().getName(),
                shift.getNotes() != null ? shift.getNotes() : "None"
            ));
            mailSender.send(message);
            log.info("Shift posted responsibility notification sent to {}", requestingEmployee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send shift posted responsibility notification to {}: {}", requestingEmployee.getEmail(), e.getMessage());
        }
    }
    
    private final JavaMailSender mailSender;
    
    @Value("${app.notification.email.from}")
    private String fromEmail;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.notification.base-url:https://useshiftly.com}")
    private String notificationBaseUrl;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    
    /**
     * Notifies an employee that they have been assigned a new shift.
     *
     * @param employee the employee assigned to the shift
     * @param shift    the new shift
     */
    @Async
    public void sendShiftAssignmentNotification(Employee employee, Shift shift) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(employee.getEmail());
            message.setSubject("New Shift Assignment");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "You have been assigned a new shift:\n\n" +
                "Date & Time: %s - %s\n" +
                "Department: %s\n" +
                "Notes: %s\n\n" +
                "Please log into Shiftly to view more details.\n\n" +
                "You can log in at: " + notificationBaseUrl + "/\n\n" +
                "Best regards,\n" +
                "Shiftly Team",
                employee.getFirstName(),
                shift.getStartTime().format(formatter),
                shift.getEndTime().format(formatter),
                shift.getDepartment().getName(),
                shift.getNotes() != null ? shift.getNotes() : "None"
            ));
            mailSender.send(message);
            log.info("Shift assignment notification sent to {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send shift assignment notification to {}: {}", employee.getEmail(), e.getMessage());
        }
    }
    
    /**
     * Notifies an employee that their shift has been updated.
     *
     * @param employee the employee whose shift was updated
     * @param shift    the updated shift
     */
    @Async
    public void sendShiftUpdateNotification(Employee employee, Shift shift) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(employee.getEmail());
            message.setSubject("Shift Update");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your shift has been updated:\n\n" +
                "Date & Time: %s - %s\n" +
                "Department: %s\n" +
                "Notes: %s\n\n" +
                "Please log into Shiftly to view the updated details.\n\n" +
                "You can log in at: " + notificationBaseUrl + "/\n\n" +
                "Best regards,\n" +
                "Shiftly Team",
                employee.getFirstName(),
                shift.getStartTime().format(formatter),
                shift.getEndTime().format(formatter),
                shift.getDepartment().getName(),
                shift.getNotes() != null ? shift.getNotes() : "None"
            ));
            mailSender.send(message);
            log.info("Shift update notification sent to {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send shift update notification to {}: {}", employee.getEmail(), e.getMessage());
        }
    }
    
    /**
     * Notifies an employee that their shift has been cancelled.
     *
     * @param employee the employee whose shift was cancelled
     * @param shift    the cancelled shift
     */
    @Async
    public void sendShiftCancellationNotification(Employee employee, Shift shift) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(employee.getEmail());
            message.setSubject("Shift Cancelled");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your shift has been cancelled:\n\n" +
                "Date & Time: %s - %s\n" +
                "Department: %s\n\n" +
                "If you have any questions, please contact your manager.\n\n" +
                "You can log in at: " + notificationBaseUrl + "/\n\n" +
                "Best regards,\n" +
                "Shiftly Team",
                employee.getFirstName(),
                shift.getStartTime().format(formatter),
                shift.getEndTime().format(formatter),
                shift.getDepartment().getName()
            ));
            mailSender.send(message);
            log.info("Shift cancellation notification sent to {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send shift cancellation notification to {}: {}", employee.getEmail(), e.getMessage());
        }
    }
    
    /**
     * Notifies both the original and pickup employees about a successful shift pickup.
     *
     * @param originalEmployee the employee who originally owned the shift
     * @param pickupEmployee   the employee who picked up the shift
     * @param shift            the shift that was picked up
     */
    @Async
    public void sendShiftPickupNotification(Employee originalEmployee, Employee pickupEmployee, Shift shift) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        
        // Notify original employee
        try {
            SimpleMailMessage messageToOriginal = new SimpleMailMessage();
            messageToOriginal.setFrom(fromEmail);
            messageToOriginal.setTo(originalEmployee.getEmail());
            messageToOriginal.setSubject("Shift Picked Up");
            messageToOriginal.setText(String.format(
                "Hello %s,\n\n" +
                "Your shift has been picked up by %s %s:\n\n" +
                "Date & Time: %s - %s\n" +
                "Department: %s\n\n" +
                "Thank you for making your shift available.\n\n" +
                "You can log in at: " + notificationBaseUrl + "/\n\n" +
                "Best regards,\n" +
                "Shiftly Team",
                originalEmployee.getFirstName(),
                pickupEmployee.getFirstName(),
                pickupEmployee.getLastName(),
                shift.getStartTime().format(formatter),
                shift.getEndTime().format(formatter),
                shift.getDepartment().getName()
            ));
            
            mailSender.send(messageToOriginal);
            log.info("Shift pickup notification sent to original employee {}", originalEmployee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send shift pickup notification to original employee {}: {}", originalEmployee.getEmail(), e.getMessage());
        }
        
        // Notify pickup employee
        try {
            SimpleMailMessage messageToPickup = new SimpleMailMessage();
            messageToPickup.setFrom(fromEmail);
            messageToPickup.setTo(pickupEmployee.getEmail());
            messageToPickup.setSubject("Shift Assignment Confirmation");
            messageToPickup.setText(String.format(
                "Hello %s,\n\n" +
                "You have successfully picked up a shift:\n\n" +
                "Date & Time: %s - %s\n" +
                "Department: %s\n" +
                "Notes: %s\n\n" +
                "Thank you for your flexibility.\n\n" +
                "You can log in at: " + notificationBaseUrl + "/\n\n" +
                "Best regards,\n" +
                "Shiftly Team",
                pickupEmployee.getFirstName(),
                shift.getStartTime().format(formatter),
                shift.getEndTime().format(formatter),
                shift.getDepartment().getName(),
                shift.getNotes() != null ? shift.getNotes() : "None"
            ));
            
            mailSender.send(messageToPickup);
            log.info("Shift pickup confirmation sent to pickup employee {}", pickupEmployee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send shift pickup confirmation to pickup employee {}: {}", pickupEmployee.getEmail(), e.getMessage());
        }
    }
    
    /**
     * Notifies a target employee that they have received a shift trade offer.
     *
     * @param targetEmployee     the employee being offered the shift
     * @param shift              the shift being offered
     * @param requestingEmployee the employee offering the shift
     */
    @Async
    public void sendShiftTradeOfferNotification(Employee targetEmployee, Shift shift, Employee requestingEmployee) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(targetEmployee.getEmail());
            message.setSubject("Shift Trade Offer");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "%s %s has offered you a shift:\n\n" +
                "Date & Time: %s - %s\n" +
                "Department: %s\n" +
                "Notes: %s\n\n" +
                "Please log into Shiftly to accept or decline this offer.\n\n" +
                "You can log in at: " + notificationBaseUrl + "/\n\n" +
                "Best regards,\n" +
                "Shiftly Team",
                targetEmployee.getFirstName(),
                requestingEmployee.getFirstName(),
                requestingEmployee.getLastName(),
                shift.getStartTime().format(formatter),
                shift.getEndTime().format(formatter),
                shift.getDepartment().getName(),
                shift.getNotes() != null ? shift.getNotes() : "None"
            ));
            mailSender.send(message);
            log.info("Shift trade offer notification sent to {}", targetEmployee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send shift trade offer notification to {}: {}", targetEmployee.getEmail(), e.getMessage());
        }
    }

    /**
     * Notifies all employees (except the requester) that a shift has been posted and is available for pickup.
     *
     * @param shift              the shift being posted
     * @param requestingEmployee the employee posting the shift
     * @param allEmployees       list of all employees to notify
     */
    @Async
    public void sendShiftPostedToEveryoneNotification(Shift shift, Employee requestingEmployee, List<Employee> allEmployees) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        for (Employee employee : allEmployees) {
            if (employee.getId().equals(requestingEmployee.getId())) continue; // Don't notify self
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(employee.getEmail());
                message.setSubject("Shift Available for Pickup");
                message.setText(String.format(
                    "Hello %s,\n\n" +
                    "%s %s has posted a shift available for pickup:\n\n" +
                    "Date & Time: %s - %s\n" +
                    "Department: %s\n" +
                    "Notes: %s\n\n" +
                    "Log into Shiftly to pick up this shift if interested.\n\n" +
                    "You can log in at: " + notificationBaseUrl + "/\n\n" +
                    "Best regards,\n" +
                    "Shiftly Team",
                    employee.getFirstName(),
                    requestingEmployee.getFirstName(),
                    requestingEmployee.getLastName(),
                    shift.getStartTime().format(formatter),
                    shift.getEndTime().format(formatter),
                    shift.getDepartment().getName(),
                    shift.getNotes() != null ? shift.getNotes() : "None"
                ));
                mailSender.send(message);
                log.info("Shift posted notification sent to {}", employee.getEmail());
            } catch (Exception e) {
                log.error("Failed to send shift posted notification to {}: {}", employee.getEmail(), e.getMessage());
            }
        }
    }
    
    /**
     * Sends a notification to both employees and managers when a trade is accepted and pending approval.
     * @param trade the ShiftTrade entity
     */
    public void sendTradeAcceptedNotification(com.hotel.scheduler.model.ShiftTrade trade) {
        // Only notify pickup and requesting employees
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        // Notify pickup employee by email
        if (trade.getPickupEmployee() != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(trade.getPickupEmployee().getEmail());
                message.setSubject("Shift Trade Accepted");
                message.setText("You have accepted a shift trade. Awaiting manager approval.");
                mailSender.send(message);
                log.info("Trade accepted email sent to pickup employee {}", trade.getPickupEmployee().getEmail());
            } catch (Exception e) {
                log.error("Failed to send trade accepted email to pickup employee {}: {}", trade.getPickupEmployee().getEmail(), e.getMessage());
            }
        }
        // Notify requesting employee by email
        if (trade.getRequestingEmployee() != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(trade.getRequestingEmployee().getEmail());
                message.setSubject("Shift Trade Accepted");
                message.setText("Your shift trade has been accepted and is pending manager approval.");
                mailSender.send(message);
                log.info("Trade accepted email sent to requesting employee {}", trade.getRequestingEmployee().getEmail());
            } catch (Exception e) {
                log.error("Failed to send trade accepted email to requesting employee {}: {}", trade.getRequestingEmployee().getEmail(), e.getMessage());
            }
        }
    }

    /**
     * Sends a notification to both employees when a trade is rejected by a manager/admin.
     * @param trade the ShiftTrade entity
     */
    public void sendTradeRejectedNotification(com.hotel.scheduler.model.ShiftTrade trade) {
        // Only notify pickup and requesting employees
        String reasonMsg = trade.getReason() != null ? " Reason: " + trade.getReason() : "";
        if (trade.getPickupEmployee() != null) {
            com.hotel.scheduler.model.Notification notification = com.hotel.scheduler.model.Notification.builder()
                .userId(trade.getPickupEmployee().getId())
                .title("Shift Trade Rejected")
                .message("Your shift trade was rejected by a manager." + reasonMsg)
                .type("TRADE_REJECTED")
                .read(false)
                .timestamp(java.time.LocalDateTime.now())
                .build();
            notificationRepository.save(notification);
        }
        if (trade.getRequestingEmployee() != null) {
            com.hotel.scheduler.model.Notification notification = com.hotel.scheduler.model.Notification.builder()
                .userId(trade.getRequestingEmployee().getId())
                .title("Shift Trade Rejected")
                .message("Your shift trade was rejected by a manager." + reasonMsg)
                .type("TRADE_REJECTED")
                .read(false)
                .timestamp(java.time.LocalDateTime.now())
                .build();
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Sends a registration email to a new employee with their temporary password and login instructions.
     * Also saves a notification for the employee.
     * @param employee The new employee
     * @param tempPassword The temporary password
     */
    public void sendEmployeeRegistrationEmail(Employee employee, String tempPassword) {
        // Save notification entity
        com.hotel.scheduler.model.Notification notification = com.hotel.scheduler.model.Notification.builder()
            .userId(employee.getId())
            .title("Welcome to Shiftly Scheduler")
            .message(String.format(
                "Your account has been created. Temporary password: %s. You will be required to change your password on first login.",
                tempPassword
            ))
            .type("info")
            .read(false)
            .timestamp(java.time.LocalDateTime.now())
            .build();
        notificationRepository.save(notification);
        // Send email
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(employee.getEmail());
            message.setSubject("Welcome to Shiftly Scheduler - Account Created");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your account has been created on Shiftly Scheduler.\n" +
                "You can log in at: " + notificationBaseUrl + "\n\n" +
                "Your temporary password: %s\n" +
                "You will be required to change your password on first login.\n\n" +
                "If you have any questions, please contact your manager or HR.\n\n" +
                "Best regards,\nShiftly Scheduler Team",
                employee.getFirstName(),
                tempPassword
            ));
            mailSender.send(message);
            log.info("Registration email sent to {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", employee.getEmail(), e.getMessage());
        }
    }
}
