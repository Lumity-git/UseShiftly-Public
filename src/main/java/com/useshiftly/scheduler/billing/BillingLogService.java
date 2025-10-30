package com.useshiftly.scheduler.billing;

import com.useshiftly.scheduler.model.Employee;
import com.useshiftly.scheduler.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillingLogService {
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * For each admin/building, log billable users for a billing period.
     * @param buildingId Building ID (admin's building)
     * @param periodStart Start of billing period
     * @param periodEnd End of billing period
     * @return Billable user count
     */
    public int logBillableUsers(Long buildingId, LocalDateTime periodStart, LocalDateTime periodEnd) {
        List<Employee> employees = employeeRepository.findActiveForBillingPeriod(buildingId, periodStart);
        // Only count employees with role EMPLOYEE
        List<Long> employeeIds = employees.stream()
                .filter(e -> e.getRole() == Employee.Role.EMPLOYEE)
                .map(Employee::getId)
                .collect(Collectors.toList());
        int billable = Math.max(0, employeeIds.size() - 5);
        // TODO: Insert into BillingLog table (use JDBC or JPA)
        // Example: billingLogRepository.save(new BillingLog(...));
        return billable;
    }
}
