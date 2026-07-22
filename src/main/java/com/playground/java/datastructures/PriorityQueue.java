package com.example.javaeight.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PriorityQueue {
    public static void main(String[] args){
        int[] arr = {3, 8, 10, 15, 22, 5, 6, 9};
        List<Integer> list = new ArrayList<>();
        list.addAll(Arrays.asList(3, 8, 10, 15, 22, 5, 6, 9));
        list.sort((a,b) -> b-a);
        for(int i=0;i<3;i++){
            System.out.println(list.get(i));
        }

        PriorityQueue priorityQueue = new PriorityQueue();

    }
}
