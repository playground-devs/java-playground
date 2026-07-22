package com.example.javaeight.strings;

import java.util.Arrays;
import java.util.Scanner;

public class AnagramDetector {
    public static void main (String[] args){
        Scanner scanner  = new Scanner(System.in);

        System.out.println("Enter the first Number....");
        String firstInput = scanner.next().toLowerCase();

        System.out.println("Enter the second Number...");
        String secondInput = scanner.next().toLowerCase();

        if(isAnagram(firstInput, secondInput)){
            System.out.println("The provide Strings are Anagram");
        }else{
            System.out.println("The provided Strings are not Anagram");
        }
    }

    private static boolean isAnagram(String firstInput, String secondInput) {
        char [] inputOne = firstInput.replaceAll("\\s", "").toCharArray();
        char [] inputSecond = secondInput.replaceAll("\\s","").toCharArray();
        Arrays.sort(inputOne);
        Arrays.sort(inputSecond);
        return Arrays.equals(inputOne, inputSecond);
    }
}
