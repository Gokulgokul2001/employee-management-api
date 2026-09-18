package com.gokul.employee.service;

import com.gokul.employee.dto.EmployeeRequest;
import com.gokul.employee.dto.EmployeeResponse;
import com.gokul.employee.entity.Department;
import com.gokul.employee.entity.Employee;
import com.gokul.employee.exception.DepartmentNotFoundException;
import com.gokul.employee.exception.EmployeeNotFoundException;
import com.gokul.employee.repository.DepartmentRepository;
import com.gokul.employee.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository) {

        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public EmployeeResponse createEmployee(EmployeeRequest request) {

        Department department = departmentRepository
                .findById(request.getDepartmentId())
                .orElseThrow(() ->
                        new DepartmentNotFoundException(
                                "Department not found with id: "
                                        + request.getDepartmentId()
                        ));

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

        Employee savedEmployee =
                employeeRepository.save(employee);

        return mapToResponse(savedEmployee);
    }

    public List<EmployeeResponse> getAllEmployees() {

        return employeeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

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

    public EmployeeResponse getEmployeeById(Long id){
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()-> new EmployeeNotFoundException(
                        "Employee not found with id: " + id));
        return mapToResponse(employee);
    }

    public EmployeeResponse updateEmployee(
            Long id,
            EmployeeRequest request){

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(()-> new EmployeeNotFoundException(
                        "Employee not found with id: " + id));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(()-> new DepartmentNotFoundException(
                        "Department not found with id: " + request.getDepartmentId()
                ));
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setJoiningDate(request.getJoiningDate());
        employee.setDesignation(request.getDesignation());
        employee.setDepartment(department);

        Employee updateEmployee = employeeRepository.save(employee);
        return mapToResponse(updateEmployee);
    }

    public void deleteEmployee(Long id){
        Employee employee = employeeRepository.findById(id).
                orElseThrow(()-> new EmployeeNotFoundException(
                        "Employee not found with id: " + id
                ));
        employeeRepository.delete(employee);
    }
}
