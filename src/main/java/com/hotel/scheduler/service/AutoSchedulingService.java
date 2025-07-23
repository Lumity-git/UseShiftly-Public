package com.hotel.scheduler.service;


import com.hotel.scheduler.dto.AutoScheduleRequestDTO;
import com.hotel.scheduler.dto.AutoScheduleResultDTO;
import com.hotel.scheduler.model.ShiftRequirement;
import com.hotel.scheduler.model.Employee;
import com.hotel.scheduler.model.EmployeeAvailability;
import com.hotel.scheduler.model.Shift;
import com.hotel.scheduler.repository.ShiftRequirementRepository;
import com.hotel.scheduler.repository.EmployeeRepository;
import com.hotel.scheduler.repository.EmployeeAvailabilityRepository;
import com.hotel.scheduler.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AutoSchedulingService {
    private final ShiftRequirementRepository shiftRequirementRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final ShiftRepository shiftRepository;

    @Transactional
    public AutoScheduleResultDTO autoSchedule(AutoScheduleRequestDTO request) {
        List<ShiftRequirement> requirements = shiftRequirementRepository.findByDepartmentIdAndShiftDateBetween(
                request.getDepartmentId(), request.getStartDate(), request.getEndDate());
        List<Employee> employees = employeeRepository.findByDepartmentIdAndActiveTrue(request.getDepartmentId());
        List<Long> employeeIds = new ArrayList<>();
        for (Employee e : employees) employeeIds.add(e.getId());
        Map<Long, List<EmployeeAvailability>> availMap = new HashMap<>();
        if (!employeeIds.isEmpty()) {
            List<EmployeeAvailability> allAvail = employeeAvailabilityRepository.findByEmployeeIdIn(employeeIds);
            for (EmployeeAvailability avail : allAvail) {
                availMap.computeIfAbsent(avail.getEmployeeId(), k -> new ArrayList<>()).add(avail);
            }
        }

        int totalShiftsScheduled = 0;
        int totalUnassigned = 0;

        for (ShiftRequirement req : requirements) {
            int assigned = 0;
            List<Employee> shuffled = new ArrayList<>(employees);
            Collections.shuffle(shuffled); // Randomize for fairness
            for (Employee emp : shuffled) {
                if (assigned >= req.getRequiredEmployees()) break;
                List<EmployeeAvailability> empAvail = availMap.getOrDefault(emp.getId(), Collections.emptyList());
                boolean available = empAvail.stream().anyMatch(a ->
                        a.getDay().equalsIgnoreCase(req.getShiftDate().getDayOfWeek().toString()) &&
                        LocalTime.parse(a.getStartTime()).compareTo(req.getStartTime()) <= 0 &&
                        LocalTime.parse(a.getEndTime()).compareTo(req.getEndTime()) >= 0
                );
                if (!available) continue;
                // Check for shift conflicts
                OffsetDateTime shiftStart = req.getShiftDate().atTime(req.getStartTime()).atOffset(OffsetDateTime.now().getOffset());
                OffsetDateTime shiftEnd = req.getShiftDate().atTime(req.getEndTime()).atOffset(OffsetDateTime.now().getOffset());
                long conflicts = shiftRepository.countConflictingShifts(emp.getId(), shiftStart, shiftEnd);
                if (conflicts > 0) continue;
                // Assign shift
                Shift shift = new Shift();
                shift.setStartTime(shiftStart);
                shift.setEndTime(shiftEnd);
                shift.setEmployee(emp);
                shift.setDepartment(req.getDepartment());
                shift.setStatus(Shift.ShiftStatus.SCHEDULED);
                shift.setAvailableForPickup(false);
                shift.setCreatedAt(OffsetDateTime.now());
                shift.setCreatedBy(emp); // Optionally set to system/admin
                shiftRepository.save(shift);
                assigned++;
                totalShiftsScheduled++;
            }
            if (assigned < req.getRequiredEmployees()) {
                totalUnassigned += (req.getRequiredEmployees() - assigned);
            }
        }

        AutoScheduleResultDTO result = new AutoScheduleResultDTO();
        result.setDepartmentId(request.getDepartmentId());
        result.setStartDate(request.getStartDate());
        result.setEndDate(request.getEndDate());
        result.setTotalShiftsScheduled(totalShiftsScheduled);
        result.setTotalUnassigned(totalUnassigned);
        return result;
    }
}
