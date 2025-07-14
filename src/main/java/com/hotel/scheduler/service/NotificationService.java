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

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    /**
     * Notify the requesting employee that they are still responsible for the shift until accepted by the target employee.
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
}
