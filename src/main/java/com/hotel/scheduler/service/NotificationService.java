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

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
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
}
