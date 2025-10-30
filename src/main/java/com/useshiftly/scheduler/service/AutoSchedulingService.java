package com.useshiftly.scheduler.service;


import com.useshiftly.scheduler.dto.AutoScheduleRequestDTO;
import com.useshiftly.scheduler.dto.AutoScheduleResultDTO;
import com.useshiftly.scheduler.model.ShiftRequirement;
import com.useshiftly.scheduler.model.Employee;
import com.useshiftly.scheduler.model.EmployeeAvailability;
import com.useshiftly.scheduler.model.Shift;
import com.useshiftly.scheduler.repository.ShiftRequirementRepository;
import com.useshiftly.scheduler.repository.EmployeeRepository;
import com.useshiftly.scheduler.repository.EmployeeAvailabilityRepository;
import com.useshiftly.scheduler.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Service for auto-scheduling shifts based on requirements and employee availability.
 * Uses injected Clock for consistent timezone-aware operations.
 */
@Service
@RequiredArgsConstructor
public class AutoSchedulingService {
    private final ShiftRequirementRepository shiftRequirementRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final ShiftRepository shiftRepository;
    private final com.useshiftly.scheduler.repository.ShiftTemplateRepository shiftTemplateRepository;
    private final Clock clock;

    @Transactional
    public AutoScheduleResultDTO autoSchedule(AutoScheduleRequestDTO request) {
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

        // If templatePairs are provided, use them for scheduling
        if (request.getTemplatePairs() != null && !request.getTemplatePairs().isEmpty()) {
            java.time.LocalDate current = request.getStartDate();
            while (!current.isAfter(request.getEndDate())) {
                String dayOfWeek = current.getDayOfWeek().toString();
                for (com.useshiftly.scheduler.dto.AutoScheduleTemplatePairDTO pair : request.getTemplatePairs()) {
                    final com.useshiftly.scheduler.model.ShiftTemplate startTemplate = (pair.getStartTemplateId() != null)
                        ? shiftTemplateRepository.findById(pair.getStartTemplateId()).orElse(null) : null;
                    final com.useshiftly.scheduler.model.ShiftTemplate endTemplate = (pair.getEndTemplateId() != null)
                        ? shiftTemplateRepository.findById(pair.getEndTemplateId()).orElse(null) : null;
                    if (startTemplate == null || endTemplate == null) continue;
                    if ((startTemplate.getIsActive() != null && !startTemplate.getIsActive()) || (endTemplate.getIsActive() != null && !endTemplate.getIsActive())) continue;
                    if (startTemplate.getDaysOfWeek() == null || !startTemplate.getDaysOfWeek().contains(dayOfWeek)) continue;
                    if (endTemplate.getDaysOfWeek() == null || !endTemplate.getDaysOfWeek().contains(dayOfWeek)) continue;
                    // Find available employees for this shift
                    List<Employee> shuffled = new ArrayList<>(employees);
                    Collections.shuffle(shuffled);
                    boolean assigned = false;
                    final String finalDayOfWeek = dayOfWeek;
                    for (Employee emp : shuffled) {
                    List<EmployeeAvailability> empAvail = availMap.getOrDefault(emp.getId(), Collections.emptyList());
                    boolean available;
                    if (empAvail.isEmpty()) {
                        // No availability set: treat as available for any shift
                        available = true;
                    } else {
                        available = empAvail.stream().anyMatch(a ->
                            a.getDay().equalsIgnoreCase(finalDayOfWeek) &&
                            java.time.LocalTime.parse(a.getStartTime()).compareTo(startTemplate.getStartTime()) <= 0 &&
                            java.time.LocalTime.parse(a.getEndTime()).compareTo(endTemplate.getEndTime()) >= 0
                        );
                    }
                    if (!available) continue;
                        // Check for shift conflicts
                        OffsetDateTime shiftStart = current.atTime(startTemplate.getStartTime()).atOffset(clock.getZone().getRules().getOffset(clock.instant()));
                        OffsetDateTime shiftEnd = current.atTime(endTemplate.getEndTime()).atOffset(clock.getZone().getRules().getOffset(clock.instant()));
                        long conflicts = shiftRepository.countConflictingShifts(emp.getId(), shiftStart, shiftEnd);
                        if (conflicts > 0) continue;
                        // Assign shift
                        Shift shift = new Shift();
                        shift.setStartTime(shiftStart);
                        shift.setEndTime(shiftEnd);
                        shift.setEmployee(emp);
                        shift.setDepartment(startTemplate.getDepartment());
                        shift.setStatus(Shift.ShiftStatus.SCHEDULED);
                        shift.setAvailableForPickup(false);
                        shift.setCreatedAt(OffsetDateTime.now(clock));
                        shift.setCreatedBy(emp);
                        shiftRepository.save(shift);
                        totalShiftsScheduled++;
                        assigned = true;
                        break; // Only assign one employee per template pair per day
                    }
                    if (!assigned) totalUnassigned++;
                }
                current = current.plusDays(1);
            }
        } else {
            // Fallback: use ShiftRequirements as before
            List<ShiftRequirement> requirements = shiftRequirementRepository.findByDepartmentIdAndShiftDateBetween(
                    request.getDepartmentId(), request.getStartDate(), request.getEndDate());
            for (ShiftRequirement req : requirements) {
                int assigned = 0;
                List<Employee> shuffled = new ArrayList<>(employees);
                Collections.shuffle(shuffled); // Randomize for fairness
                for (Employee emp : shuffled) {
                    if (assigned >= req.getRequiredEmployees()) break;
                    List<EmployeeAvailability> empAvail = availMap.getOrDefault(emp.getId(), Collections.emptyList());
                    boolean available;
                    if (empAvail.isEmpty()) {
                        // No availability set: treat as available for any shift
                        available = true;
                    } else {
                        available = empAvail.stream().anyMatch(a ->
                            a.getDay().equalsIgnoreCase(req.getShiftDate().getDayOfWeek().toString()) &&
                            LocalTime.parse(a.getStartTime()).compareTo(req.getStartTime()) <= 0 &&
                            LocalTime.parse(a.getEndTime()).compareTo(req.getEndTime()) >= 0
                        );
                    }
                    if (!available) continue;
                    // Check for shift conflicts
                    OffsetDateTime shiftStart = req.getShiftDate().atTime(req.getStartTime()).atOffset(clock.getZone().getRules().getOffset(clock.instant()));
                    OffsetDateTime shiftEnd = req.getShiftDate().atTime(req.getEndTime()).atOffset(clock.getZone().getRules().getOffset(clock.instant()));
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
                    shift.setCreatedAt(OffsetDateTime.now(clock));
                    shift.setCreatedBy(emp); // Optionally set to system/admin
                    shiftRepository.save(shift);
                    assigned++;
                    totalShiftsScheduled++;
                }
                if (assigned < req.getRequiredEmployees()) {
                    totalUnassigned += (req.getRequiredEmployees() - assigned);
                }
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
