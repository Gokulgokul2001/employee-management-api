
package com.gokul.employee.controller;

import com.gokul.employee.dto.LeaveRequest;
import com.gokul.employee.dto.LeaveResponse;
import com.gokul.employee.dto.LeaveStatusRequest;
import com.gokul.employee.service.LeaveService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@SecurityRequirement(name = "bearerAuth")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @Operation(summary = "Create a leave request")
    @PostMapping
    public ResponseEntity<LeaveResponse> createLeave(
            @Valid @RequestBody LeaveRequest request) {

        LeaveResponse response = leaveService.createLeave(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(summary = "Get all leave requests")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getAllLeaves() {

        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    @Operation(summary = "Get leaves by employee ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveResponse>> getLeavesByEmployeeId(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                leaveService.getLeavesByEmployeeId(employeeId));
    }

    @Operation(summary = "Get leaves by status")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LeaveResponse>> getLeavesByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                leaveService.getLeavesByStatus(status));
    }

    @Operation(summary = "Update a leave request")
    @PutMapping("/{id}")
    public ResponseEntity<LeaveResponse> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequest request) {

        return ResponseEntity.ok(
                leaveService.updateLeave(id, request));
    }

    @Operation(summary = "Delete a leave request")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(
            @PathVariable Long id) {

        leaveService.deleteLeave(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Approve or reject a leave request",
            description = "Requires ADMIN or HR role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveResponse> updateLeaveStatus(
            @PathVariable Long id,
            @Valid @RequestBody LeaveStatusRequest request) {

        return ResponseEntity.ok(
                leaveService.updateLeaveStatus(id, request));
    }

    @Operation(summary = "Get my leave requests")
    @GetMapping("/my-leaves")
    public ResponseEntity<List<LeaveResponse>> getMyLeaves() {
        return ResponseEntity.ok(leaveService.getMyLeaves());
    }
}