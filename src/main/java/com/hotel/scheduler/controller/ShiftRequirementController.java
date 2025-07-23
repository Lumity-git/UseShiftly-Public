package com.hotel.scheduler.controller;

import com.hotel.scheduler.model.ShiftRequirement;
import com.hotel.scheduler.service.ShiftRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shift-requirements")
public class ShiftRequirementController {
    private final ShiftRequirementService shiftRequirementService;

    @Autowired
    public ShiftRequirementController(ShiftRequirementService shiftRequirementService) {
        this.shiftRequirementService = shiftRequirementService;
    }

    @GetMapping
    public List<ShiftRequirement> getAll() {
        return shiftRequirementService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftRequirement> getById(@PathVariable Long id) {
        Optional<ShiftRequirement> req = shiftRequirementService.findById(id);
        return req.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ShiftRequirement create(@RequestBody ShiftRequirement shiftRequirement) {
        return shiftRequirementService.save(shiftRequirement);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftRequirement> update(@PathVariable Long id, @RequestBody ShiftRequirement updated) {
        return shiftRequirementService.findById(id)
                .map(existing -> {
                    updated.setId(id);
                    return ResponseEntity.ok(shiftRequirementService.save(updated));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (shiftRequirementService.findById(id).isPresent()) {
            shiftRequirementService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/department/{departmentId}")
    public List<ShiftRequirement> getByDepartment(@PathVariable Long departmentId) {
        return shiftRequirementService.findByDepartment(departmentId);
    }

    @GetMapping("/department/{departmentId}/range")
    public List<ShiftRequirement> getByDepartmentAndDateRange(
            @PathVariable Long departmentId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        return shiftRequirementService.findByDepartmentAndDateRange(departmentId, start, end);
    }
}
