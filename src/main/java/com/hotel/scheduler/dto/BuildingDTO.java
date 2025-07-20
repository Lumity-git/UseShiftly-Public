package com.hotel.scheduler.dto;

import lombok.Data;
import java.util.List;

@Data
public class BuildingDTO {
    private Long id;
    private String name;
    private Long adminId;
    private Long managerId;
    private List<Long> employeeIds;
}
