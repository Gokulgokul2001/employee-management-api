
package com.gokul.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreationResponse {

    private EmployeeResponse employee;

    private String temporaryPassword;

}