package com.gokul.employee.service;

import com.gokul.employee.dto.LeaveBalanceResponse;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.entity.LeaveBalance;
import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.repository.EmployeeRepository;
import com.gokul.employee.repository.LeaveBalanceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveBalanceService {

    private static final int DEFAULT_ANNUAL_ENTITLEMENT = 30;

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveBalanceService(
            LeaveBalanceRepository leaveBalanceRepository,
            EmployeeRepository employeeRepository) {

        this.leaveBalanceRepository = leaveBalanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public LeaveBalance initializeLeaveBalance(Long employeeId) {

        return leaveBalanceRepository.findByEmployeeId(employeeId)
                .orElseGet(() -> {

                    Employee employee = employeeRepository.findById(employeeId)
                            .orElseThrow(() ->
                                    new EmployeeNotFoundException(
                                            "Employee not found with id: " + employeeId
                                    )
                            );

                    LeaveBalance balance = LeaveBalance.builder()
                            .employee(employee)
                            .annualEntitlement(DEFAULT_ANNUAL_ENTITLEMENT)
                            .usedDays(0)
                            .remainingDays(DEFAULT_ANNUAL_ENTITLEMENT)
                            .build();

                    return leaveBalanceRepository.save(balance);
                });
    }

    @Transactional(readOnly = true)
    public LeaveBalance getLeaveBalance(Long employeeId) {

        return leaveBalanceRepository.findByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Leave balance not found for employee id: " + employeeId
                        )
                );
    }

    @Transactional(readOnly = true)
    public LeaveBalanceResponse getLeaveBalanceResponse(Long employeeId) {

        LeaveBalance balance = getLeaveBalance(employeeId);

        Employee employee = balance.getEmployee();

        String employeeName = employee.getFirstName()
                + " "
                + employee.getLastName();

        return new LeaveBalanceResponse(
                employee.getId(),
                employeeName,
                balance.getAnnualEntitlement(),
                balance.getUsedDays(),
                balance.getRemainingDays()
        );
    }

    @Transactional(readOnly = true)
    public LeaveBalanceResponse getLeaveBalanceByEmail(String email) {

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with email: " + email
                        )
                );

        return getLeaveBalanceResponse(employee.getId());
    }
}