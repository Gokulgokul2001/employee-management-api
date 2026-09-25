package com.gokul.employee.repository;

import com.gokul.employee.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaveBalanceRepository
        extends JpaRepository<LeaveBalance, Long> {

    Optional<LeaveBalance> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeId(Long employeeId);
}