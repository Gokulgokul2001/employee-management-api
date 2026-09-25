package com.gokul.employee.service;

import com.gokul.employee.dto.LeaveRequest;
import com.gokul.employee.dto.LeaveResponse;
import com.gokul.employee.dto.LeaveStatusRequest;

import com.gokul.employee.entity.Employee;
import com.gokul.employee.entity.Leave;
import com.gokul.employee.entity.LeaveBalance;

import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.exception.LeaveNotFoundException;

import com.gokul.employee.repository.EmployeeRepository;
import com.gokul.employee.repository.LeaveRepository;
import com.gokul.employee.repository.LeaveBalanceRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    public LeaveService(
            LeaveRepository leaveRepository,
            EmployeeRepository employeeRepository,
            LeaveBalanceRepository leaveBalanceRepository) {

        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.leaveBalanceRepository = leaveBalanceRepository;
    }

    // Get authenticated user's email from JWT
    private String getAuthenticatedEmail() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new AccessDeniedException(
                    "User is not authenticated");
        }

        return authentication.getName();
    }

    // Get employee associated with authenticated user
    private Employee getAuthenticatedEmployee() {

        String email = getAuthenticatedEmail();

        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found for email: " + email));
    }

    // Ensure only EMPLOYEE role can create, edit, or delete
    private void requireEmployeeRole() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "User is not authenticated");
        }

        boolean isEmployee = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_EMPLOYEE"));

        if (!isEmployee) {
            throw new AccessDeniedException(
                    "Only employees can perform this operation");
        }
    }

    // Ensure only HR or ADMIN can view all leaves or update status
    private void requireHrOrAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new AccessDeniedException(
                    "User is not authenticated");
        }

        boolean authorized = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                                || authority.getAuthority().equals("ROLE_HR"));

        if (!authorized) {
            throw new AccessDeniedException(
                    "Only HR or Admin can perform this operation");
        }
    }

    // Verify leave belongs to authenticated employee
    private void validateOwnership(Leave leave) {

        Employee employee = getAuthenticatedEmployee();

        if (!leave.getEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException(
                    "You are not authorized to access this leave request");
        }
    }

    // Create leave request
    @Transactional
    public LeaveResponse createLeave(LeaveRequest request) {

        requireEmployeeRole();

        Employee employee = getAuthenticatedEmployee();

        validateLeaveDates(
                request.getStartDate(),
                request.getEndDate());

        validateNoOverlappingLeave(
                employee.getId(),
                request.getStartDate(),
                request.getEndDate(),
                null);

        Leave leave = Leave.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .leaveType(request.getLeaveType())
                .status("PENDING")
                .reason(request.getReason())
                .build();

        return mapToResponse(leaveRepository.save(leave));
    }

    // Get all leave requests - HR/Admin only
    public List<LeaveResponse> getAllLeaves() {

        requireHrOrAdmin();

        return leaveRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get leave requests by employee ID - HR/Admin only
    public List<LeaveResponse> getLeavesByEmployeeId(Long employeeId) {

        requireHrOrAdmin();

        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with id: " + employeeId));

        return leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get leave requests by status - HR/Admin only
    public List<LeaveResponse> getLeavesByStatus(String status) {

        requireHrOrAdmin();

        return leaveRepository.findByStatus(status.toUpperCase())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Employee views own leave requests
    public List<LeaveResponse> getMyLeaves() {

        requireEmployeeRole();

        Employee employee = getAuthenticatedEmployee();

        return leaveRepository.findByEmployeeId(employee.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Update own PENDING leave request only
    @Transactional
    public LeaveResponse updateLeave(
            Long id,
            LeaveRequest request) {

        requireEmployeeRole();

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(
                        "Leave not found with id: " + id));

        validateOwnership(leave);

        if (!"PENDING".equals(leave.getStatus())) {
            throw new IllegalArgumentException(
                    "Only PENDING leave requests can be edited");
        }

        validateLeaveDates(
                request.getStartDate(),
                request.getEndDate());

        Employee employee = getAuthenticatedEmployee();

        validateNoOverlappingLeave(
                employee.getId(),
                request.getStartDate(),
                request.getEndDate(),
                leave.getId());

        // Employee ownership cannot be changed
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());

        return mapToResponse(leaveRepository.save(leave));
    }

    // Delete own PENDING leave request only
    @Transactional
    public void deleteLeave(Long id) {

        requireEmployeeRole();

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(
                        "Leave not found with id: " + id));

        validateOwnership(leave);

        if (!"PENDING".equals(leave.getStatus())) {
            throw new IllegalArgumentException(
                    "Only PENDING leave requests can be deleted");
        }

        leaveRepository.delete(leave);
    }

    // Approve or reject leave - HR/Admin only
    @Transactional
    public LeaveResponse updateLeaveStatus(
            Long id,
            LeaveStatusRequest request) {

        requireHrOrAdmin();

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveNotFoundException(
                        "Leave not found with id: " + id));

        String newStatus = request.getStatus()
                .trim()
                .toUpperCase();

        if (!newStatus.equals("APPROVED")
                && !newStatus.equals("REJECTED")) {

            throw new IllegalArgumentException(
                    "Status must be APPROVED or REJECTED");
        }

        String currentStatus = leave.getStatus();

        // Prevent duplicate balance deduction/restoration
        if (currentStatus.equals(newStatus)) {
            return mapToResponse(leave);
        }

        // Prevent reprocessing a rejected leave
        if ("REJECTED".equals(currentStatus)) {
            throw new IllegalArgumentException(
                    "Rejected leave requests cannot be modified");
        }

        // Approve leave and deduct balance
        if ("APPROVED".equals(newStatus)) {

            validateNoOverlappingLeave(
                    leave.getEmployee().getId(),
                    leave.getStartDate(),
                    leave.getEndDate(),
                    leave.getId());

            updateBalance(leave, true);
        }

        // FIX: Reject previously approved leave and restore balance
        if ("REJECTED".equals(newStatus)
                && "APPROVED".equals(currentStatus)) {

            updateBalance(leave, false);
        }

        leave.setStatus(newStatus);

        return mapToResponse(leaveRepository.save(leave));
    }

    // Validate leave dates
    private void validateLeaveDates(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date");
        }
    }

    // Validate overlapping PENDING or APPROVED leave requests
    private void validateNoOverlappingLeave(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Long excludeLeaveId) {

        List<String> statuses = List.of("PENDING", "APPROVED");

        boolean hasOverlap;

        if (excludeLeaveId == null) {

            hasOverlap = leaveRepository.existsOverlappingLeave(
                    employeeId,
                    statuses,
                    startDate,
                    endDate);

        } else {

            hasOverlap =
                    leaveRepository.existsOverlappingLeaveExcludingId(
                            employeeId,
                            statuses,
                            startDate,
                            endDate,
                            excludeLeaveId);
        }

        if (hasOverlap) {
            throw new IllegalArgumentException(
                    "You already have a pending or approved leave "
                            + "request for the selected dates.");
        }
    }

    // Calculate working days (Monday-Friday)
    private int calculateWorkingDays(
            LocalDate startDate,
            LocalDate endDate) {

        int workingDays = 0;

        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {

            DayOfWeek day = date.getDayOfWeek();

            if (day != DayOfWeek.SATURDAY
                    && day != DayOfWeek.SUNDAY) {

                workingDays++;
            }

            date = date.plusDays(1);
        }

        return workingDays;
    }

    // Update employee leave balance
    private void updateBalance(Leave leave, boolean deduct) {

        Long employeeId = leave.getEmployee().getId();

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeId(employeeId)
                .orElseGet(() -> createInitialBalance(employeeId));

        int workingDays = calculateWorkingDays(
                leave.getStartDate(),
                leave.getEndDate());

        if (deduct) {

            if (balance.getRemainingDays() < workingDays) {
                throw new IllegalArgumentException(
                        "Insufficient leave balance. Remaining days: "
                                + balance.getRemainingDays()
                                + ", requested days: "
                                + workingDays);
            }

            balance.setUsedDays(
                    balance.getUsedDays() + workingDays);

            balance.setRemainingDays(
                    balance.getRemainingDays() - workingDays);

        } else {

            // Restore deducted leave days
            balance.setUsedDays(
                    Math.max(0, balance.getUsedDays() - workingDays));

            balance.setRemainingDays(
                    Math.min(
                            balance.getAnnualEntitlement(),
                            balance.getRemainingDays() + workingDays));
        }

        leaveBalanceRepository.save(balance);
    }

    // Initialize balance if it does not exist
    private LeaveBalance createInitialBalance(Long employeeId) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with id: " + employeeId));

        LeaveBalance balance = LeaveBalance.builder()
                .employee(employee)
                .annualEntitlement(30)
                .usedDays(0)
                .remainingDays(30)
                .build();

        return leaveBalanceRepository.save(balance);
    }

    // Map entity to response DTO
    private LeaveResponse mapToResponse(Leave leave) {

        return new LeaveResponse(
                leave.getId(),
                leave.getEmployee().getId(),
                leave.getEmployee().getFirstName()
                        + " "
                        + leave.getEmployee().getLastName(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getLeaveType(),
                leave.getStatus(),
                leave.getReason()
        );
    }
}