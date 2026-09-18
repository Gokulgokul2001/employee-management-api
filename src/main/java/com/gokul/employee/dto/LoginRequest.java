package com.gokul.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invaild email formate")
    private String email;

    @NotBlank(message = "password is required")
    private String password;
}
