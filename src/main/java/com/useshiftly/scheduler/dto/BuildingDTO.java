package com.useshiftly.scheduler.dto;

import lombok.Data;
import java.util.List;

@Data
public class BuildingDTO {
    private Long id;
    private String name;
    private String address;
    private Long adminId;
    private List<Long> managerIds;
    private List<Long> employeeIds;
}
