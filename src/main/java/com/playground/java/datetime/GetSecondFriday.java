package com.playground.java.datetime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GetSecondFriday {
    public static void main(String [] args){

//        final ConcurrentHashMap<String, List<String>> shipNodeMap = new ConcurrentHashMap<String, List<String>>();
//
//        if (shipNodeMap.containsKey(null)) {
//            System.out.println("Inside if condition");
//        }

        /*LocalDate firstInYear = LocalDate.of(2024,2, 1);

        LocalDate secondFriday = firstInYear.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY)).with(TemporalAdjusters.next(DayOfWeek.FRIDAY));*/

        List<Integer> lineNumber = new ArrayList<>();
        lineNumber.add(8);
        int minLineNumber = lineNumber.stream().mapToInt(e -> e.intValue()).min().getAsInt();

    }
}
