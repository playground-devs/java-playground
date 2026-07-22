package com.playground.java.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GetTopThreeUsers {

    public static void main(String[] args){
        List<Transaction> transactions = new ArrayList<>();
        Transaction t1 = new Transaction(101, 200);
        Transaction t2 = new Transaction(102, 500);
        Transaction t3 = new Transaction(101, 300);
        Transaction t4 = new Transaction(103, 150);
        Transaction t5 = new Transaction(104, 700);
        Transaction t6 = new Transaction(106, 300);
        Transaction t7 = new Transaction(107, 400);
        Transaction t8 = new Transaction(108, 200);
        Transaction t9 = new Transaction(109, 100);

        transactions.addAll(List.of(t1,t2,t3,t4,t5,t6,t7,t8,t9));

        Map<Integer, Integer> userTotalMap = new HashMap<>();

        transactions.stream().forEach( transaction -> {
            userTotalMap.put(transaction.getUserId(), userTotalMap.getOrDefault(transaction.getUserId(), 0)+transaction.getAmount());
        });

        List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(userTotalMap.entrySet());
        sorted.sort((a,b) -> b.getValue() - a.getValue());
        //List topThreeUsers = sorted.stream().limit(3).map(Map.Entry::getKey).collect(Collectors.toList());

        List topThreeUsers  = userTotalMap.entrySet().stream().sorted((a,b) -> b.getValue().compareTo(a.getValue())).limit(3).map(Map.Entry::getKey).collect(Collectors.toList());

        System.out.println("top three users :: "+topThreeUsers);

    }
}
