package com.example.javaeight.strings;

import java.util.Arrays;
import java.util.Scanner;

public class CheckIfAnagramWithInbuitMethods {
    public static void main(String[] args){

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the first string");
        String stringOne = scanner.next();
        System.out.println("Enter the second string");
        String stringTwo = scanner.next();

        if(stringOne.length() != stringTwo.length()){
            System.out.println("NOT A ANAGRAM");
        }else{
            char[] arr1 = stringOne.toLowerCase().toCharArray();
            char[] arr2 = stringTwo.toLowerCase().toCharArray();
            Arrays.sort(arr1);
            Arrays.sort(arr2);

            if(Arrays.equals(arr1, arr2)){
                System.out.println("ANAGRAM");
            }else{
                System.out.println("NOT A ANAGRAM");
            }
        }
    }
}
