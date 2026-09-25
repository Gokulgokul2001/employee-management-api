package com.gokul.employee.controller;

import com.gokul.employee.dto.LeaveBalanceResponse;
import com.gokul.employee.entity.LeaveBalance;
import com.gokul.employee.service.LeaveBalanceService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-balances")
public class LeaveBalanceController {

    private final LeaveBalanceService leaveBalanceService;

    public LeaveBalanceController(
            LeaveBalanceService leaveBalanceService) {
        this.leaveBalanceService = leaveBalanceService;
    }

    // Initialize leave balance for an employee
    @PostMapping("/initialize/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<LeaveBalanceResponse> initializeLeaveBalance(
            @PathVariable Long employeeId) {

        leaveBalanceService.initializeLeaveBalance(employeeId);

        return ResponseEntity.ok(
                leaveBalanceService.getLeaveBalanceResponse(employeeId)
        );
    }

    // Get a selected employee's leave balance (Admin/HR)
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<LeaveBalanceResponse> getLeaveBalance(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                leaveBalanceService.getLeaveBalanceResponse(employeeId)
        );
    }

    // Get the logged-in employee's own leave balance
    @GetMapping("/my-balance")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<LeaveBalanceResponse> getMyLeaveBalance(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();

        return ResponseEntity.ok(
                leaveBalanceService.getLeaveBalanceByEmail(email)
        );
    }
}