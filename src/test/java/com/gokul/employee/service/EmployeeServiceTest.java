package com.gokul.employee.service;

import com.gokul.employee.dto.EmployeeRequest;
import com.gokul.employee.dto.EmployeeResponse;
import com.gokul.employee.entity.Department;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.repository.DepartmentRepository;
import com.gokul.employee.repository.EmployeeRepository;
import com.gokul.employee.exception.DepartmentNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        employeeService = new EmployeeService(
                employeeRepository,
                departmentRepository
        );
    }

    @Test
    void createEmployee_shouldCreateSuccessfully() {

        EmployeeRequest request = new EmployeeRequest(
                "EMP001",
                "Gokul",
                "S",
                "gokul@test.com",
                "9876543210",
                LocalDate.of(2026, 9, 17),
                "Java Developer",
                1L
        );

        Department department = Department.builder()
                .id(1L)
                .name("IT")
                .description("IT Department")
                .build();

        Employee savedEmployee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .department(department)
                .build();

        when(departmentRepository.findById(request.getDepartmentId()))
                .thenReturn(Optional.of(department));

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(savedEmployee);

        EmployeeResponse response =
                employeeService.createEmployee(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("EMP001", response.getEmployeeCode());
        assertEquals("Gokul", response.getFirstName());
        assertEquals("S", response.getLastName());
        assertEquals("gokul@test.com", response.getEmail());
        assertEquals("Java Developer", response.getDesignation());
        assertEquals(1L, response.getDepartmentId());
        assertEquals("IT", response.getDepartmentName());

        verify(departmentRepository)
                .findById(1L);

        verify(employeeRepository)
                .save(any(Employee.class));
    }

    @Test
    void createEmployee_shouldThrowException_whenDepartmentDoesNotExist() {

        EmployeeRequest request = new EmployeeRequest(
                "EMP002",
                "Gokul",
                "S",
                "gokul2@test.com",
                "9876543210",
                LocalDate.of(2026, 9, 18),
                "Java Developer",
                99L
        );

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        DepartmentNotFoundException exception =
                assertThrows(
                        DepartmentNotFoundException.class,
                        () -> employeeService.createEmployee(request)
                );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(departmentRepository).findById(99L);

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }
    @Test
    void getAllEmployees_shouldReturnEmployeeList() {

        Department department = Department.builder()
                .id(1L)
                .name("IT")
                .description("Information Technology")
                .build();

        Employee employee1 = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .department(department)
                .build();

        Employee employee2 = Employee.builder()
                .id(2L)
                .employeeCode("EMP002")
                .firstName("Rahul")
                .lastName("K")
                .email("rahul@test.com")
                .phone("9876543211")
                .joiningDate(LocalDate.of(2026, 9, 18))
                .designation("Software Engineer")
                .department(department)
                .build();

        when(employeeRepository.findAll())
                .thenReturn(List.of(employee1, employee2));

        List<EmployeeResponse> result =
                employeeService.getAllEmployees();

        assertEquals(2, result.size());

        assertEquals("EMP001", result.get(0).getEmployeeCode());
        assertEquals("Gokul", result.get(0).getFirstName());

        assertEquals("EMP002", result.get(1).getEmployeeCode());
        assertEquals("Rahul", result.get(1).getFirstName());

        verify(employeeRepository).findAll();
    }

    @Test
    void getEmployeeById_shouldReturnEmployee_whenEmployeeExists() {

        Department department = Department.builder()
                .id(1L)
                .name("IT")
                .description("Information Technology")
                .build();

        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .department(department)
                .build();

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        EmployeeResponse result =
                employeeService.getEmployeeById(1L);

        assertEquals(1L, result.getId());
        assertEquals("EMP001", result.getEmployeeCode());
        assertEquals("Gokul", result.getFirstName());
        assertEquals("S", result.getLastName());
        assertEquals("gokul@test.com", result.getEmail());
        assertEquals("IT", result.getDepartmentName());

        verify(employeeRepository).findById(1L);
    }

    @Test
    void getEmployeeById_shouldThrowException_whenEmployeeDoesNotExist() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> employeeService.getEmployeeById(99L)
                );

        assertEquals(
                "Employee not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findById(99L);
    }

    @Test
    void updateEmployee_shouldUpdateAndReturnEmployee() {

        Department oldDepartment = Department.builder()
                .id(1L)
                .name("IT")
                .description("Information Technology")
                .build();

        Department newDepartment = Department.builder()
                .id(2L)
                .name("HR")
                .description("Human Resources")
                .build();

        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .department(oldDepartment)
                .build();

        EmployeeRequest request = new EmployeeRequest(
                "EMP001",
                "Gokul",
                "Subramonian",
                "gokul.updated@test.com",
                "9876543211",
                LocalDate.of(2026, 9, 18),
                "Senior Java Developer",
                2L
        );

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(departmentRepository.findById(2L))
                .thenReturn(Optional.of(newDepartment));

        when(employeeRepository.save(any(Employee.class)))
                .thenReturn(employee);

        EmployeeResponse result =
                employeeService.updateEmployee(1L, request);

        assertEquals("Gokul", result.getFirstName());
        assertEquals("Subramonian", result.getLastName());
        assertEquals("gokul.updated@test.com", result.getEmail());
        assertEquals("Senior Java Developer", result.getDesignation());
        assertEquals("HR", result.getDepartmentName());

        verify(employeeRepository).findById(1L);
        verify(departmentRepository).findById(2L);
        verify(employeeRepository).save(employee);
    }

    @Test
    void updateEmployee_shouldThrowException_whenEmployeeDoesNotExist() {

        EmployeeRequest request = new EmployeeRequest(
                "EMP001",
                "Gokul",
                "S",
                "gokul@test.com",
                "9876543210",
                LocalDate.of(2026, 9, 18),
                "Java Developer",
                1L
        );

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> employeeService.updateEmployee(99L, request)
                );

        assertEquals(
                "Employee not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findById(99L);

        verify(departmentRepository, never())
                .findById(anyLong());

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }

    @Test
    void updateEmployee_shouldThrowException_whenDepartmentDoesNotExist() {

        Department oldDepartment = Department.builder()
                .id(1L)
                .name("IT")
                .description("Information Technology")
                .build();

        Employee employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("Gokul")
                .lastName("S")
                .email("gokul@test.com")
                .phone("9876543210")
                .joiningDate(LocalDate.of(2026, 9, 17))
                .designation("Java Developer")
                .department(oldDepartment)
                .build();

        EmployeeRequest request = new EmployeeRequest(
                "EMP001",
                "Gokul",
                "S",
                "gokul.updated@test.com",
                "9876543211",
                LocalDate.of(2026, 9, 18),
                "Senior Java Developer",
                99L
        );

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        DepartmentNotFoundException exception =
                assertThrows(
                        DepartmentNotFoundException.class,
                        () -> employeeService.updateEmployee(1L, request)
                );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findById(1L);
        verify(departmentRepository).findById(99L);

        verify(employeeRepository, never())
                .save(any(Employee.class));
    }

    @Test
    void deleteEmployee_shouldDeleteEmployee_whenEmployeeExists() {

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

        when(employeeRepository.findById(1L))
                .thenReturn(Optional.of(employee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).findById(1L);
        verify(employeeRepository).delete(employee);
    }

    @Test
    void deleteEmployee_shouldThrowException_whenEmployeeDoesNotExist() {

        when(employeeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EmployeeNotFoundException exception =
                assertThrows(
                        EmployeeNotFoundException.class,
                        () -> employeeService.deleteEmployee(99L)
                );

        assertEquals(
                "Employee not found with id: 99",
                exception.getMessage()
        );

        verify(employeeRepository).findById(99L);

        verify(employeeRepository, never())
                .delete(any(Employee.class));
    }
}