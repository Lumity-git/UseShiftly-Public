package com.hotel.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shift_trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftTrade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_employee_id", nullable = false)
    private Employee requestingEmployee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_employee_id")
    private Employee pickupEmployee;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status = TradeStatus.PENDING;
    
    @Column
    private String reason;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "approved_by_manager_id")
    private Long approvedByManagerId;
    
    public enum TradeStatus {
        PENDING, PICKED_UP, CANCELLED, APPROVED, REJECTED, POSTED_TO_EVERYONE
    }
}
