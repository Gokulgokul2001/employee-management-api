package com.gokul.employee.service;

import com.gokul.employee.dto.DepartmentRequest;
import com.gokul.employee.dto.DepartmentResponse;
import com.gokul.employee.entity.Department;
import com.gokul.employee.exception.DepartmentAlreadyExistsException;
import com.gokul.employee.exception.DepartmentNotFoundException;
import com.gokul.employee.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository){
        this.departmentRepository = departmentRepository;
    }

    public DepartmentResponse createDepartment(DepartmentRequest request){

        if (departmentRepository.existsByName(request.getName())){
            throw new DepartmentAlreadyExistsException(
                    "Department already exists"
            );
        }

        Department department = Department.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Department savedDepartment = departmentRepository.save(department);

        return new DepartmentResponse(
                savedDepartment.getId(),
                savedDepartment.getName(),
                savedDepartment.getDescription()
        );
    }
    public List<DepartmentResponse> getAllDepartments(){
        return departmentRepository.findAll()
                .stream()
                .map(department -> new DepartmentResponse(
                        department.getId(),
                        department.getName(),
                        department.getDescription()
                ))
                .toList();
    }

    public DepartmentResponse getDepartmentById(Long id){

       Department department =  departmentRepository.findById(id).
                orElseThrow(()-> new DepartmentNotFoundException(
                        "Department not found with id: " + id
                ));
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription()
        );
    }

    public DepartmentResponse updateDepartment(
            Long id,
            DepartmentRequest request){
        Department department = departmentRepository.findById(id)
                .orElseThrow(()->
                        new DepartmentNotFoundException(
                                "Department not found with id: " + id));

        if (!department.getName().equals(request.getName())
        && departmentRepository.existsByName(request.getName())){
            throw new DepartmentAlreadyExistsException(
                    "Department already exists"
            );
        }
        department.setName(request.getName());
        department.setDescription(request.getDescription());

        Department updateDepartment =
                departmentRepository.save(department);

        return new DepartmentResponse(
                updateDepartment.getId(),
                updateDepartment.getName(),
                updateDepartment.getDescription()
        );
    }

    public void deleteDepartment(Long id){
        Department department = departmentRepository.findById(id)
                .orElseThrow(()->
                        new DepartmentNotFoundException(
                                "Department not found with id: " + id
                        ));
        departmentRepository.delete(department);
    }
}
