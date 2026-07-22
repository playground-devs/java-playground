package com.playground.java.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class InterviewQuestion {

     public static void main(String[] args) {

         AtomicReference<String> result = new AtomicReference<>("");

         List<String> words = new ArrayList<>();

         words.add("memory");

         words.add("performance");

         words.add("interview");
 
        words.stream().forEach(word -> result.set(result + word + " "));
 
        System.out.println(result.get());

     }

 }