package com.gokul.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;

    private Long employeeId;

    private String employeeName;

    private LocalDate attendanceDate;

    private String status;

    private LocalTime checkIn;

    private LocalTime checkOut;
}