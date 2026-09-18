package com.gokul.employee.dto;

import com.gokul.employee.entity.Role;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class AuthResponse {
    private String token;
    private String email;
    private Role role;
}
