package com.hotel.scheduler.dto.shift;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateShiftRequest {
    
    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]", shape = JsonFormat.Shape.STRING)
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]", shape = JsonFormat.Shape.STRING)
    private LocalDateTime endTime;
    
    private Long employeeId;
    
    @NotNull(message = "Department is required")
    private Long departmentId;
    
    private String notes;
}
