package com.gokul.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LeaveBalanceResponse {

    private Long employeeId;

    private String employeeName;

    private Integer annualEntitlement;

    private Integer usedDays;

    private Integer remainingDays;
}