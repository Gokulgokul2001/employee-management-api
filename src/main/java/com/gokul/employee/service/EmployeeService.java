
package com.gokul.employee.service;

import com.gokul.employee.dto.EmployeeRequest;
import com.gokul.employee.dto.EmployeeResponse;
import com.gokul.employee.dto.EmployeeCreationResponse;

import com.gokul.employee.entity.Department;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.entity.Role;
import com.gokul.employee.entity.User;

import com.gokul.employee.exception.DepartmentNotFoundException;
import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.exception.EmailAlreadyExistsException;

import com.gokul.employee.repository.DepartmentRepository;
import com.gokul.employee.repository.EmployeeRepository;
import com.gokul.employee.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            LeaveBalanceService leaveBalanceService,
            PasswordEncoder passwordEncoder) {

        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.leaveBalanceService = leaveBalanceService;
        this.passwordEncoder = passwordEncoder;
    }

    // Create employee, login account and leave balance
    @Transactional
    public EmployeeCreationResponse createEmployee(EmployeeRequest request) {

        // Prevent duplicate login accounts
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "A user account already exists with this email"
            );
        }

        // Find department
        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new DepartmentNotFoundException(
                                "Department not found with id: "
                                        + request.getDepartmentId()
                        ));

        // Generate temporary password
        String temporaryPassword = "password@123";

        // Create employee
        Employee employee = Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .joiningDate(request.getJoiningDate())
                .designation(request.getDesignation())
                .department(department)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Create employee login account
        User user = User.builder()
                .name(request.getFirstName() + " " + request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .role(Role.EMPLOYEE)
                .build();

        userRepository.save(user);

        // Initialize 30-day leave balance
        leaveBalanceService.initializeLeaveBalance(
                savedEmployee.getId()
        );

        // Return employee details and temporary password
        EmployeeResponse employeeResponse =
                mapToResponse(savedEmployee);

        return new EmployeeCreationResponse(
                employeeResponse,
                temporaryPassword
        );
    }


    // Get all employees
    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get employee by ID
    public EmployeeResponse getEmployeeById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with id: " + id
                        ));

        return mapToResponse(employee);
    }

    // Update employee
    @Transactional
    public EmployeeResponse updateEmployee(
            Long id,
            EmployeeRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with id: " + id
                        ));

        // Prevent changing to an email owned by another user
        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {

            throw new EmailAlreadyExistsException(
                    "A user account already exists with this email"
            );
        }

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new DepartmentNotFoundException(
                                "Department not found with id: "
                                        + request.getDepartmentId()
                        ));

        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setDesignation(request.getDesignation());
        employee.setDepartment(department);

        Employee updatedEmployee = employeeRepository.save(employee);

        return mapToResponse(updatedEmployee);
    }

    // Delete employee
    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found with id: " + id
                        ));

        employeeRepository.delete(employee);
    }

    // Map entity to response DTO
    private EmployeeResponse mapToResponse(Employee employee) {

        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getJoiningDate(),
                employee.getDesignation(),
                employee.getDepartment().getId(),
                employee.getDepartment().getName()
        );
    }

    // Get currently authenticated employee by email
    public EmployeeResponse getMyEmployeeDetails(String email) {

        Employee employee = employeeRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                "Employee not found for email: " + email
                        ));

        return mapToResponse(employee);
    }
}