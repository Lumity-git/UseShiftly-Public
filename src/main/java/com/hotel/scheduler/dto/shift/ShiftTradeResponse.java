
package com.hotel.scheduler.dto.shift;

import lombok.Data;
import java.time.OffsetDateTime;

/**
 * ShiftTradeResponse: Data Transfer Object (DTO) for representing shift trade information in API responses.
 *
 * Usage:
 * - Use this DTO to serialize shift trade data for frontend consumption.
 * - Provides a static mapper method to convert a ShiftTrade entity to a DTO instance.
 * - Includes all relevant fields for displaying trade details, employee info, and shift metadata.
 *
 * Fields:
 * - id: Trade ID
 * - shiftId: Associated shift ID
 * - startTime, endTime: Shift start/end times
 * - departmentName: Department name for the shift
 * - position: Position or fallback to department name
 * - requestingEmployeeId, requestingEmployeeName: Employee who requested the trade
 * - pickupEmployeeId, pickupEmployeeName: Employee who picked up the trade
 * - status: Trade status (enum as string)
 * - requestedAt: When the trade was requested
 */
@Data
public class ShiftTradeResponse {
    /**
     * Maps a ShiftTrade entity to a ShiftTradeResponse DTO.
     * Handles nulls and fallbacks for missing data.
     * @param trade ShiftTrade entity
     * @return ShiftTradeResponse DTO
     */
    public static ShiftTradeResponse fromEntity(com.hotel.scheduler.model.ShiftTrade trade) {
        ShiftTradeResponse dto = new ShiftTradeResponse();
        dto.setId(trade.getId());
        dto.setStatus(trade.getStatus().name());
        if (trade.getShift() != null) {
            dto.setShiftId(trade.getShift().getId());
            dto.setStartTime(trade.getShift().getStartTime());
            dto.setEndTime(trade.getShift().getEndTime());
            if (trade.getShift().getDepartment() != null) {
                dto.setDepartmentName(trade.getShift().getDepartment().getName());
            }
            // If getPosition() exists on Shift, map it; otherwise, use department name as fallback
            // dto.setPosition(trade.getShift().getPosition());
            if (trade.getShift().getDepartment() != null) {
                dto.setPosition(trade.getShift().getDepartment().getName());
            } else {
                dto.setPosition("General Staff");
            }
        }
        if (trade.getRequestingEmployee() != null) {
            dto.setRequestingEmployeeId(trade.getRequestingEmployee().getId());
            dto.setRequestingEmployeeName(trade.getRequestingEmployee().getFirstName() + " " + trade.getRequestingEmployee().getLastName());
        } else if (trade.getPickupEmployee() != null) {
            // Fallback: use pickup employee name if requesting employee is missing
            dto.setRequestingEmployeeName(trade.getPickupEmployee().getFirstName() + " " + trade.getPickupEmployee().getLastName());
        } else {
            dto.setRequestingEmployeeName("Unknown");
        }
        if (trade.getPickupEmployee() != null) {
            dto.setPickupEmployeeId(trade.getPickupEmployee().getId());
            dto.setPickupEmployeeName(trade.getPickupEmployee().getFirstName() + " " + trade.getPickupEmployee().getLastName());
        }
        // Map requestedAt if available
        try {
            java.lang.reflect.Method m = trade.getClass().getMethod("getRequestedAt");
            Object requestedAt = m.invoke(trade);
            if (requestedAt != null && requestedAt instanceof java.time.OffsetDateTime) {
                dto.setRequestedAt((java.time.OffsetDateTime) requestedAt);
            }
        } catch (Exception ignore) {}
        return dto;
    }

    private Long id;
    private Long shiftId;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private String departmentName;
    private String position;
    private Long requestingEmployeeId;
    private String requestingEmployeeName;
    private Long pickupEmployeeId;
    private String pickupEmployeeName;
    private String status;
    private OffsetDateTime requestedAt;
}
