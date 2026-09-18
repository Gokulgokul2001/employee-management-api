package com.gokul.employee.service;

import com.gokul.employee.dto.AttendanceRequest;
import com.gokul.employee.dto.AttendanceResponse;
import com.gokul.employee.entity.Attendance;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.exception.AttendanceNotFoundException;
import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.repository.AttendanceRepository;
import com.gokul.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        attendanceService = new AttendanceService(
                attendanceRepository,
                employeeRepository
        );
    }

    @Test
    void createAttendance_shouldCreateAttendanceSuccessfully() {

        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .build();

        AttendanceRequest request = new AttendanceRequest(
                1L,
                LocalDate.of(2026, 9, 18),
                "PRESENT",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 18)
        );

        Attendance savedAttendance = Attendance.builder()
                .id(1L)
                .employee(employee)
                .attendanceDate(request.getAttendanceDate())
                .status(request.getStatus())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .build();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(attendanceRepository.save(any(Attendance.class)))
                .thenReturn(savedAttendance);

        AttendanceResponse result =
                attendanceService.createAttendance(request);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("GokulS", result.getEmployeeName());
        assertEquals(LocalDate.of(2026, 9, 18), result.getAttendanceDate());
        assertEquals("PRESENT", result.getStatus());
        assertEquals(
                LocalDate.of(2026, 9, 18),
                result.getCheckIn()
        );

        assertEquals(
                LocalDate.of(2026, 9, 18),
                result.getCheckOut()
        );

        verify(employeeRepository).findById(1L);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void createAttendance_shouldThrowException_whenEmployeeDoesNotExist() {

        AttendanceRequest request = new AttendanceRequest(
                99L,
                LocalDate.of(2026, 9, 18),
                "PRESENT",
                LocalDate.of(2026, 9, 18),
                LocalDate.of(2026, 9, 18)
        );

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> attendanceService.createAttendance(request)
                );

        assertEquals(
                "Employee not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findById(99L);

        verify(attendanceRepository, never())
                .save(any(Attendance.class));
    }

    @Test
    void getAllAttendance_shouldReturnAttendanceList() {

        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .build();

        Attendance attendance1 = Attendance.builder()
                .id(1L)
                .employee(employee)
                .attendanceDate(LocalDate.of(2026, 9, 18))
                .status("PRESENT")
                .checkIn(LocalDate.of(2026, 9, 18))
                .checkOut(LocalDate.of(2026, 9, 18))
                .build();

        Attendance attendance2 = Attendance.builder()
                .id(2L)
                .employee(employee)
                .attendanceDate(LocalDate.of(2026, 9, 19))
                .status("LATE")
                .checkIn(LocalDate.of(2026, 9, 19))
                .checkOut(LocalDate.of(2026, 9, 19))
                .build();

        when(attendanceRepository.findAll())
                .thenReturn(List.of(attendance1, attendance2));

        List<AttendanceResponse> result =
                attendanceService.getAllAttendance();

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("PRESENT", result.get(0).getStatus());

        assertEquals(2L, result.get(1).getId());
        assertEquals("LATE", result.get(1).getStatus());

        verify(attendanceRepository).findAll();
    }
    @Test
    void getAttendanceByEmployeeId_shouldReturnAttendanceList() {

        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .build();

        Attendance attendance = Attendance.builder()
                .id(1L)
                .employee(employee)
                .attendanceDate(LocalDate.of(2026, 9, 18))
                .status("PRESENT")
                .checkIn(LocalDate.of(2026, 9, 18))
                .checkOut(LocalDate.of(2026, 9, 18))
                .build();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(attendanceRepository.findByEmployeeId(1L))
                .thenReturn(List.of(attendance));

        List<AttendanceResponse> result =
                attendanceService.getAttendanceByEmployeeId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getEmployeeId());
        assertEquals("GokulS", result.get(0).getEmployeeName());
        assertEquals("PRESENT", result.get(0).getStatus());

        verify(attendanceRepository).findByEmployeeId(1L);
    }

    @Test
    void getAttendanceByEmployeeId_shouldThrowException_whenEmployeeDoesNotExist() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> attendanceService.getAttendanceByEmployeeId(99L)
                );

        assertEquals(
                "Employee not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findById(99L);

        verify(attendanceRepository, never())
                .findByEmployeeId(99L);
    }

    @Test
    void getAttendanceByDate_shouldReturnAttendanceList() {

        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .build();

        LocalDate attendanceDate = LocalDate.of(2026, 9, 18);

        Attendance attendance = Attendance.builder()
                .id(1L)
                .employee(employee)
                .attendanceDate(attendanceDate)
                .status("PRESENT")
                .checkIn(attendanceDate)
                .checkOut(attendanceDate)
                .build();

        when(attendanceRepository.findByAttendanceDate(attendanceDate))
                .thenReturn(List.of(attendance));

        List<AttendanceResponse> result =
                attendanceService.getAttendanceByDate(attendanceDate);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getEmployeeId());
        assertEquals("GokulS", result.get(0).getEmployeeName());
        assertEquals(attendanceDate, result.get(0).getAttendanceDate());
        assertEquals("PRESENT", result.get(0).getStatus());

        verify(attendanceRepository)
                .findByAttendanceDate(attendanceDate);
    }

    @Test
    void getAttendanceByDate_shouldReturnEmptyList_whenNoAttendanceExists() {

        LocalDate attendanceDate = LocalDate.of(2026, 9, 20);

        when(attendanceRepository.findByAttendanceDate(attendanceDate))
                .thenReturn(List.of());

        List<AttendanceResponse> result =
                attendanceService.getAttendanceByDate(attendanceDate);

        assertEquals(0, result.size());

        verify(attendanceRepository)
                .findByAttendanceDate(attendanceDate);
    }

    @Test
    void updateAttendance_shouldUpdateAndReturnAttendance() {

        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .build();

        Attendance attendance = Attendance.builder()
                .id(1L)
                .employee(employee)
                .attendanceDate(LocalDate.of(2026, 9, 18))
                .status("PRESENT")
                .checkIn(LocalDate.of(2026, 9, 18))
                .checkOut(LocalDate.of(2026, 9, 18))
                .build();

        AttendanceRequest request = new AttendanceRequest(
                1L,
                LocalDate.of(2026, 9, 19),
                "LATE",
                LocalDate.of(2026, 9, 19),
                LocalDate.of(2026, 9, 19)
        );

        when(attendanceRepository.findById(1L))
                .thenReturn(Optional.of(attendance));

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(attendanceRepository.save(any(Attendance.class)))
                .thenReturn(attendance);

        AttendanceResponse result =
                attendanceService.updateAttendance(1L, request);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getEmployeeId());
        assertEquals("LATE", result.getStatus());
        assertEquals(
                LocalDate.of(2026, 9, 19),
                result.getAttendanceDate()
        );

        verify(attendanceRepository).findById(1L);
        verify(employeeRepository).findById(1L);
        verify(attendanceRepository).save(attendance);
    }

    @Test
    void updateAttendance_shouldThrowException_whenAttendanceDoesNotExist() {

        AttendanceRequest request = new AttendanceRequest(
                1L,
                LocalDate.of(2026, 9, 19),
                "PRESENT",
                LocalDate.of(2026, 9, 19),
                LocalDate.of(2026, 9, 19)
        );

        when(attendanceRepository.findById(99L))
                .thenReturn(Optional.empty());

        AttendanceNotFoundException exception =
                assertThrows(
                        AttendanceNotFoundException.class,
                        () -> attendanceService.updateAttendance(99L, request)
                );

        assertEquals(
                "Attendance not found with id: 99",
                exception.getMessage()
        );

        verify(attendanceRepository).findById(99L);

        verify(employeeRepository, never())
                .findById(anyLong());

        verify(attendanceRepository, never())
                .save(any(Attendance.class));
    }

    @Test
    void deleteAttendance_shouldDeleteAttendance_whenAttendanceExists() {

        Attendance attendance = Attendance.builder()
                .id(1L)
                .build();

        when(attendanceRepository.findById(1L))
                .thenReturn(Optional.of(attendance));

        attendanceService.deleteAttendance(1L);

        verify(attendanceRepository).findById(1L);
        verify(attendanceRepository).delete(attendance);
    }

    @Test
    void deleteAttendance_shouldThrowException_whenAttendanceDoesNotExist() {

        when(attendanceRepository.findById(99L))
                .thenReturn(Optional.empty());

        AttendanceNotFoundException exception =
                assertThrows(
                        AttendanceNotFoundException.class,
                        () -> attendanceService.deleteAttendance(99L)
                );

        assertEquals(
                "Attendance not found with the id: 99",
                exception.getMessage()
        );

        verify(attendanceRepository).findById(99L);

        verify(attendanceRepository, never())
                .delete(any(Attendance.class));
    }
}