package com.hotel.scheduler.dto;
import lombok.Data;
import java.util.List;

@Data
public class ShiftDistributionDTO {
    private List<String> labels;
    private List<Integer> data;
}
