package com.hotel.scheduler.dto.shift;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShiftTradeResponse {
    private Long id;
    private Long shiftId;
    private Long requestingEmployeeId;
    private String requestingEmployeeName;
    private Long pickupEmployeeId;
    private String pickupEmployeeName;
    private String status;
    private LocalDateTime requestedAt;
}
