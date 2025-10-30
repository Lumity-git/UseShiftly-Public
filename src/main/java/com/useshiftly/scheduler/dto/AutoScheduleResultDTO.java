package com.useshiftly.scheduler.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO for the result of auto-scheduling: contains scheduled shift IDs or details, and any unassigned requirements.
 */
@Data
public class AutoScheduleResultDTO {
    private Long departmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalShiftsScheduled;
    private int totalUnassigned;
    // Optionally, add lists of scheduled shifts and unassigned requirements
}
