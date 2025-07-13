package com.hotel.scheduler.dto.shift;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShiftResponse {
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    private String employeeName;
    private String employeeEmail;
    private Long employeeId;
    
    private String departmentName;
    private Long departmentId;
    
    private String notes;
    private String status;
    private Boolean availableForPickup;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    private String createdByName;
}
