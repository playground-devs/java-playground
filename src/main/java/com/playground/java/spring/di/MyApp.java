package com.playground.java.spring.di;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class MyApp {
    public static void main(String[] args){

        ApplicationContext context = SpringApplication.run(MyApp.class, args);
        Car car = context.getBean(Car.class);
        car.drive();
    }
}
