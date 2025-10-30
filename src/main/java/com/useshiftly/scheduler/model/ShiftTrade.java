
package com.useshiftly.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.OffsetDateTime;

/**
 * ShiftTrade: JPA entity representing a shift trade or transfer request between employees.
 *
 * Usage:
 * - Used to track the lifecycle of a shift trade, including offer, pickup, approval, and completion.
 * - Supports both direct offers and public postings for shift pickup.
 * - Status transitions are managed via the TradeStatus enum.
 *
 * Fields:
 * - id: Primary key
 * - shift: The shift being traded
 * - requestingEmployee: Employee offering the shift
 * - pickupEmployee: Employee picking up the shift (nullable until accepted)
 * - status: Current status of the trade (enum)
 * - reason: Optional reason for trade
 * - requestedAt: When the trade was requested
 * - completedAt: When the trade was completed (if applicable)
 * - approvedByManagerId: Manager/admin who approved the trade (nullable)
 *
 * JPA/Hibernate:
 * - Table: shift_trades
 * - Uses Lombok for boilerplate reduction
 */
@Entity
@Table(name = "shift_trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftTrade {
    /** Primary key for the shift trade. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The shift being traded. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    /** Employee offering the shift. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_employee_id", nullable = false)
    private Employee requestingEmployee;

    /** Employee picking up the shift (nullable until accepted). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_employee_id")
    private Employee pickupEmployee;

    /** Current status of the trade. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status = TradeStatus.PENDING;

    /** Optional reason for the trade. */
    @Column
    private String reason;

    /** Timestamp when the trade was requested. */
    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt = OffsetDateTime.now();

    /** Timestamp when the trade was completed (if applicable). */
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    /** Manager/admin who approved the trade (nullable). */
    @Column(name = "approved_by_manager_id")
    private Long approvedByManagerId;

    /**
     * Enum for trade status values.
     * PENDING: Awaiting pickup/acceptance
     * PENDING_APPROVAL: Awaiting manager/admin approval
     * PICKED_UP: Trade completed and shift picked up
     * CANCELLED: Trade cancelled or declined
     * APPROVED: Trade approved by manager/admin
     * REJECTED: Trade rejected by manager/admin
     * POSTED_TO_EVERYONE: Publicly posted for any employee to pick up
     */
    public enum TradeStatus {
        PENDING, PENDING_APPROVAL, PICKED_UP, CANCELLED, APPROVED, REJECTED, POSTED_TO_EVERYONE
    }
}
