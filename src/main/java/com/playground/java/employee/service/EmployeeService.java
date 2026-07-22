package com.example.javaeight.employee.service;

import com.example.javaeight.spring.rest.Employee;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeService{
    public static void main(String[] args){
        Employee employee1 = new Employee("IT", "Developer", "E123", "John Doe", "75000");
        Employee employee2 = new Employee("HR", "Manager", "E456", "Jane Smith", "85000");
        Employee employee3 = new Employee("Finance", "Analyst", "E789", "Alice Johnson", "90000");
        Employee employee4 = new Employee("Marketing", "Executive", "E101", "Bob Brown", "70000");
        Employee employee5 = new Employee("Sales", "Representative", "E102", "Charlie White", "80000");
        Employee employee6 = new Employee("IT", "Developer", "E124", "Diana Green", "72000");
        Employee employee7 = new Employee("Finance", "Analyst", "790", "Diana Green", "72000");

        List employees = new ArrayList<>();
        employees.addAll(Arrays.asList(employee1, employee2, employee3, employee4, employee5, employee6, employee7));

        Object obj = employees.stream().collect(Collectors.groupingBy(Employee::getEmployeeDepartment));
        Map<String, List<Employee>> groupedEmployees = (Map<String, List<Employee>>) obj;

        employees.sort(Comparator.comparing(Employee::getEmployeeName));
        System.out.println("Sorted employees list by name: "+employees);

        employees.sort(Comparator.comparing(Employee::getEmployeeSalary));
        System.out.println("Employee with lowest salary: "+employees.get(0));

        employees.sort(Comparator.comparing(Employee::getEmployeeSalary).reversed());
        System.out.println("Employee with highest salary: "+employees.get(0));

    }
}
