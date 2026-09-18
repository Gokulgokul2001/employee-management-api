package com.gokul.employee.service;

import com.gokul.employee.dto.AttendanceRequest;
import com.gokul.employee.dto.AttendanceResponse;
import com.gokul.employee.entity.Attendance;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.exception.AttendanceNotFoundException;
import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.repository.AttendanceRepository;
import com.gokul.employee.repository.EmployeeRepository;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            EmployeeRepository employeeRepository
    ){
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    public AttendanceResponse createAttendance(
            AttendanceRequest request){
        Employee employee = employeeRepository
                .findById(request.getEmployeeId())
                .orElseThrow(()-> new EmployeeNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()
                ));
        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .build();
        Attendance savedAttendance = attendanceRepository.save(attendance);
        return mapToResponse(savedAttendance);
    }

    public List<AttendanceResponse> getAllAttendance(){
        return attendanceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    private AttendanceResponse mapToResponse(Attendance attendance){
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getEmployee().getId(),
                attendance.getEmployee().getFirstName()
                + ""
                +attendance.getEmployee().getLastName(),
                attendance.getAttendanceDate(),
                attendance.getStatus(),
                attendance.getCheckIn(),
                attendance.getCheckOut()
        );
    }
    public List<AttendanceResponse> getAttendanceByEmployeeId(
            Long employeeId) {

        employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with id: " + employeeId
                        ));

        return attendanceRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AttendanceResponse> getAttendanceByDate(
            LocalDate attendanceDate) {

        return attendanceRepository
                .findByAttendanceDate(attendanceDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AttendanceResponse updateAttendance(
            Long id,
            AttendanceRequest request){

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(()-> new AttendanceNotFoundException(
                        "Attendance not found with id: " + id
                ));
        Employee employee = employeeRepository.findById(
                request.getEmployeeId()
        ).orElseThrow(()->
                new EmployeeNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()
                ));
        attendance.setEmployee(employee);
        attendance.setAttendanceDate(request.getAttendanceDate());
        attendance.setStatus(request.getStatus());
        attendance.setCheckIn(request.getCheckIn());
        attendance.setCheckOut(request.getCheckOut());

        Attendance updateAttendance =
                attendanceRepository.save(attendance);
        return mapToResponse(updateAttendance);
    }

    public void deleteAttendance(Long id){
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(()-> new AttendanceNotFoundException(
                        "Attendance not found with the id: " + id
                ));
        attendanceRepository.delete(attendance);
    }
}
