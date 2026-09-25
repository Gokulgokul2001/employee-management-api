package com.gokul.employee.service;

import com.gokul.employee.dto.AttendanceRequest;
import com.gokul.employee.dto.AttendanceResponse;
import com.gokul.employee.entity.Attendance;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.repository.AttendanceRepository;
import com.gokul.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;


    // =========================================================
    // CREATE ATTENDANCE - HR / ADMIN
    // =========================================================

    public AttendanceResponse createAttendance(AttendanceRequest request) {

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with id: "
                                        + request.getEmployeeId()
                        ));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .build();

        Attendance savedAttendance =
                attendanceRepository.save(attendance);

        return mapToResponse(savedAttendance);
    }


    // =========================================================
    // EMPLOYEE CHECK-IN
    // =========================================================

    public AttendanceResponse checkIn(String email) {

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with email: "
                                        + email
                        ));

        LocalDate today = LocalDate.now();

        // Prevent duplicate attendance for the same day
        if (attendanceRepository
                .findByEmployeeIdAndAttendanceDate(
                        employee.getId(),
                        today
                )
                .isPresent()) {

            throw new IllegalStateException(
                    "Attendance already marked for today"
            );
        }

        LocalTime currentTime = LocalTime.now();

        // Attendance status
        String status;

        if (currentTime.isAfter(LocalTime.of(9, 30))) {
            status = "LATE";
        } else {
            status = "PRESENT";
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(today)
                .status(status)
                .checkIn(currentTime)
                .build();

        Attendance savedAttendance =
                attendanceRepository.save(attendance);

        return mapToResponse(savedAttendance);
    }


    // =========================================================
    // EMPLOYEE CHECK-OUT
    // =========================================================

    public AttendanceResponse checkOut(String email) {

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with email: "
                                        + email
                        ));

        LocalDate today = LocalDate.now();

        Attendance attendance =
                attendanceRepository
                        .findByEmployeeIdAndAttendanceDate(
                                employee.getId(),
                                today
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Please check-in before checking out"
                                ));

        // Check if employee has already checked in
        if (attendance.getCheckIn() == null) {
            throw new IllegalStateException(
                    "Please check-in before checking out"
            );
        }

        // Prevent duplicate checkout
        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException(
                    "Attendance already checked out for today"
            );
        }

        attendance.setCheckOut(LocalTime.now());

        Attendance updatedAttendance =
                attendanceRepository.save(attendance);

        return mapToResponse(updatedAttendance);
    }


    // =========================================================
    // GET MY ATTENDANCE
    // =========================================================

    public List<AttendanceResponse> getMyAttendance(String email) {

        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with email: "
                                        + email
                        ));

        return attendanceRepository
                .findByEmployeeId(employee.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET ALL ATTENDANCE - HR / ADMIN
    // =========================================================

    public List<AttendanceResponse> getAllAttendance() {

        return attendanceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // GET ATTENDANCE BY EMPLOYEE - HR / ADMIN
    // =========================================================
    public List<AttendanceResponse> getAttendanceByEmployeeId(
            Long employeeId) {

        return attendanceRepository
                .findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // GET ATTENDANCE BY DATE - HR / ADMIN
    // =========================================================

    public List<AttendanceResponse> getAttendanceByDate(
            LocalDate attendanceDate) {

        return attendanceRepository
                .findByAttendanceDate(attendanceDate)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    // =========================================================
    // UPDATE ATTENDANCE - HR / ADMIN
    // =========================================================

    public AttendanceResponse updateAttendance(Long id, AttendanceRequest request) {

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Attendance not found with id: " + id));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()));

        attendance.setEmployee(employee);
        attendance.setAttendanceDate(request.getAttendanceDate());

        LocalTime checkIn = request.getCheckIn();
        LocalTime checkOut = request.getCheckOut();

        /*
         * Determine attendance status automatically
         * based on check-in time.
         */
        String status;

        if ("ABSENT".equalsIgnoreCase(request.getStatus())) {

            status = "ABSENT";

        } else if (checkIn != null) {

            if (checkIn.isAfter(LocalTime.of(9, 30))) {
                status = "LATE";
            } else {
                status = "PRESENT";
            }

        } else {

            status = request.getStatus();
        }

        attendance.setStatus(status);
        attendance.setCheckIn(checkIn);
        attendance.setCheckOut(checkOut);

        Attendance updatedAttendance = attendanceRepository.save(attendance);

        return mapToResponse(updatedAttendance);
    }

    // =========================================================
    // DELETE ATTENDANCE - HR / ADMIN
    // =========================================================

    public void deleteAttendance(Long id) {

        if (!attendanceRepository.existsById(id)) {
            throw new IllegalStateException(
                    "Attendance not found with id: " + id
            );
        }

        attendanceRepository.deleteById(id);
    }


    // =========================================================
    // ENTITY -> RESPONSE
    // =========================================================

    private AttendanceResponse mapToResponse(
            Attendance attendance) {

        Employee employee = attendance.getEmployee();

        String employeeName =
                employee.getFirstName()
                        + " "
                        + employee.getLastName();

        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(employee.getId())
                .employeeName(employeeName)
                .attendanceDate(
                        attendance.getAttendanceDate()
                )
                .status(
                        attendance.getStatus()
                )
                .checkIn(
                        attendance.getCheckIn()
                )
                .checkOut(
                        attendance.getCheckOut()
                )
                .build();
    }
}