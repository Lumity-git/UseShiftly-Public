package com.hotel.scheduler.dto;

import java.time.LocalDate;
import java.util.List;

public class AutoScheduleRequestDTO {
    private Long departmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<AutoScheduleTemplatePairDTO> templatePairs;

    public Long getDepartmentId() {
        return departmentId;
    }
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public List<AutoScheduleTemplatePairDTO> getTemplatePairs() {
        return templatePairs;
    }
    public void setTemplatePairs(List<AutoScheduleTemplatePairDTO> templatePairs) {
        this.templatePairs = templatePairs;
    }
    // Optionally, add fields for preferences, excluded employees, etc.
}
