package com.useshiftly.scheduler.dto;
import lombok.Data;
import java.util.List;

@Data
public class MonthlyTrendDTO {
    private List<String> labels;
    private List<Integer> data;
}
