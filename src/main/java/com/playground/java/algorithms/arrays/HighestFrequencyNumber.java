package com.playground.java.algorithms.arrays;

import java.util.HashMap;
import java.util.Map;

public class HighestFrequencyNumber {
    public static void main(String[] args){
        int[] arr = {1,2,1,4,1,5,1,6};
        Map<Integer, Integer> map = new HashMap<>();
        for(int num: arr){
            map.put(num, map.getOrDefault(num,0)+1);
        }

        int maxFreq = 0;
        int mostFrequentElement = -1;

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxFreq) {
                maxFreq = entry.getValue();
                mostFrequentElement = entry.getKey();
            }
        }

        System.out.println("Most frequent element: " + mostFrequentElement);
        System.out.println("Frequency: " + maxFreq);
    }
}
