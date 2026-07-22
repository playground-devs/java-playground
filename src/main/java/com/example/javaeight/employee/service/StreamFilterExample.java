package com.example.javaeight.employee.service;
import com.example.javaeight.spring.rest.Employee;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StreamFilterExample {

    public static void main(String [] args){
        String[] ref = {"HEllo", "", " ", null, "ram", "Raj" };
        List strings = Arrays.stream(ref).filter(s -> s != null && s.strip().length() > 0).collect(Collectors.toList());
        System.out.println(strings);

        Employee employee = new Employee("AAA","SI", "100", "ABC", "1000");
        Employee employee1 = new Employee("BBB","SE", "200", "XYZ", "2000");
        Employee employee2 = new Employee("CCC","SSE", "300", "PQR", "3000");
        List<Employee> employees = Arrays.asList(employee, employee1, employee2);

        employees.sort(Comparator.comparing(Employee::getEmployeeSalary).reversed());
        employees.stream().distinct().skip(1).findFirst().ifPresent(System.out::println);
        //System.out.println(employees.get(1));

        employees.stream().sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed()).distinct().skip(1).findFirst().ifPresent(System.out::println);
    }
}
