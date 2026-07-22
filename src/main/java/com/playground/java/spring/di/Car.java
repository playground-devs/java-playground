package com.playground.java.spring.di;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Car {

    private String carName;
    private String carModel;
    private String carColor;

    Engine engine;

    public Car() {
        // Default constructor
    }
    @Autowired
    public Car(Engine engine){
        this.engine = engine;
    }

    public void drive(){
        System.out.println("Driving the car: ");
    }
}
