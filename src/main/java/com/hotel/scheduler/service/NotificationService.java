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
     * Notify manager/admin that a trade was accepted by the employee (for approval).
     */
    private final com.hotel.scheduler.repository.EmployeeRepository employeeRepository;

    // ...existing code...
    /**
     * Sends a registration email to a new employee with their temporary password and login instructions.
     * @param employee The new employee
     * @param tempPassword The temporary password
     */
    @Async
    public void sendEmployeeRegistrationEmail(Employee employee, String tempPassword) {
        if (!emailEnabled) {
            log.info("Email notifications disabled");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(employee.getEmail());
            message.setSubject("Welcome to Hotel Scheduler - Account Created");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your account has been created on Hotel Scheduler.\n" +
                "You can log in at: http://localhost:8080/login.html\n\n" +
                "Your temporary password: %s\n" +
                "You will be required to change your password on first login.\n\n" +
                "If you have any questions, please contact your manager or HR.\n\n" +
                "Best regards,\nHotel Scheduler Team",
                employee.getFirstName(),
                tempPassword
            ));
            mailSender.send(message);
            log.info("Registration email sent to {}", employee.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", employee.getEmail(), e.getMessage());
        }
    }

    /**
     * Notify requester that a trade was declined by the employee.
     */
    /**
     * Notifies the requesting employee that their shift trade offer was declined.
     *
     * @param trade the shift trade that was declined
     */
    public void sendTradeDeclinedNotification(com.hotel.scheduler.model.ShiftTrade trade) {
        // Instead of email, create notification object for requesting employee
        if (trade.getRequestingEmployee() != null) {
            createNotification(
                trade.getRequestingEmployee(),
                "Shift Trade Declined",
                String.format("Your shift trade offer was declined by %s %s. Shift: %s Date & Time: %s - %s Department: %s",
                    trade.getPickupEmployee() != null ? trade.getPickupEmployee().getFirstName() : "",
                    trade.getPickupEmployee() != null ? trade.getPickupEmployee().getLastName() : "",
                    trade.getShift() != null ? trade.getShift().getId() : "",
                    trade.getShift() != null ? trade.getShift().getStartTime().format(formatter) : "",
                    trade.getShift() != null ? trade.getShift().getEndTime().format(formatter) : "",
                    trade.getShift() != null && trade.getShift().getDepartment() != null ? trade.getShift().getDepartment().getName() : ""
                ),
                "TRADE_DECLINED"
            );
        }
    }
    // Helper to create and save notification objects for the frontend tab
    /**
     * Helper to create and save notification objects for the frontend notification tab.
     *
     * @param recipient the employee to notify
     * @param title     the notification title
     * @param message   the notification message
     * @param type      the notification type (e.g., TRADE_ACCEPTED)
     */
    private void createNotification(com.hotel.scheduler.model.Employee recipient, String title, String message, String type) {
        // TODO: Implement Notification entity and repository
        // Notification notification = new Notification();
        // notification.setRecipient(recipient);
        // notification.setTitle(title);
        // notification.setMessage(message);
        // notification.setType(type);
        // notification.setTimestamp(java.time.OffsetDateTime.now());
        // notification.setRead(false);
        // notificationRepository.save(notification);
        log.info("[NOTIFY] Would create notification for {}: {} - {}", recipient.getEmail(), title, message);
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
                "Best regards,\nHotel Management",
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
                "Best regards,\nHotel Management",
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
                "Please log into the scheduling system to view more details.\n\n" +
                "Best regards,\n" +
                "Hotel Management",
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
                "Please log into the scheduling system to view the updated details.\n\n" +
                "Best regards,\n" +
                "Hotel Management",
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
                "Best regards,\n" +
                "Hotel Management",
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
                "Best regards,\n" +
                "Hotel Management",
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
                "Best regards,\n" +
                "Hotel Management",
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
                "Please log into the scheduling system to accept or decline this offer.\n\n" +
                "Best regards,\n" +
                "Hotel Management",
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
                    "Log into the scheduling system to pick up this shift if interested.\n\n" +
                    "Best regards,\n" +
                    "Hotel Management",
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
        // Notify all managers/admins
        List<com.hotel.scheduler.model.Employee> managers = employeeRepository.findAll().stream()
            .filter(e -> e.getRole() == com.hotel.scheduler.model.Employee.Role.MANAGER || e.getRole() == com.hotel.scheduler.model.Employee.Role.ADMIN)
            .toList();
        for (com.hotel.scheduler.model.Employee manager : managers) {
            createNotification(
                manager,
                "Shift Trade Accepted - Pending Manager Approval",
                String.format("A shift trade has been accepted by %s %s and is now pending your approval. Shift: %s Date & Time: %s - %s Department: %s",
                    trade.getPickupEmployee() != null ? trade.getPickupEmployee().getFirstName() : "",
                    trade.getPickupEmployee() != null ? trade.getPickupEmployee().getLastName() : "",
                    trade.getShift() != null ? trade.getShift().getId() : "",
                    trade.getShift() != null ? trade.getShift().getStartTime().format(formatter) : "",
                    trade.getShift() != null ? trade.getShift().getEndTime().format(formatter) : "",
                    trade.getShift() != null && trade.getShift().getDepartment() != null ? trade.getShift().getDepartment().getName() : ""
                ),
                "TRADE_ACCEPTED"
            );
        }
        // Notify pickup employee
        if (trade.getPickupEmployee() != null) {
            createNotification(
                trade.getPickupEmployee(),
                "Shift Trade Accepted",
                "You have accepted a shift trade. Awaiting manager approval.",
                "TRADE_ACCEPTED"
            );
        }
        // Notify requesting employee
        if (trade.getRequestingEmployee() != null) {
            createNotification(
                trade.getRequestingEmployee(),
                "Shift Trade Accepted",
                "Your shift trade has been accepted and is pending manager approval.",
                "TRADE_ACCEPTED"
            );
        }
    }

    /**
     * Sends a notification to both employees when a trade is rejected by a manager/admin.
     * @param trade the ShiftTrade entity
     */
    public void sendTradeRejectedNotification(com.hotel.scheduler.model.ShiftTrade trade) {
        String reasonMsg = trade.getReason() != null ? " Reason: " + trade.getReason() : "";
        // Notify pickup employee
        if (trade.getPickupEmployee() != null) {
            createNotification(
                trade.getPickupEmployee(),
                "Shift Trade Rejected",
                "Your shift trade was rejected by a manager." + reasonMsg,
                "TRADE_REJECTED"
            );
        }
        // Notify requesting employee
        if (trade.getRequestingEmployee() != null) {
            createNotification(
                trade.getRequestingEmployee(),
                "Shift Trade Rejected",
                "Your shift trade was rejected by a manager." + reasonMsg,
                "TRADE_REJECTED"
            );
        }
    }
}
