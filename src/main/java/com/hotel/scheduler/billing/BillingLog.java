package com.hotel.scheduler.billing;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "billing_log")
public class BillingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "billing_period", nullable = false)
    private LocalDate billingPeriod;

    @Column(name = "employee_ids", nullable = false, length = 2048)
    private String employeeIds; // comma-separated

    @Column(name = "billable_users", nullable = false)
    private Integer billableUsers;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
