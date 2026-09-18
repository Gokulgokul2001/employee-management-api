package com.gokul.employee.exception;

public class AttendanceNotFoundException extends RuntimeException{
    public AttendanceNotFoundException(String message){
        super(message);
    }
}
