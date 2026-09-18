package com.gokul.employee.controller;


import com.gokul.employee.dto.LeaveRequest;
import com.gokul.employee.dto.LeaveResponse;
import com.gokul.employee.dto.LeaveStatusRequest;
import com.gokul.employee.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.PrePersist;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@SecurityRequirement(name = "bearerAuth")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService){
        this.leaveService = leaveService;
    }

    @Operation(
            summary = "Create a leave request",
            description = "Creates a new leave request with PENDING status."
    )
    @PostMapping
    public ResponseEntity<LeaveResponse> createLeave(
            @Valid @RequestBody LeaveRequest request){
        LeaveResponse response = leaveService.createLeave(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get all leave requests",
            description = "Returns all leave requests."
    )
    @GetMapping
    public ResponseEntity<List<LeaveResponse>> getAllLeaves(){
        List<LeaveResponse> leaves = leaveService.getAllLeaves();
        return ResponseEntity.ok(leaves);
    }

    @Operation(
            summary = "Get leaves by employee",
            description = "Returns all leave requests for a specific employee."
    )
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveResponse>> getLeavesByEmployeeId(
            @PathVariable Long employeeId){
        List<LeaveResponse> leaves =
                leaveService.getLeavesByEmployeeId(employeeId);
        return ResponseEntity.ok(leaves);
    }

    @Operation(
            summary = "Get leaves by status",
            description = "Returns leave requests filtered by their status."
    )
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LeaveResponse>> getLeavesByStatus(
            @PathVariable String status){
    List<LeaveResponse> leaves =
            leaveService.getLeavesByStatus(status);
    return ResponseEntity.ok(leaves);
    }

    @Operation(
            summary = "Update a leave request",
            description = "Updates an existing leave request."
    )
    @PutMapping("/{id}")
    public ResponseEntity<LeaveResponse> updateLeave(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequest request) {

        LeaveResponse response =
                leaveService.updateLeave(id, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a leave request",
            description = "Deletes an existing leave request."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeave(
            @PathVariable Long id) {

        leaveService.deleteLeave(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Update leave status",
            description = "Approves or rejects a leave request. Requires ADMIN or HR role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeaveResponse> updateLeaveStatus(
            @PathVariable Long id,
            @Valid @RequestBody LeaveStatusRequest request){

        LeaveResponse response =
                leaveService.updateLeaveStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
