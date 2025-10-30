package com.useshiftly.scheduler.controller;

import com.useshiftly.scheduler.model.EmployeeAvailability;
import com.useshiftly.scheduler.repository.EmployeeAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeAvailabilityController {

    @Autowired
    private EmployeeAvailabilityRepository availabilityRepo;

    @GetMapping("/{employeeId}/availability")
    public List<EmployeeAvailability> getAvailability(@PathVariable Long employeeId) {
        return availabilityRepo.findByEmployeeId(employeeId);
    }

    @PostMapping("/{employeeId}/availability")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> setAvailability(@PathVariable Long employeeId, @RequestBody List<EmployeeAvailability> availabilities) {
        availabilityRepo.deleteByEmployeeId(employeeId);
        for (EmployeeAvailability avail : availabilities) {
            avail.setEmployeeId(employeeId);
            availabilityRepo.save(avail);
        }
        return ResponseEntity.ok().build();
    }
}
