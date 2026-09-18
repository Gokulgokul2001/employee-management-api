package com.gokul.employee.exception;

public class LeaveNotFoundException extends RuntimeException {

    public LeaveNotFoundException(String message) {
        super(message);
    }
}