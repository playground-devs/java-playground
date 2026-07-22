package com.playground.java.streams;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FindTop3Numbers {
    public static void main(String[] args) {
//        int[] arr = {1, 2, 3, 5, 2, 3, 7, 2, 6, 10};
//        Arrays.stream(arr).boxed().sorted(Comparator.comparing(Integer::intValue).reversed()).distinct().forEach(num -> System.out.println(num));
//
//        Arrays.stream(arr).boxed().sorted(Comparator.comparing(Integer::intValue).reversed()).distinct().limit(3).forEach(System.out::println);
//        List numList = Arrays.stream(arr).boxed().sorted(Comparator.comparing(Integer::intValue).reversed()).distinct().limit(3).collect(Collectors.toList());
//        System.out.println(numList);
//
//
//        String[] words = {"apple", "banana", "apple", "orange", "banana", "apple"};
//        Map<String, Integer> wordCount = new HashMap<>();
//
//        List startsWithA = Arrays.stream(words).filter(s -> s.startsWith("a")).collect(Collectors.toList());
//        System.out.println(startsWithA);
//
//        Arrays.stream(words).collect(Collectors.groupingBy(s -> s.charAt(0), TreeMap::new, Collectors.toList()))
//                .forEach((k, v) -> System.out.println(k + "--->" + v));
//
//        Arrays.stream(arr).boxed().filter(num -> num % 2 == 0).distinct().collect(Collectors.toList()).forEach(System.out::println);
//        Arrays.stream(arr).boxed().filter(num -> num % 2 != 0).distinct().collect(Collectors.toList()).forEach(System.out::println);
//
//
//        int[] arrr = {1, 2, 3, 5, 2, 3, 7, 2, 6, 10};
//        IntStream.range(0, arrr.length)
//                .boxed()
//                .filter(i -> arrr[i] % 2 == 0)
//                .forEach(i -> System.out.println(i + "" + arrr[i]));
//
//        List<Integer> numbers = new ArrayList<>(Arrays.asList(3, 8, 10, 15, 22, 5, 6, 9));
//
//        IntStream.range(0, numbers.size())
//                .filter(i -> numbers.get(i) % 2 == 0)
//                .forEach(i -> System.out.println("Index::"+i+" Number::"+numbers.get(i)));

        String[] words = {"apple", "banana", "apple", "orange", "banana", "apple"};
        Map<String, Long> result = Arrays.stream(words).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        Map<String, Long> newResult = Arrays.stream(words).collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting())).descendingMap();
        Arrays.stream(words).collect(Collectors.groupingBy(word -> word.charAt(0))).entrySet().forEach(System.out::println);
        Arrays.stream(words).collect(Collectors.groupingBy(word -> word.charAt(0), TreeMap::new, Collectors.toList())).descendingMap().entrySet().forEach(System.out::println);

        result.entrySet().forEach( e -> System.out.println(e.getKey()+" "+e.getValue()));
        newResult.entrySet().forEach( e -> System.out.println(e.getKey()+" "+e.getValue()));
    }
}