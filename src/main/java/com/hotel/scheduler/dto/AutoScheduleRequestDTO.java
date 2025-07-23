package com.hotel.scheduler.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for auto-schedule request: specifies department, date range, and optional preferences.
 */
@Data
public class AutoScheduleRequestDTO {
    private Long departmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    // Optionally, add fields for preferences, excluded employees, etc.
}
