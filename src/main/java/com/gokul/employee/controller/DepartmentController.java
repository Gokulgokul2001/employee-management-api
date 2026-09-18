package com.gokul.employee.controller;

import com.gokul.employee.dto.DepartmentRequest;
import com.gokul.employee.dto.DepartmentResponse;
import com.gokul.employee.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService){
        this.departmentService = departmentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PostMapping
    @Operation(
            summary = "Create a department",
            description = "Creates a new department. Requires ADMIN or HR role."
    )
    public ResponseEntity<DepartmentResponse> createdDepartment(
            @Valid @RequestBody DepartmentRequest request){

        DepartmentResponse response = departmentService.createDepartment(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "Get all departments",
            description = "Returns a list of all departments."
    )
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments(){
        List<DepartmentResponse> departments =
                departmentService.getAllDepartments();
        return ResponseEntity.ok(departments);
    }

    @Operation(
            summary = "Get department by ID",
            description = "Returns a department using its unique ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @PathVariable Long id){
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update a department",
            description = "Updates an existing department. Requires ADMIN or HR role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request){
        DepartmentResponse response =
                departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a department",
            description = "Deletes an existing department. Requires ADMIN or HR role."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id){
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
