package com.gokul.employee.exception;

public class DepartmentAlreadyExistsException extends RuntimeException{
    public DepartmentAlreadyExistsException(String message){
        super(message);
    }
}
