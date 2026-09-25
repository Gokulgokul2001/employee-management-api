
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class LeaveServiceTest {

    @Mock
    private LeaveRepository leaveRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    private LeaveService leaveService;
    private Employee employee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        leaveService = new LeaveService(
                leaveRepository,
                employeeRepository,
                leaveBalanceRepository
        );

        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .build();

        setAuthenticatedUser("ROLE_EMPLOYEE");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(String role) {
        var authentication =
                new UsernamePasswordAuthenticationToken(
                        "gokul@test.com",
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    private Leave createLeave(String status) {
        return Leave.builder()
                .id(1L)
                .employee(employee)
                .startDate(LocalDate.of(2026, 9, 21))
                .endDate(LocalDate.of(2026, 9, 22))
                .leaveType("CASUAL")
                .status(status)
                .reason("Personal work")
                .build();
    }

    private LeaveBalance createBalance() {
        return LeaveBalance.builder()
                .id(1L)
                .employee(employee)
                .annualEntitlement(30)
                .usedDays(0)
                .remainingDays(30)
                .build();
    }

    // CREATE LEAVE

    @Test
    void createLeave_shouldCreateLeaveSuccessfully() {
        LeaveRequest request = new LeaveRequest(
                1L,
                LocalDate.of(2026, 9, 21),
                LocalDate.of(2026, 9, 22),
                "CASUAL",
                "Personal work"
        );

        when(employeeRepository.findByEmail("gokul@test.com"))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.existsOverlappingLeave(
                anyLong(), any(), any(), any()))
                .thenReturn(false);

        when(leaveRepository.save(any(Leave.class)))
                .thenAnswer(invocation -> {
                    Leave leave = invocation.getArgument(0);
                    leave.setId(1L);
                    return leave;
                });

        LeaveResponse result = leaveService.createLeave(request);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getEmployeeID());
        assertEquals("Gokul S", result.getEmployeeName());
        assertEquals(LocalDate.of(2026, 9, 21), result.getStartDate());
        assertEquals(LocalDate.of(2026, 9, 22), result.getEndDate());
        assertEquals("CASUAL", result.getLeaveType());
        assertEquals("PENDING", result.getStatus());
        assertEquals("Personal work", result.getReason());

        verify(employeeRepository).findByEmail("gokul@test.com");
        verify(leaveRepository).save(any(Leave.class));
    }

    @Test
    void createLeave_shouldThrowException_whenEmployeeDoesNotExist() {
        LeaveRequest request = new LeaveRequest(
                99L,
                LocalDate.of(2026, 9, 21),
                LocalDate.of(2026, 9, 22),
                "CASUAL",
                "Personal work"
        );

        when(employeeRepository.findByEmail("gokul@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> leaveService.createLeave(request)
        );

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void createLeave_shouldThrowException_whenStartDateIsAfterEndDate() {
        LeaveRequest request = new LeaveRequest(
                1L,
                LocalDate.of(2026, 9, 25),
                LocalDate.of(2026, 9, 22),
                "CASUAL",
                "Personal work"
        );

        when(employeeRepository.findByEmail("gokul@test.com"))
                .thenReturn(Optional.of(employee));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.createLeave(request)
        );

        assertEquals(
                "Start date cannot be after end date",
                exception.getMessage()
        );

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    // GET ALL LEAVES - HR/ADMIN

    @Test
    void getAllLeaves_shouldReturnLeaveList() {
        setAuthenticatedUser("ROLE_HR");

        Leave leave1 = createLeave("PENDING");

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

        List<LeaveResponse> result = leaveService.getAllLeaves();

        assertEquals(2, result.size());
        assertEquals("Gokul S", result.get(0).getEmployeeName());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("APPROVED", result.get(1).getStatus());

        verify(leaveRepository).findAll();
    }

    // GET LEAVES BY EMPLOYEE ID - HR/ADMIN

    @Test
    void getLeavesByEmployeeId_shouldReturnLeaveList() {
        setAuthenticatedUser("ROLE_ADMIN");

        Leave leave = createLeave("PENDING");

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.findByEmployeeId(1L))
                .thenReturn(List.of(leave));

        List<LeaveResponse> result =
                leaveService.getLeavesByEmployeeId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1L, result.get(0).getEmployeeID());
        assertEquals("Gokul S", result.get(0).getEmployeeName());
        assertEquals("PENDING", result.get(0).getStatus());
    }

    @Test
    void getLeavesByEmployeeId_shouldThrowException_whenEmployeeDoesNotExist() {
        setAuthenticatedUser("ROLE_HR");

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                EmployeeNotFoundException.class,
                () -> leaveService.getLeavesByEmployeeId(99L)
        );

        verify(leaveRepository, never()).findByEmployeeId(99L);
    }

    // GET LEAVES BY STATUS - HR/ADMIN

    @Test
    void getLeavesByStatus_shouldReturnLeaveList() {
        setAuthenticatedUser("ROLE_HR");

        Leave leave1 = createLeave("PENDING");
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
        assertEquals("PENDING", result.get(1).getStatus());
    }

    @Test
    void getLeavesByStatus_shouldReturnEmptyList_whenNoLeavesExist() {
        setAuthenticatedUser("ROLE_ADMIN");

        when(leaveRepository.findByStatus("APPROVED"))
                .thenReturn(List.of());

        List<LeaveResponse> result =
                leaveService.getLeavesByStatus("APPROVED");

        assertTrue(result.isEmpty());
    }

    @Test
    void getLeavesByStatus_shouldConvertStatusToUpperCase() {
        setAuthenticatedUser("ROLE_HR");

        when(leaveRepository.findByStatus("PENDING"))
                .thenReturn(List.of());

        leaveService.getLeavesByStatus("pending");

        verify(leaveRepository).findByStatus("PENDING");
    }

    // UPDATE LEAVE - OWN PENDING REQUEST ONLY

    @Test
    void updateLeave_shouldUpdateAndReturnLeave() {
        Leave leave = createLeave("PENDING");

        LeaveRequest request = new LeaveRequest(
                1L,
                LocalDate.of(2026, 9, 24),
                LocalDate.of(2026, 9, 25),
                "SICK",
                "Medical appointment"
        );

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(employeeRepository.findByEmail("gokul@test.com"))
                .thenReturn(Optional.of(employee));

        when(leaveRepository.existsOverlappingLeaveExcludingId(
                anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(false);

        when(leaveRepository.save(any(Leave.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveResponse result = leaveService.updateLeave(1L, request);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getEmployeeID());
        assertEquals(LocalDate.of(2026, 9, 24), result.getStartDate());
        assertEquals(LocalDate.of(2026, 9, 25), result.getEndDate());
        assertEquals("SICK", result.getLeaveType());
        assertEquals("PENDING", result.getStatus());
        assertEquals("Medical appointment", result.getReason());
    }

    @Test
    void updateLeave_shouldThrowException_whenLeaveDoesNotExist() {
        LeaveRequest request = new LeaveRequest(
                1L,
                LocalDate.of(2026, 9, 24),
                LocalDate.of(2026, 9, 25),
                "SICK",
                "Medical appointment"
        );

        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveNotFoundException.class,
                () -> leaveService.updateLeave(99L, request)
        );

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void updateLeave_shouldThrowException_whenApprovedLeaveIsEdited() {
        Leave leave = createLeave("APPROVED");

        LeaveRequest request = new LeaveRequest(
                1L,
                LocalDate.of(2026, 9, 24),
                LocalDate.of(2026, 9, 25),
                "SICK",
                "Medical appointment"
        );

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(employeeRepository.findByEmail("gokul@test.com"))
                .thenReturn(Optional.of(employee));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.updateLeave(1L, request)
        );

        assertEquals(
                "Only PENDING leave requests can be edited",
                exception.getMessage()
        );
    }

    // UPDATE LEAVE STATUS - HR/ADMIN

    @Test
    void updateLeaveStatus_shouldApproveLeaveAndDeductBalance() {
        setAuthenticatedUser("ROLE_HR");

        Leave leave = createLeave("PENDING");
        LeaveBalance balance = createBalance();

        LeaveStatusRequest request =
                new LeaveStatusRequest("APPROVED");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(leaveRepository.existsOverlappingLeaveExcludingId(
                anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(false);

        when(leaveBalanceRepository.findByEmployeeId(1L))
                .thenReturn(Optional.of(balance));

        when(leaveRepository.save(any(Leave.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveResponse result =
                leaveService.updateLeaveStatus(1L, request);

        assertEquals("APPROVED", result.getStatus());
        assertEquals(2, balance.getUsedDays());
        assertEquals(28, balance.getRemainingDays());

        verify(leaveBalanceRepository).save(balance);
        verify(leaveRepository).save(leave);
    }

    @Test
    void updateLeaveStatus_shouldNotDeductBalanceTwice_whenAlreadyApproved() {
        setAuthenticatedUser("ROLE_ADMIN");

        Leave leave = createLeave("APPROVED");

        LeaveStatusRequest request =
                new LeaveStatusRequest("APPROVED");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        LeaveResponse result =
                leaveService.updateLeaveStatus(1L, request);

        assertEquals("APPROVED", result.getStatus());

        verify(leaveBalanceRepository, never())
                .findByEmployeeId(anyLong());

        verify(leaveBalanceRepository, never())
                .save(any(LeaveBalance.class));

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    @Test
    void updateLeaveStatus_shouldRestoreBalance_whenApprovedLeaveIsRejected() {
        setAuthenticatedUser("ROLE_HR");

        Leave leave = createLeave("APPROVED");

        LeaveBalance balance = LeaveBalance.builder()
                .id(1L)
                .employee(employee)
                .annualEntitlement(30)
                .usedDays(2)
                .remainingDays(28)
                .build();

        LeaveStatusRequest request =
                new LeaveStatusRequest("REJECTED");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(leaveBalanceRepository.findByEmployeeId(1L))
                .thenReturn(Optional.of(balance));

        when(leaveRepository.save(any(Leave.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LeaveResponse result =
                leaveService.updateLeaveStatus(1L, request);

        assertEquals("REJECTED", result.getStatus());
        assertEquals(0, balance.getUsedDays());
        assertEquals(30, balance.getRemainingDays());

        verify(leaveBalanceRepository).save(balance);
        verify(leaveRepository).save(leave);
    }

    @Test
    void updateLeaveStatus_shouldThrowException_whenStatusIsInvalid() {
        setAuthenticatedUser("ROLE_ADMIN");

        Leave leave = createLeave("PENDING");

        LeaveStatusRequest request =
                new LeaveStatusRequest("CANCELLED");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.updateLeaveStatus(1L, request)
        );

        assertEquals(
                "Status must be APPROVED or REJECTED",
                exception.getMessage()
        );
    }

    @Test
    void updateLeaveStatus_shouldThrowException_whenLeaveDoesNotExist() {
        setAuthenticatedUser("ROLE_HR");

        LeaveStatusRequest request =
                new LeaveStatusRequest("APPROVED");

        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveNotFoundException.class,
                () -> leaveService.updateLeaveStatus(99L, request)
        );
    }

    @Test
    void updateLeaveStatus_shouldThrowException_whenBalanceIsInsufficient() {
        setAuthenticatedUser("ROLE_HR");

        Leave leave = createLeave("PENDING");

        LeaveBalance balance = LeaveBalance.builder()
                .id(1L)
                .employee(employee)
                .annualEntitlement(30)
                .usedDays(29)
                .remainingDays(1)
                .build();

        LeaveStatusRequest request =
                new LeaveStatusRequest("APPROVED");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(leaveRepository.existsOverlappingLeaveExcludingId(
                anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(false);

        when(leaveBalanceRepository.findByEmployeeId(1L))
                .thenReturn(Optional.of(balance));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.updateLeaveStatus(1L, request)
        );

        assertTrue(exception.getMessage().contains(
                "Insufficient leave balance"
        ));

        verify(leaveRepository, never()).save(any(Leave.class));
    }

    // DELETE LEAVE - OWN PENDING REQUEST ONLY

    @Test
    void deleteLeave_shouldDeleteLeave_whenLeaveExists() {
        Leave leave = createLeave("PENDING");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(employeeRepository.findByEmail("gokul@test.com"))
                .thenReturn(Optional.of(employee));

        leaveService.deleteLeave(1L);

        verify(leaveRepository).delete(leave);
    }

    @Test
    void deleteLeave_shouldThrowException_whenApprovedLeaveIsDeleted() {
        Leave leave = createLeave("APPROVED");

        when(leaveRepository.findById(1L))
                .thenReturn(Optional.of(leave));

        when(employeeRepository.findByEmail("gokul@test.com"))
                .thenReturn(Optional.of(employee));

        assertThrows(
                IllegalArgumentException.class,
                () -> leaveService.deleteLeave(1L)
        );

        verify(leaveRepository, never()).delete(any(Leave.class));
    }

    @Test
    void deleteLeave_shouldThrowException_whenLeaveDoesNotExist() {
        when(leaveRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                LeaveNotFoundException.class,
                () -> leaveService.deleteLeave(99L)
        );

        verify(leaveRepository, never()).delete(any(Leave.class));
    }
}