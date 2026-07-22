package com.example.javaeight.employee.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class EmployeeOne {
    private String employeeName;
    private Integer employeeId;
    private String department;
    private Double employeeSalary;
}
