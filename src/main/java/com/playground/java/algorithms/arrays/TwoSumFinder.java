package com.example.javaeight.algorithms.arrays;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class TwoSumFinder {
    public static void main(String[] args){
       int[] arr = {3,4,8,11,18,2};
       int target = 19;
       findTwoSum(arr, target);
//       Map map = new HashMap<>();
//       for(int i=0;i<arr.length-1;i++){
//            int result = arr[i] + arr[i + 1];
//           if(result==target){
//               map.put(arr[i],arr[i+1]);
//           }
//       }
//        map.forEach((k,v) -> System.out.println(k+"-->"+v));
    }
    public static void findTwoSum(int[] arr, int target){
        Optional<int[]> result = IntStream.range(0, arr.length)
                .boxed()
                .flatMap(index -> {
                    final int i = index;
                    return IntStream.range(i+1, arr.length)
                            .filter(j -> arr[i] + arr[j] == target)
                            .mapToObj(j -> new int[]{arr[i], arr[j]});
                }).findFirst();
        if(result.isPresent()){
            int[] pair = result.get();
            System.out.println("Pair found: "+ pair[0]+"+"+pair[1]+"="+target);
        }else{
            System.out.println("No pair found with the given target.");
        }
    }
}
