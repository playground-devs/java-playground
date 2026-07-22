package com.playground.java.streams;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EvenOddWithStreams {
    public static void main(String[] args){
        List<Integer> numbers = Arrays.asList(3, 8, 10, 15, 22, 5, 6, 9);
        List evenNumbers = numbers.stream().collect(Collectors.partitioningBy(n -> n % 2 == 0)).get(true);


        List oddNumbers = numbers.stream().filter( n -> n % 2 != 0).collect(Collectors.toList());
        System.out.println("Even Numbers::"+evenNumbers);
        System.out.println("Odd Numbers::"+oddNumbers);
        List topThree = numbers.stream().sorted((a,b) -> b-a).limit(3).collect(Collectors.toList());
        System.out.println("Top three Numbers::"+topThree);

        int[] arr = {3, 8, 10, 15, 22, 5, 6, 9};

        Arrays.stream(arr).boxed().sorted((a,b) -> b-a).limit(3).forEach(System.out::println);

        Arrays.stream(arr).boxed().sorted((a,b) -> b-a).limit(3).forEach(n -> System.out.println(n));

    }
}
