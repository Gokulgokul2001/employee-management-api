package com.gokul.employee.controller;

import com.gokul.employee.dto.AttendanceRequest;
import com.gokul.employee.dto.AttendanceResponse;
import com.gokul.employee.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService){
        this.attendanceService = attendanceService;
    }

    @Operation(
            summary = "Create attendance",
            description = "Creates an attendance record for an employee. Requires ADMIN or HR role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping
    public ResponseEntity<AttendanceResponse> createAttendance(
            @Valid @RequestBody AttendanceRequest request){
        AttendanceResponse response =
                attendanceService.createAttendance(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get all attendance records",
            description = "Returns all attendance records."
    )
    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAllAttendance(){
       List<AttendanceResponse> attendance =
               attendanceService.getAllAttendance();
        return ResponseEntity.ok(attendance);
    }

    @Operation(
            summary = "Get attendance by employee",
            description = "Returns attendance records for a specific employee."
    )
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByEmployeeId(
            @PathVariable Long employeeId){
        List<AttendanceResponse> attendance =
                attendanceService.getAttendanceByEmployeeId(employeeId);
        return ResponseEntity.ok(attendance);
    }

    @Operation(
            summary = "Get attendance by date",
            description = "Returns attendance records for a specific date."
    )
    @GetMapping("/date/{attendanceDate}")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByDate(
            @PathVariable LocalDate attendanceDate) {

        List<AttendanceResponse> attendance =
                attendanceService.getAttendanceByDate(attendanceDate);

        return ResponseEntity.ok(attendance);
    }

    @Operation(
            summary = "Update attendance",
            description = "Updates an existing attendance record. Requires ADMIN or HR role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request){
        AttendanceResponse response =
                attendanceService.updateAttendance(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete attendance",
            description = "Deletes an existing attendance record. Requires ADMIN or HR role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Valid> deleteAttendance(
            @PathVariable Long id){
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}
