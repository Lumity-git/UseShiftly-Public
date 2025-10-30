package com.useshiftly.scheduler.service;

import com.useshiftly.scheduler.model.ShiftRequirement;
import com.useshiftly.scheduler.repository.ShiftRequirementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftRequirementService {
    private final ShiftRequirementRepository shiftRequirementRepository;

    @Autowired
    public ShiftRequirementService(ShiftRequirementRepository shiftRequirementRepository) {
        this.shiftRequirementRepository = shiftRequirementRepository;
    }

    public ShiftRequirement save(ShiftRequirement shiftRequirement) {
        return shiftRequirementRepository.save(shiftRequirement);
    }

    public Optional<ShiftRequirement> findById(Long id) {
        return shiftRequirementRepository.findById(id);
    }

    public List<ShiftRequirement> findAll() {
        return shiftRequirementRepository.findAll();
    }

    public void deleteById(Long id) {
        shiftRequirementRepository.deleteById(id);
    }

    public List<ShiftRequirement> findByDepartment(Long departmentId) {
        return shiftRequirementRepository.findByDepartmentId(departmentId);
    }

    public List<ShiftRequirement> findByDepartmentAndDateRange(Long departmentId, LocalDate start, LocalDate end) {
        return shiftRequirementRepository.findByDepartmentIdAndShiftDateBetween(departmentId, start, end);
    }
}
