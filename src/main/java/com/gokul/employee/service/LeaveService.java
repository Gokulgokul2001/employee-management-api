package com.gokul.employee.service;

import com.gokul.employee.dto.LeaveRequest;
import com.gokul.employee.dto.LeaveResponse;
import com.gokul.employee.dto.LeaveStatusRequest;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.entity.Leave;
import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.exception.LeaveNotFoundException;
import com.gokul.employee.repository.EmployeeRepository;
import com.gokul.employee.repository.LeaveRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveService {
    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveService(
            LeaveRepository leaveRepository,
            EmployeeRepository employeeRepository){
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
    }

    public LeaveResponse createLeave(LeaveRequest request){
        Employee employee = employeeRepository.findById(
                request.getEmployeeId())
                .orElseThrow(()-> new EmployeeNotFoundException(
                        "Employee not found for the id: " + request.getEmployeeId()
                ));
        Leave leave = Leave.builder()
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .leaveType(request.getLeaveType())
                .status("PENDING")
                .reason(request.getReason())
                .build();

        Leave savedLeave = leaveRepository.save(leave);
        return mapToResponse(savedLeave);
    }

    public List<LeaveResponse> getAllLeaves(){
        return leaveRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<LeaveResponse> getLeavesByEmployeeId(
            Long employeeId){
        employeeRepository.findById(employeeId)
                .orElseThrow(()->
                        new EmployeeNotFoundException(
                                "Employee not found with the id: " + employeeId
                        ));
        return leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<LeaveResponse> getLeavesByStatus(String status){
        return leaveRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LeaveResponse mapToResponse(Leave leave){
        return new LeaveResponse(
                leave.getId(),
                leave.getEmployee().getId(),
                leave.getEmployee().getFirstName()
                + ""
                +leave.getEmployee().getLastName(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getLeaveType(),
                leave.getStatus(),
                leave.getReason()
        );
    }

    public LeaveResponse updateLeave(
            Long id,
            LeaveRequest request){
        Leave leave = leaveRepository.findById(id)
                .orElseThrow(()->
                new LeaveNotFoundException("Leave not found for the id: " + id));
        Employee employee = employeeRepository.findById(
                request.getEmployeeId()
        ).orElseThrow(()->
                new EmployeeNotFoundException("Employee not found for the id: " +
                        request.getEmployeeId()));

        leave.setEmployee(employee);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setLeaveType(request.getLeaveType());
        leave.setReason(request.getReason());

        Leave updatedLeave =
                leaveRepository.save(leave);

        return mapToResponse(updatedLeave);
    }
    public void deleteLeave(Long id) {

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(() ->
                        new LeaveNotFoundException(
                                "Leave not found with id: " + id
                        ));

        leaveRepository.delete(leave);
    }

    public LeaveResponse updateLeaveStatus(
            Long id,
            LeaveStatusRequest request){

        Leave leave = leaveRepository.findById(id)
                .orElseThrow(()->
                        new LeaveNotFoundException(
                                "Leave not found with id: " + id
                        ));
        String status = request.getStatus().toUpperCase();

        if (!status.equals("APPROVED")
        && !status.equals("REJECTED")){
            throw new IllegalArgumentException(
                    "Status must be APPROVED or REJECTED"
            );
        }
        leave.setStatus(status);

        Leave updateLeave = leaveRepository.save(leave);
        return mapToResponse(updateLeave);
    }
}
