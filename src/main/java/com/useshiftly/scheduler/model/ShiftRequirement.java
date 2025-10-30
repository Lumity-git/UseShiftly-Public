package com.useshiftly.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * ShiftRequirement: Defines a required shift for a department on a specific date.
 * Used by the auto-scheduling system to determine which shifts need to be filled.
 */
@Entity
@Table(name = "shift_requirements")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRequirement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Department for which the shift is required. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /** Date of the required shift. */
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    /** Shift start time. */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /** Shift end time. */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** Number of employees required for this shift. */
    @Column(name = "required_employees", nullable = false)
    private int requiredEmployees;

    /** Optional notes for the shift requirement. */
    @Column
    private String notes;
}
