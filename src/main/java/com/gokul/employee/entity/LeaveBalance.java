package com.gokul.employee.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "leave_balances",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "employee_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            unique = true
    )
    private Employee employee;

    @Column(nullable = false)
    private Integer annualEntitlement;

    @Column(nullable = false)
    @Builder.Default
    private Integer usedDays = 0;

    @Column(nullable = false)
    private Integer remainingDays;
}