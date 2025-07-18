package com.hotel.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkEmployeeImportResult {
    private int total;
    private int success;
    private int failed;
    private List<String> errors;
}
