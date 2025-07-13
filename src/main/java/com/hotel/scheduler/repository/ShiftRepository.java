package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    
    List<Shift> findByEmployeeId(Long employeeId);
    
    List<Shift> findByDepartmentId(Long departmentId);
    
    @Query("SELECT s FROM Shift s WHERE s.startTime >= :startDate AND s.endTime <= :endDate")
    List<Shift> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Shift s WHERE s.employee.id = :employeeId AND " +
           "s.startTime >= :startDate AND s.endTime <= :endDate")
    List<Shift> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Shift s WHERE s.availableForPickup = true AND s.status = 'AVAILABLE_FOR_PICKUP'")
    List<Shift> findAvailableForPickup();
    
    @Query("SELECT s FROM Shift s WHERE s.department.id = :departmentId AND " +
           "s.startTime >= :startDate AND s.endTime <= :endDate")
    List<Shift> findByDepartmentAndDateRange(@Param("departmentId") Long departmentId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Shift s WHERE s.employee.id = :employeeId AND " +
           "s.startTime >= :startTime AND s.endTime <= :endTime")
    long countConflictingShifts(@Param("employeeId") Long employeeId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
}
