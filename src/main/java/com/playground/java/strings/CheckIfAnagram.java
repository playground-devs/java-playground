package com.example.javaeight.strings;

import java.util.Scanner;

public class CheckIfAnagram {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the first string:");
        String inputOne = scanner.next();
        System.out.println("Enter the second string:");
        String inputTwo = scanner.next();
        int counter = 0;
        if(inputOne.length() == inputTwo.length()){
            for(int i=0;i < inputOne.length();i++){
                for(int j=0;j < inputTwo.length();j++){
                    if(inputOne.charAt(i) == inputTwo.charAt(j)){
                        counter++;
                       break;
                    }
                }
            }
            if(counter == inputTwo.length()){
                System.out.println("ANAGRAM");
            } else {
                System.out.println("NOT A ANAGRAM");
            }
        }else {
            System.out.println("NOT A ANAGRAM");
        }
        scanner.close();
    }
}
