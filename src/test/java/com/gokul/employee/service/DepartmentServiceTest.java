package com.gokul.employee.service;

import com.gokul.employee.exception.DepartmentAlreadyExistsException;
import com.gokul.employee.exception.DepartmentNotFoundException;
import com.gokul.employee.repository.DepartmentRepository;
import com.gokul.employee.dto.DepartmentRequest;
import com.gokul.employee.dto.DepartmentResponse;
import com.gokul.employee.entity.Department;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        departmentService = new DepartmentService(
                departmentRepository
        );
    }

    @Test
    void createDepartment_shouldCreateSuccessfully() {

        DepartmentRequest request = new DepartmentRequest(
                "Information Technology",
                "IT Department"
        );

        when(departmentRepository.existsByName(request.getName()))
                .thenReturn(false);

        Department savedDepartment = Department.builder()
                .id(1L)
                .name("Information Technology")
                .description("IT Department")
                .build();

        when(departmentRepository.save(any(Department.class)))
                .thenReturn(savedDepartment);

        DepartmentResponse response =
                departmentService.createDepartment(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(
                "Information Technology",
                response.getName()
        );
        assertEquals(
                "IT Department",
                response.getDescription()
        );

        verify(departmentRepository)
                .existsByName("Information Technology");

        verify(departmentRepository)
                .save(any(Department.class));
    }
    @Test
    void createDepartment_shouldThrowException_whenNameAlreadyExists() {

        DepartmentRequest request = new DepartmentRequest(
                "Finance",
                "Finance Department"
        );

        when(departmentRepository.existsByName(request.getName()))
                .thenReturn(true);

        DepartmentAlreadyExistsException exception =
                assertThrows(
                        DepartmentAlreadyExistsException.class,
                        () -> departmentService.createDepartment(request)
                );

        assertEquals(
                "Department already exists",
                exception.getMessage()
        );

        verify(departmentRepository)
                .existsByName("Finance");

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    @Test
    void getAllDepartments_shouldReturnAllDepartments() {

        List<Department> departments = List.of(
                Department.builder()
                        .id(1L)
                        .name("IT")
                        .description("IT Department")
                        .build(),

                Department.builder()
                        .id(2L)
                        .name("HR")
                        .description("HR Department")
                        .build()
        );

        when(departmentRepository.findAll())
                .thenReturn(departments);

        List<DepartmentResponse> response =
                departmentService.getAllDepartments();

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals("IT", response.get(0).getName());
        assertEquals("HR", response.get(1).getName());

        verify(departmentRepository).findAll();
    }

    @Test
    void getDepartmentById_shouldReturnDepartment_whenIdExists() {

        Department department = Department.builder()
                .id(1L)
                .name("IT")
                .description("IT Department")
                .build();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        DepartmentResponse response =
                departmentService.getDepartmentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("IT", response.getName());
        assertEquals("IT Department", response.getDescription());

        verify(departmentRepository).findById(1L);
    }

    @Test
    void getDepartmentById_shouldThrowException_whenIdDoesNotExist() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        DepartmentNotFoundException exception =
                assertThrows(
                        DepartmentNotFoundException.class,
                        () -> departmentService.getDepartmentById(99L)
                );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(departmentRepository).findById(99L);
    }
    @Test
    void updateDepartment_shouldUpdateSuccessfully() {

        DepartmentRequest request = new DepartmentRequest(
                "Updated IT",
                "Updated IT Department"
        );

        Department existingDepartment = Department.builder()
                .id(1L)
                .name("IT")
                .description("IT Department")
                .build();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(existingDepartment));

        when(departmentRepository.save(any(Department.class)))
                .thenReturn(existingDepartment);

        DepartmentResponse response =
                departmentService.updateDepartment(1L, request);

        assertNotNull(response);
        assertEquals("Updated IT", response.getName());
        assertEquals(
                "Updated IT Department",
                response.getDescription()
        );

        verify(departmentRepository).findById(1L);
        verify(departmentRepository).save(existingDepartment);
    }
    @Test
    void updateDepartment_shouldThrowException_whenIdDoesNotExist() {

        DepartmentRequest request = new DepartmentRequest(
                "Updated IT",
                "Updated IT Department"
        );

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        DepartmentNotFoundException exception =
                assertThrows(
                        DepartmentNotFoundException.class,
                        () -> departmentService.updateDepartment(99L, request)
                );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(departmentRepository).findById(99L);

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    @Test
    void deleteDepartment_shouldDeleteSuccessfully() {

        Department department = Department.builder()
                .id(1L)
                .name("IT")
                .description("IT Department")
                .build();

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).findById(1L);
        verify(departmentRepository).delete(department);
    }

    @Test
    void deleteDepartment_shouldThrowException_whenIdDoesNotExist() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        DepartmentNotFoundException exception =
                assertThrows(
                        DepartmentNotFoundException.class,
                        () -> departmentService.deleteDepartment(99L)
                );

        assertEquals(
                "Department not found with id: 99",
                exception.getMessage()
        );

        verify(departmentRepository).findById(99L);

        verify(departmentRepository, never())
                .delete(any(Department.class));
    }


}
