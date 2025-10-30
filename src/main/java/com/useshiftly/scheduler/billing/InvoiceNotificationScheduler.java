
package com.useshiftly.scheduler.billing;

import com.useshiftly.scheduler.model.Employee;
import com.useshiftly.scheduler.service.EmployeeService;
import com.useshiftly.scheduler.service.NotificationService;
import com.useshiftly.scheduler.service.UserActionLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class InvoiceNotificationScheduler {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserActionLogService userActionLogService;

    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendUpcomingInvoiceNotifications() {
        List<Employee> admins = employeeService.getAllAdmins();
        LocalDate today = LocalDate.now();
        for (Employee admin : admins) {
            // Example: Assume billing is monthly on the 1st
            LocalDate nextBillingDate = today.withDayOfMonth(1).plusMonths(1);
            long daysUntilBilling = ChronoUnit.DAYS.between(today, nextBillingDate);
            if (daysUntilBilling == 2) {
                // Get all employees for this admin
                List<Employee> employees = employeeService.getAllByAdminId(admin.getId());
                // --- PACKAGE LOGIC ---
                // For now, assume admin has a field or method getPackageType() returning "Basic" or "Pro"
                // TODO: Replace with actual package field or method on Employee
                String packageType = "Basic";
                if (admin instanceof Employee && admin.getRole() == Employee.Role.ADMIN) {
                    // Example: If you add a getPackageType() method, use it here
                    // packageType = admin.getPackageType();
                    // For now, fallback to email domain logic for demo purposes
                    if (admin.getEmail() != null && admin.getEmail().endsWith("@pro.com")) {
                        packageType = "Pro";
                    }
                }
                int billableUsers;
                double projectedBill;
                if (packageType.equals("Basic")) {
                    billableUsers = Math.max(0, employees.size() - 5);
                    projectedBill = billableUsers * 4.0;
                } else if (packageType.equals("Pro")) {
                    billableUsers = employees.size();
                    projectedBill = billableUsers * 7.0;
                } else {
                    billableUsers = 0;
                    projectedBill = 0.0;
                }
                String period = nextBillingDate.getMonth() + " " + nextBillingDate.getYear();
                // Compose email
                StringBuilder body = new StringBuilder();
                body.append("Dear ").append(admin.getEmail()).append(",\n\n");
                body.append("Your upcoming invoice for period ").append(period).append(" is due in 2 days.\n");
                body.append("Package: ").append(packageType).append("\n");
                body.append("Employees: ").append(employees.size()).append("\n");
                body.append("Billable Users: ").append(billableUsers).append("\n");
                body.append("Projected Charge: $").append(projectedBill).append("\n\n");
                if (packageType.equals("Pro")) {
                    body.append("Includes website, email, and full Android/iPhone app access.\n\n");
                } else {
                    body.append("Includes website and email access only.\n\n");
                }
                body.append("Please ensure payment to avoid service interruption.\n\n");
                body.append("Thank you,\nShiftly Scheduler Team");
                // Send email
                notificationService.sendEmail(admin.getEmail(), "Upcoming Invoice Notification", body.toString());
                // Log notification event in audit log
                userActionLogService.logAction("INVOICE_NOTIFICATION_SENT", admin.getId());
            }
        }
    }
}
