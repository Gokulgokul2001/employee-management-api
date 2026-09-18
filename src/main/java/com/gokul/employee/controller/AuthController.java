package com.gokul.employee.controller;

import com.gokul.employee.dto.AuthResponse;
import com.gokul.employee.dto.LoginRequest;
import com.gokul.employee.dto.RegisterRequest;
import com.gokul.employee.dto.RegisterResponse;
import com.gokul.employee.entity.User;
import com.gokul.employee.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with a BCrypt-encrypted password."
    )

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(

            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(
            summary = "User login",
            description = "Authenticates the user and returns a JWT token."
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request){
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

}