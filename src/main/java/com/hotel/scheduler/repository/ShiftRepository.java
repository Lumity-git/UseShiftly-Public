
package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * ShiftRepository: Spring Data JPA repository for Shift entities.
 *
 * Usage:
 * - Provides CRUD and custom query methods for retrieving and managing shifts.
 * - Used by the service layer for all shift-related data access.
 *
 * Key Methods:
 * - findByEmployeeId: Get all shifts for a specific employee
 * - findByDepartmentId: Get all shifts for a specific department
 * - findByDateRange: Get all shifts within a date range
 * - findByEmployeeAndDateRange: Get shifts for an employee within a date range
 * - findAvailableForPickup: Get all shifts available for pickup/trade
 * - findByDepartmentAndDateRange: Get shifts for a department within a date range
 * - countConflictingShifts: Count shifts that conflict with a given time range (for conflict checking)
 */
@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    /**
     * Returns all shifts assigned to a specific employee.
     * @param employeeId Employee ID
     * @return List of shifts
     */
    List<Shift> findByEmployeeId(Long employeeId);

    /**
     * Returns all shifts for a specific department.
     * @param departmentId Department ID
     * @return List of shifts
     */
    List<Shift> findByDepartmentId(Long departmentId);

    /**
     * Returns all shifts within a given date range.
     * @param startDate Start date/time
     * @param endDate End date/time
     * @return List of shifts
     */
    @Query("SELECT s FROM Shift s WHERE s.startTime >= :startDate AND s.endTime <= :endDate")
    List<Shift> findByDateRange(@Param("startDate") OffsetDateTime startDate, 
                               @Param("endDate") OffsetDateTime endDate);

    /**
     * Returns all shifts for an employee within a date range.
     * @param employeeId Employee ID
     * @param startDate Start date/time
     * @param endDate End date/time
     * @return List of shifts
     */
    @Query("SELECT s FROM Shift s WHERE s.employee.id = :employeeId AND " +
           "s.startTime >= :startDate AND s.endTime <= :endDate")
    List<Shift> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                          @Param("startDate") OffsetDateTime startDate,
                                          @Param("endDate") OffsetDateTime endDate);

    /**
     * Returns all shifts available for pickup/trade.
     * @return List of available shifts
     */
    @Query("SELECT s FROM Shift s WHERE s.availableForPickup = true AND s.status = 'AVAILABLE_FOR_PICKUP'")
    List<Shift> findAvailableForPickup();

    /**
     * Returns all shifts for a department within a date range.
     * @param departmentId Department ID
     * @param startDate Start date/time
     * @param endDate End date/time
     * @return List of shifts
     */
    @Query("SELECT s FROM Shift s WHERE s.department.id = :departmentId AND " +
           "s.startTime >= :startDate AND s.endTime <= :endDate")
    List<Shift> findByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                                           @Param("startDate") OffsetDateTime startDate,
                                           @Param("endDate") OffsetDateTime endDate);

    /**
     * Counts the number of shifts that conflict with a given time range for an employee.
     * Used to prevent scheduling conflicts.
     * @param employeeId Employee ID
     * @param startTime Proposed shift start
     * @param endTime Proposed shift end
     * @return Number of conflicting shifts
     */
    @Query("SELECT COUNT(s) FROM Shift s WHERE s.employee.id = :employeeId AND " +
           "s.startTime >= :startTime AND s.endTime <= :endTime")
    long countConflictingShifts(@Param("employeeId") Long employeeId,
                               @Param("startTime") OffsetDateTime startTime,
                               @Param("endTime") OffsetDateTime endTime);
}
