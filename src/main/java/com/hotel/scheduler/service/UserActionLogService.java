package com.hotel.scheduler.service;

import com.hotel.scheduler.model.UserActionLog;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.repository.UserActionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for logging user actions for auditing and tracking purposes.
 * <p>
 * Persists user action logs to the database, including user, role, building, and action details.
 * <b>Usage:</b> Injected into controllers and services to record important user activities.
 */
@Service
@RequiredArgsConstructor
public class UserActionLogService {
    private final UserActionLogRepository userActionLogRepository;
    private final com.hotel.scheduler.repository.EmployeeRepository employeeRepository;

    /**
     * Logs a user action with associated employee and building information.
     *
     * @param action   the action performed (e.g., "LOGIN", "SHIFT_CREATED")
     * @param employeeId the ID of the employee performing the action
     */
    public void logAction(String action, Long employeeId) {
        if (employeeId == null) return;
        Employee employee = employeeRepository.findByIdWithBuilding(employeeId)
            .orElse(null);
        if (employee == null || employee.getBuilding() == null) return;
        Long buildingId = employee.getBuilding().getId();
        String buildingName = employee.getBuilding().getName();
        UserActionLog log = UserActionLog.builder()
                .action(action)
                .userUuid(employee.getUuid())
                .userEmail(employee.getEmail())
                .role(employee.getRole().name())
                .buildingId(buildingId)
                .buildingName(buildingName)
                .build();
        userActionLogRepository.save(log);
    }
}
