package com.playground.java.employee.service;

import com.playground.java.employee.model.EmployeeOne;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class GroupByDepartmentSecondSalary {
    public static void main(String[] args){

        EmployeeOne e1 = new EmployeeOne("A", 11, "d1", 10.0);
        EmployeeOne e2 = new EmployeeOne("B", 22, "d2", 20.0);
        EmployeeOne e3 = new EmployeeOne("C", 22, "d1", 30.0);
        EmployeeOne e4 = new EmployeeOne("D", 44, "d3", 40.0);

        List<EmployeeOne> employeeList = new ArrayList<>(Arrays.asList(e1, e2, e3, e4));

        Map<String, EmployeeOne> mapTwo  =  employeeList.stream().collect(Collectors.groupingBy(EmployeeOne::getDepartment, Collectors.collectingAndThen(
                Collectors.toList(),
                list -> list.stream().sorted(Comparator.comparing(EmployeeOne::getEmployeeSalary).reversed()).skip(1).findFirst().orElse(null)
        )));

        System.out.println(mapTwo);

        int [] num = {1,2,3,4,5,6,7,8,9,10};

        stream(num).boxed().sorted((a, b) -> b-a).limit(3).forEach(System.out::println);
        stream(num).boxed().sorted(Comparator.reverseOrder()).limit(3).forEach(System.out::println);
        stream(num).boxed().sorted(Comparator.comparingInt(Integer::intValue).reversed()).limit(3).forEach(System.out::println);
        /*Arrays.asList(num).stream().sorted((a,b) -> b-a).limit(3).forEach(System.out::println);*/

        String name = "Ramaraju Gaddam";
        name.replaceAll("\\s","").chars().mapToObj(c -> (char) c).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).forEach( (k, v) -> System.out.println(k+"==="+v));

        name.replaceAll("\\s","").chars().mapToObj(c -> (char) c).filter( c -> "aeiou".indexOf(c) > -1).forEach(System.out::println);
        String words = "Ramaraju Gaddam is good boy";
        Arrays.stream(words.split("\\s")).collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).forEach((k,v) -> System.out.println(k+"==="+v));
    }

}
