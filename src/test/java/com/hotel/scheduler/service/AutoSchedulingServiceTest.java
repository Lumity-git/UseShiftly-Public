package com.hotel.scheduler.service;

import com.hotel.scheduler.dto.AutoScheduleRequestDTO;
import com.hotel.scheduler.dto.AutoScheduleResultDTO;
import com.hotel.scheduler.model.*;
import com.hotel.scheduler.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoSchedulingServiceTest {
    @Mock
    private ShiftRequirementRepository shiftRequirementRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeAvailabilityRepository employeeAvailabilityRepository;
    @Mock
    private ShiftRepository shiftRepository;
    @InjectMocks
    private AutoSchedulingService autoSchedulingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAutoSchedule_assignsShiftsBasedOnAvailability() {
        Long deptId = 1L;
        LocalDate date = LocalDate.of(2025, 7, 24);
        Department dept = new Department();
        dept.setId(deptId);
        dept.setName("Front Desk");
        dept.setActive(true);
        ShiftRequirement req = new ShiftRequirement(1L, dept, date, LocalTime.of(9,0), LocalTime.of(17,0), 1, null);
        Employee emp = new Employee(); emp.setId(2L); emp.setFirstName("Test"); emp.setLastName("User");
        EmployeeAvailability avail = new EmployeeAvailability(); avail.setEmployeeId(2L); avail.setDay(date.getDayOfWeek().toString()); avail.setStartTime("09:00"); avail.setEndTime("17:00");
        when(shiftRequirementRepository.findByDepartmentIdAndShiftDateBetween(eq(deptId), any(), any())).thenReturn(List.of(req));
        when(employeeRepository.findByDepartmentIdAndActiveTrue(deptId)).thenReturn(List.of(emp));
        when(employeeAvailabilityRepository.findByEmployeeIdIn(List.of(2L))).thenReturn(List.of(avail));
        when(shiftRepository.countConflictingShifts(anyLong(), any(), any())).thenReturn(0L);
        AutoScheduleRequestDTO request = new AutoScheduleRequestDTO();
        request.setDepartmentId(deptId);
        request.setStartDate(date);
        request.setEndDate(date);
        AutoScheduleResultDTO result = autoSchedulingService.autoSchedule(request);
        assertEquals(1, result.getTotalShiftsScheduled());
        assertEquals(0, result.getTotalUnassigned());
    }

    @Test
    void testAutoSchedule_unassignedIfNoAvailableEmployee() {
        Long deptId = 1L;
        LocalDate date = LocalDate.of(2025, 7, 24);
        Department dept = new Department();
        dept.setId(deptId);
        dept.setName("Front Desk");
        dept.setActive(true);
        ShiftRequirement req = new ShiftRequirement(1L, dept, date, LocalTime.of(9,0), LocalTime.of(17,0), 1, null);
        Employee emp = new Employee(); emp.setId(2L);
        when(shiftRequirementRepository.findByDepartmentIdAndShiftDateBetween(eq(deptId), any(), any())).thenReturn(List.of(req));
        when(employeeRepository.findByDepartmentIdAndActiveTrue(deptId)).thenReturn(List.of(emp));
        when(employeeAvailabilityRepository.findByEmployeeIdIn(List.of(2L))).thenReturn(Collections.emptyList());
        AutoScheduleRequestDTO request = new AutoScheduleRequestDTO();
        request.setDepartmentId(deptId);
        request.setStartDate(date);
        request.setEndDate(date);
        AutoScheduleResultDTO result = autoSchedulingService.autoSchedule(request);
        assertEquals(0, result.getTotalShiftsScheduled());
        assertEquals(1, result.getTotalUnassigned());
    }
}
