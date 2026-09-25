
package com.gokul.employee.repository;

import com.gokul.employee.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    // Get leave requests by employee ID
    List<Leave> findByEmployeeId(Long employeeId);

    // Get leave requests by status
    List<Leave> findByStatus(String status);

    // Check whether an employee has overlapping active leave
    @Query("""
        SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
        FROM Leave l
        WHERE l.employee.id = :employeeId
          AND l.status IN :statuses
          AND l.startDate <= :endDate
          AND l.endDate >= :startDate
    """)
    boolean existsOverlappingLeave(
            @Param("employeeId") Long employeeId,
            @Param("statuses") List<String> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Check overlapping leave while excluding the current request
    @Query("""
        SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
        FROM Leave l
        WHERE l.employee.id = :employeeId
          AND l.id <> :excludeLeaveId
          AND l.status IN :statuses
          AND l.startDate <= :endDate
          AND l.endDate >= :startDate
    """)
    boolean existsOverlappingLeaveExcludingId(
            @Param("employeeId") Long employeeId,
            @Param("statuses") List<String> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeLeaveId") Long excludeLeaveId
    );
}