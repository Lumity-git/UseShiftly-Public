package com.hotel.scheduler.repository;

import com.hotel.scheduler.model.ShiftTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftTradeRepository extends JpaRepository<ShiftTrade, Long> {
    @Query("SELECT st FROM ShiftTrade st WHERE st.shift.id = :shiftId")
    List<ShiftTrade> findByShiftId(@Param("shiftId") Long shiftId);
    
    List<ShiftTrade> findByRequestingEmployeeId(Long employeeId);
    
    List<ShiftTrade> findByPickupEmployeeId(Long employeeId);
    
    List<ShiftTrade> findByStatus(ShiftTrade.TradeStatus status);
    
    @Query("SELECT st FROM ShiftTrade st WHERE st.shift.department.id = :departmentId")
    List<ShiftTrade> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT st FROM ShiftTrade st WHERE st.requestingEmployee.id = :employeeId OR st.pickupEmployee.id = :employeeId")
    List<ShiftTrade> findByEmployeeInvolved(@Param("employeeId") Long employeeId);
}
