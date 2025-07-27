package com.hotel.scheduler.billing;

import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.EmployeeRepository;
import com.hotel.scheduler.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class BillingUsageService {
    /**
     * Returns a map of admin email to package type for billing.
     */
    public Map<String, String> getAdminPackageTypes() {
        Map<String, String> result = new HashMap<>();
        List<Employee> admins = employeeRepository.findByRoleAndActiveTrue(Employee.Role.ADMIN);
        for (Employee admin : admins) {
            result.put(admin.getEmail(), admin.getPackageType() != null ? admin.getPackageType() : "Basic");
        }
        return result;
    }
    /**
     * Returns a map of admin email to employee count for billing.
     * This is used by the billing calculation logic.
     */
    public Map<String, Integer> getAdminEmployeeCounts() {
        // TODO: Replace with real repository logic
        // Example stub: fetch all admins and their employee counts
        Map<String, Integer> result = new java.util.HashMap<>();
        // result.put("admin1@company.com", 8);
        // result.put("admin2@company.com", 4);
        return result;
    }
    private final EmployeeRepository employeeRepository;
    private final BuildingRepository buildingRepository;

    /**
     * Returns a list of usage stats for each admin: employees, managers, buildings, billable users, over free tier.
     */
    public List<Map<String, Object>> getAllAdminUsage() {
        // Find all admins
        List<Employee> admins = employeeRepository.findByRoleAndActiveTrue(Employee.Role.ADMIN);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Employee admin : admins) {
            long employees = employeeRepository.countByAdminIdAndRole(admin.getId(), Employee.Role.EMPLOYEE);
            long managers = employeeRepository.countByAdminIdAndRole(admin.getId(), Employee.Role.MANAGER);
            long buildings = buildingRepository.countByAdminId(admin.getId());
            long billable = Math.max(0, employees - 5);
            boolean overFree = employees > 5;
            Map<String, Object> row = new HashMap<>();
            row.put("adminEmail", admin.getEmail());
            row.put("employees", employees);
            row.put("managers", managers);
            row.put("buildings", buildings);
            row.put("billableUsers", billable);
            row.put("overFreeTier", overFree);
            result.add(row);
        }
        return result;
    }
}
