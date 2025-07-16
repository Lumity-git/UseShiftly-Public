package com.hotel.scheduler.dto.shift;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ShiftResponse {
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", shape = JsonFormat.Shape.STRING)
    private OffsetDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", shape = JsonFormat.Shape.STRING)
    private OffsetDateTime endTime;
    
    private String employeeName;
    private String employeeEmail;
    private Long employeeId;
    
    private String departmentName;
    private Long departmentId;
    
    private String notes;
    private String status;
    private Boolean availableForPickup;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", shape = JsonFormat.Shape.STRING)
    private OffsetDateTime createdAt;
    
    private String createdByName;
}
