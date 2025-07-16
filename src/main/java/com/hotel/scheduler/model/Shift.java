
package com.hotel.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.OffsetDateTime;

/**
 * Shift: JPA entity representing a scheduled work shift for a hotel employee.
 *
 * Usage:
 * - Represents a single shift assignment, including time, department, and employee.
 * - Used for scheduling, trading, and reporting in the hotel scheduler system.
 * - Supports status transitions (scheduled, completed, cancelled, available for pickup, pending).
 *
 * Fields:
 * - id: Primary key
 * - startTime, endTime: Shift start and end times
 * - employee: Assigned employee (nullable if not assigned)
 * - department: Department for the shift (required)
 * - notes: Optional notes for the shift
 * - status: Current status of the shift (enum)
 * - availableForPickup: True if shift is posted for pickup/trade
 * - createdAt, updatedAt: Audit timestamps
 * - createdBy: Employee who created the shift
 *
 * JPA/Hibernate:
 * - Table: shifts
 * - Uses Lombok for boilerplate reduction
 * - @PreUpdate sets updatedAt timestamp automatically
 */
@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
    /** Primary key for the shift. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Shift start time (required). */
    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    /** Shift end time (required). */
    @Column(name = "end_time", nullable = false)
    private OffsetDateTime endTime;

    /** Assigned employee (nullable if not assigned). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    /** Department for the shift (required). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /** Optional notes for the shift. */
    @Column
    private String notes;

    /** Current status of the shift. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status = ShiftStatus.SCHEDULED;

    /** True if shift is posted for pickup/trade. */
    @Column(name = "is_available_for_pickup")
    private Boolean availableForPickup = false;

    /** Timestamp when the shift was created. */
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Timestamp when the shift was last updated. */
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    /** Employee who created the shift. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private Employee createdBy;

    /**
     * Enum for shift status values.
     * SCHEDULED: Assigned and scheduled
     * COMPLETED: Shift completed
     * CANCELLED: Shift cancelled
     * AVAILABLE_FOR_PICKUP: Posted for pickup/trade
     * PENDING: Pending assignment or trade
     */
    public enum ShiftStatus {
        SCHEDULED, COMPLETED, CANCELLED, AVAILABLE_FOR_PICKUP, PENDING
    }

    /**
     * JPA lifecycle callback to update the updatedAt timestamp before update.
     */
    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
