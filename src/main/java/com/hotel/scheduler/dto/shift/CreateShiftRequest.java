package com.hotel.scheduler.dto.shift;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CreateShiftRequest {
    
    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;

    @NotNull(message = "End time is required")
    private OffsetDateTime endTime;
    
    private Long employeeId;
    
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    private String notes;
}
