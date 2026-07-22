package com.example.javaeight.spring.rest;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.annotation.processing.Generated;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "employees")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Employee {
    private String employeeName;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String employeeId;
    private String employeeDepartment;
    private String employeeDesignations;
    private String employeeSalary;

//    public Employee(){
//        //Default constructor
//    }
//    public Employee(String employeeDepartment, String employeeDesignations, String employeeId, String employeeName, String employeeSalary) {
//        this.employeeDepartment = employeeDepartment;
//        this.employeeDesignations = employeeDesignations;
//        this.employeeId = employeeId;
//        this.employeeName = employeeName;
//        this.employeeSalary = employeeSalary;
//
//    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeDepartment() {
        return employeeDepartment;
    }

    public void setEmployeeDepartment(String employeeDepartment) {
        this.employeeDepartment = employeeDepartment;
    }

    public String getEmployeeDesignations() {
        return employeeDesignations;
    }

    public void setEmployeeDesignations(String employeeDesignations) {
        this.employeeDesignations = employeeDesignations;
    }

    public String getEmployeeSalary() {
        return employeeSalary;
    }

    public void setEmployeeSalary(String employeeSalary) {
        this.employeeSalary = employeeSalary;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeName='" + employeeName + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", employeeDepartment='" + employeeDepartment + '\'' +
                ", employeeDesignations='" + employeeDesignations + '\'' +
                ", employeeSalary='" + employeeSalary + '\'' +
                '}';
    }
}

