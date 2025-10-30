package com.useshiftly.scheduler.service;

import com.useshiftly.scheduler.model.ShiftTemplate;
import com.useshiftly.scheduler.model.Department;
import com.useshiftly.scheduler.repository.ShiftTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShiftTemplateService {
    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;

    public List<ShiftTemplate> getAllTemplates() {
        return shiftTemplateRepository.findAll();
    }

    public List<ShiftTemplate> getTemplatesByDepartment(Long departmentId) {
        return shiftTemplateRepository.findByDepartmentId(departmentId);
    }

    public Optional<ShiftTemplate> getTemplate(Long id) {
        return shiftTemplateRepository.findById(id);
    }

    public ShiftTemplate saveTemplate(ShiftTemplate template) {
        return shiftTemplateRepository.save(template);
    }

    public void deleteTemplate(Long id) {
        shiftTemplateRepository.deleteById(id);
    }
}
