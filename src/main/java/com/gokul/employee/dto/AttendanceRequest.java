package com.gokul.employee.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRequest {

    @NotNull(message = "Employee Id is required")
    private Long employeeId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    private String status;

    private LocalTime checkIn;

    private LocalTime checkOut;
}