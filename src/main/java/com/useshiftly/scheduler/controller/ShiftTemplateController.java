package com.useshiftly.scheduler.controller;

import com.useshiftly.scheduler.model.ShiftTemplate;
import com.useshiftly.scheduler.service.ShiftTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shift-templates")
public class ShiftTemplateController {
    @Autowired
    private ShiftTemplateService shiftTemplateService;

    @GetMapping
    public List<ShiftTemplate> getAllTemplates() {
        return shiftTemplateService.getAllTemplates();
    }

    @GetMapping("/department/{departmentId}")
    public List<ShiftTemplate> getTemplatesByDepartment(@PathVariable Long departmentId) {
        return shiftTemplateService.getTemplatesByDepartment(departmentId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftTemplate> getTemplate(@PathVariable Long id) {
        Optional<ShiftTemplate> template = shiftTemplateService.getTemplate(id);
        return template.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ShiftTemplate createTemplate(@RequestBody ShiftTemplate template) {
        return shiftTemplateService.saveTemplate(template);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftTemplate> updateTemplate(@PathVariable Long id, @RequestBody ShiftTemplate template) {
        if (!shiftTemplateService.getTemplate(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        template.setId(id);
        return ResponseEntity.ok(shiftTemplateService.saveTemplate(template));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        if (!shiftTemplateService.getTemplate(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        shiftTemplateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
