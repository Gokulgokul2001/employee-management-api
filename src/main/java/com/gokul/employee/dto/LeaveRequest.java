package com.gokul.employee.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequest {
    @NotNull(message = "Employee Id is required")
    private Long employeeId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Leave type is required")
    @Size(max = 50, message = "Leave type must not exceed 50 characters")
    private String leaveType;

    @NotBlank(message = "Leave reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
