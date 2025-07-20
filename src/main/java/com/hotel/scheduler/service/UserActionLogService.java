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

    /**
     * Logs a user action with associated employee and building information.
     *
     * @param action   the action performed (e.g., "LOGIN", "SHIFT_CREATED")
     * @param employee the employee performing the action
     */
    public void logAction(String action, Employee employee) {
        if (employee == null || employee.getBuilding() == null) return;
        Long buildingId = null;
        String buildingName = null;
        try {
            buildingId = employee.getBuilding().getId();
            buildingName = employee.getBuilding().getName();
        } catch (Exception e) {
            // Avoid LazyInitializationException
            buildingId = employee.getBuilding().getId();
            buildingName = "Unknown";
        }
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
