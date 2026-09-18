package com.gokul.employee.service;

import com.gokul.employee.dto.LeaveRequest;
import com.gokul.employee.dto.LeaveResponse;
import com.gokul.employee.dto.LeaveStatusRequest;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.entity.Leave;
import com.gokul.employee.repository.EmployeeRepository;
import com.gokul.employee.repository.LeaveRepository;
import com.gokul.employee.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        leaveService = new LeaveService(
                leaveRepository,
                employeeRepository
        );
    }

    @Test
    void createLeave_shouldCreateLeaveSuccessfully() {

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

        LeaveRequest request = new LeaveRequest(
                1L,
                LocalDate.of(2026, 9, 20),
                LocalDate.of(2026, 9, 22),
                "CASUAL",
                "Personal work"
        );

        Leave savedLeave = Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .leaveType(request.getLeaveType())
                .status("PENDING")
                .reason(request.getReason())
                .build();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.save(any(Leave.class)))
                .thenReturn(savedLeave);

        LeaveResponse result =
                leaveService.createLeave(request);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getEmployeeID());
        assertEquals("GokulS", result.getEmployeeName());
        assertEquals(
                LocalDate.of(2026, 9, 20),
                result.getStartDate()
        );
        assertEquals(
                LocalDate.of(2026, 9, 22),
                result.getEndDate()
        );
        assertEquals("CASUAL", result.getLeaveType());
        assertEquals("PENDING", result.getStatus());
        assertEquals("Personal work", result.getReason());

        verify(employeeRepository).findById(1L);
        verify(leaveRepository).save(any(Leave.class));
    }

    @Test
    void createLeave_shouldThrowException_whenEmployeeDoesNotExist() {

        LeaveRequest request = new LeaveRequest(
                99L,
                LocalDate.of(2026, 9, 20),
                LocalDate.of(2026, 9, 22),
                "CASUAL",
                "Personal work"
        );

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> leaveService.createLeave(request)
                );

        assertEquals(
                "Employee not found for the id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findById(99L);

        verify(leaveRepository, never())
                .save(any(Leave.class));
    }

    @Test
    void getAllLeaves_shouldReturnLeaveList() {

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

        Leave leave1 = Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .leaveType("CASUAL")
                .status("PENDING")
                .reason("Personal work")
                .build();

        Leave leave2 = Leave.builder()
                .id(2L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2026, 10, 2))
                .leaveType("SICK")
                .status("APPROVED")
                .reason("Medical appointment")
                .build();

        when(leaveRepository.findAll())
                .thenReturn(List.of(leave1, leave2));

        List<LeaveResponse> result =
                leaveService.getAllLeaves();

        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getId());
        assertEquals("CASUAL", result.get(0).getLeaveType());
        assertEquals("PENDING", result.get(0).getStatus());

        assertEquals(2L, result.get(1).getId());
        assertEquals("SICK", result.get(1).getLeaveType());
        assertEquals("APPROVED", result.get(1).getStatus());

        verify(leaveRepository).findAll();
    }

    @Test
    void getLeavesByEmployeeId_shouldReturnLeaveList() {

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

        Leave leave = Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .leaveType("CASUAL")
                .status("PENDING")
                .reason("Personal work")
                .build();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.findByEmployeeId(1L))
                .thenReturn(List.of(leave));

        List<LeaveResponse> result =
                leaveService.getLeavesByEmployeeId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getEmployeeID());
        assertEquals("GokulS", result.get(0).getEmployeeName());
        assertEquals("CASUAL", result.get(0).getLeaveType());
        assertEquals("PENDING", result.get(0).getStatus());

        verify(employeeRepository).findById(1L);
        verify(leaveRepository).findByEmployeeId(1L);
    }

    @Test
    void getLeavesByEmployeeId_shouldThrowException_whenEmployeeDoesNotExist() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> leaveService.getLeavesByEmployeeId(99L)
                );

        assertEquals(
                "Employee not found with the id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findById(99L);

        verify(leaveRepository, never())
                .findByEmployeeId(99L);
    }

    @Test
    void getLeavesByStatus_shouldReturnLeaveList() {

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

        Leave leave1 = Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .leaveType("CASUAL")
                .status("PENDING")
                .reason("Personal work")
                .build();

        Leave leave2 = Leave.builder()
                .id(2L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2026, 10, 2))
                .leaveType("SICK")
                .status("PENDING")
                .reason("Medical appointment")
                .build();

        when(leaveRepository.findByStatus("PENDING"))
                .thenReturn(List.of(leave1, leave2));

        List<LeaveResponse> result =
                leaveService.getLeavesByStatus("PENDING");

        assertEquals(2, result.size());

        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("CASUAL", result.get(0).getLeaveType());

        assertEquals("PENDING", result.get(1).getStatus());
        assertEquals("SICK", result.get(1).getLeaveType());

        verify(leaveRepository).findByStatus("PENDING");
    }

    @Test
    void getLeavesByStatus_shouldReturnEmptyList_whenNoLeavesExist() {

        when(leaveRepository.findByStatus("APPROVED"))
                .thenReturn(List.of());

        List<LeaveResponse> result =
                leaveService.getLeavesByStatus("APPROVED");

        assertEquals(0, result.size());

        verify(leaveRepository)
                .findByStatus("APPROVED");
    }

    @Test
    void updateLeave_shouldUpdateAndReturnLeave() {

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

        Leave leave = Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .leaveType("CASUAL")
                .status("PENDING")
                .reason("Personal work")
                .build();

        LeaveRequest request = new LeaveRequest(
                1L,
                LocalDate.of(2026, 9, 25),
                LocalDate.of(2026, 9, 27),
                "SICK",
                "Medical appointment"
        );

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.save(any(Leave.class)))
                .thenReturn(leave);

        LeaveResponse result =
                leaveService.updateLeave(1L, request);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getEmployeeID());
        assertEquals(
                LocalDate.of(2026, 9, 25),
                result.getStartDate()
        );
        assertEquals(
                LocalDate.of(2026, 9, 27),
                result.getEndDate()
        );
        assertEquals("SICK", result.getLeaveType());
        assertEquals("PENDING", result.getStatus());
        assertEquals("Medical appointment", result.getReason());

        verify(leaveRepository).findById(1L);
        verify(employeeRepository).findById(1L);
        verify(leaveRepository).save(leave);
    }
    @Test
    void updateLeave_shouldThrowException_whenLeaveDoesNotExist() {

        LeaveRequest request = new LeaveRequest(
                1L,
                LocalDate.of(2026, 9, 25),
                LocalDate.of(2026, 9, 27),
                "SICK",
                "Medical appointment"
        );

        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        LeaveNotFoundException exception =
                assertThrows(
                        LeaveNotFoundException.class,
                        () -> leaveService.updateLeave(99L, request)
                );

        assertEquals(
                "Leave not found for the id: 99",
                exception.getMessage()
        );

        verify(leaveRepository).findById(99L);

        verify(employeeRepository, never())
                .findById(anyLong());

        verify(leaveRepository, never())
                .save(any(Leave.class));
    }

    @Test
    void updateLeave_shouldThrowException_whenEmployeeDoesNotExist() {

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

        Leave leave = Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .leaveType("CASUAL")
                .status("PENDING")
                .reason("Personal work")
                .build();

        LeaveRequest request = new LeaveRequest(
                99L,
                LocalDate.of(2026, 9, 25),
                LocalDate.of(2026, 9, 27),
                "SICK",
                "Medical appointment"
        );

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> leaveService.updateLeave(1L, request)
                );

        assertEquals(
                "Employee not found for the id: 99",
                exception.getMessage()
        );

        verify(leaveRepository).findById(1L);
        verify(employeeRepository).findById(99L);

        verify(leaveRepository, never())
                .save(any(Leave.class));
    }

    @Test
    void updateLeaveStatus_shouldUpdateStatusSuccessfully() {

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

        Leave leave = Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .leaveType("CASUAL")
                .status("PENDING")
                .reason("Personal work")
                .build();

        LeaveStatusRequest request =
                new LeaveStatusRequest("APPROVED");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(leaveRepository.save(any(Leave.class)))
                .thenReturn(leave);

        LeaveResponse result =
                leaveService.updateLeaveStatus(1L, request);

        assertEquals(1L, result.getId());
        assertEquals("APPROVED", result.getStatus());

        verify(leaveRepository).findById(1L);
        verify(leaveRepository).save(leave);
    }

    @Test
    void updateLeaveStatus_shouldThrowException_whenStatusIsInvalid() {

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

        Leave leave = Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 9, 20))
                .endDate(LocalDate.of(2026, 9, 22))
                .leaveType("CASUAL")
                .status("PENDING")
                .reason("Personal work")
                .build();

        LeaveStatusRequest request =
                new LeaveStatusRequest("CANCELLED");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> leaveService.updateLeaveStatus(1L, request)
                );

        assertEquals(
                "Status must be APPROVED or REJECTED",
                exception.getMessage()
        );

        verify(leaveRepository).findById(1L);

        verify(leaveRepository, never())
                .save(any(Leave.class));
    }

    @Test
    void updateLeaveStatus_shouldThrowException_whenLeaveDoesNotExist() {

        LeaveStatusRequest request =
                new LeaveStatusRequest("APPROVED");

        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        LeaveNotFoundException exception =
                assertThrows(
                        LeaveNotFoundException.class,
                        () -> leaveService.updateLeaveStatus(99L, request)
                );

        assertEquals(
                "Leave not found with id: 99",
                exception.getMessage()
        );

        verify(leaveRepository).findById(99L);

        verify(leaveRepository, never())
                .save(any(Leave.class));
    }

    @Test
    void deleteLeave_shouldDeleteLeave_whenLeaveExists() {

        Leave leave = Leave.builder()
                .id(1L)
                .build();

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        leaveService.deleteLeave(1L);

        verify(leaveRepository).findById(1L);
        verify(leaveRepository).delete(leave);
    }

    @Test
    void deleteLeave_shouldThrowException_whenLeaveDoesNotExist() {

        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        LeaveNotFoundException exception =
                assertThrows(
                        LeaveNotFoundException.class,
                        () -> leaveService.deleteLeave(99L)
                );

        assertEquals(
                "Leave not found with id: 99",
                exception.getMessage()
        );

        verify(leaveRepository).findById(99L);

        verify(leaveRepository, never())
                .delete(any(Leave.class));
    }
}