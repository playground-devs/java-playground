package com.playground.java.employee.service;

import com.playground.java.employee.model.Employee;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeServiceImpl {
    public static void main(String[] args){

        Employee employeeOne = new Employee("Ram", 11, 5000.0);
        Employee employeeTwo = new Employee("Raju", 12, 4000.0);
        Employee employeeThree = new Employee("Hari", 9, 3000.0);
        Employee employeeFour = new Employee("Harsha", 13, 7000.0);
        Employee employeeFive = new Employee("Ravi", 15, 9000.0);

        List<Employee> employeeList = new ArrayList<>();
        employeeList.addAll(Arrays.asList(employeeOne,employeeTwo,employeeThree, employeeFour, employeeFive));
        List<Employee> higestSalaryEmployee = employeeList.stream().sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed()).limit(1).collect(Collectors.toList());

        employeeList.sort(Comparator.comparing(Employee::getEmployeeSalary).reversed());
        System.out.println(employeeList.stream().findFirst()+"+"+higestSalaryEmployee);
        //employeeList.stream().findFirst();

    }
}
