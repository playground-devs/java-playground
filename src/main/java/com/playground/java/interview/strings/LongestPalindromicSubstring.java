package com.playground.java.interview.strings;

/**
 * PATTERN: Strings / Expand Around Center
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Find the longest substring of a given string that reads the same
 * forwards and backwards (a palindrome).
 */
public class LongestPalindromicSubstring {

    // ================= PROBLEM =================
    // You get a string. You need to find the longest substring within it that is a
    // palindrome (reads the same forwards and backwards).
    // Example: s = "babad" -> output = "bab" (or "aba", both are valid length-3 answers)
    //
    // ================= SIMPLE APPROACH =================
    // Check every possible substring of the string.
    // For each substring, check if it is a palindrome by comparing characters from
    // both ends moving inward. Keep track of the longest palindrome found.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // There are O(n^2) possible substrings, and checking whether each one is a
    // palindrome takes O(n) time. That gives O(n^3) total time, which is too slow
    // for longer strings (e.g., thousands of characters).
    //
    // ================= OPTIMIZED APPROACH =================
    // Use "expand around center". A palindrome is symmetric around its center, and
    // there are 2n-1 possible centers (n single-character centers, n-1 between-character
    // centers for even-length palindromes).
    // For each possible center, expand outward in both directions as long as the
    // characters on both sides match. Track the longest palindrome found across all centers.
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure is needed - just two index pointers expanding outward.
    // By reusing the "expand" logic for every center, checking each center takes
    // O(n) time in the worst case, but with only 2n-1 centers total, this brings the
    // overall time down to O(n^2), better than the brute force's O(n^3).
    // (Manacher's Algorithm can solve this in O(n) time using extra bookkeeping,
    // but expand-around-center is the standard interview-friendly optimized answer.)
    //
    // ================= EDGE CASES =================
    // - Empty string: no palindrome exists, return "".
    // - Single character string: the whole string is the answer.
    // - Entire string is a palindrome already (e.g., "racecar").
    // - String with all identical characters (e.g., "aaaa"): whole string is the answer.
    // - Multiple palindromes of the same maximum length: any one of them is acceptable.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n^2) for expand-around-center - 2n-1 centers, each expansion
    // can take up to O(n) time. Brute force is O(n^3).
    // Space Complexity: O(1) for expand-around-center - only a few index variables.
    // Brute force is also O(1) extra space (excluding the substrings being checked).
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why are there 2n-1 centers instead of just n centers?
    // - Can you solve this in O(n) time using Manacher's Algorithm? What's the core idea?
    // - How would dynamic programming (a 2D isPalindrome table) solve this, and what's its space cost?
    // - What if you needed to count all palindromic substrings instead of finding the longest one?
    // - How would you modify this to find the longest palindromic subsequence instead of substring?
    // - What if the string could be extremely long (megabytes) - would you still use O(n^2)?
    // - How do you handle ties when multiple palindromes share the same maximum length?

    // Brute force: check every substring for being a palindrome. O(n^3).
    public static String longestPalindromeBruteForce(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        String longest = "";
        for (int start = 0; start < s.length(); start++) {
            for (int end = start; end < s.length(); end++) {
                String candidate = s.substring(start, end + 1);
                if (isPalindrome(candidate) && candidate.length() > longest.length()) {
                    longest = candidate;
                }
            }
        }
        return longest;
    }

    private static boolean isPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;
        while (left < right) {
            if (s.charAt(left) != s.charAt(right)) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }

    // Optimized: expand around every possible center. O(n^2).
    public static String longestPalindromeOptimized(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        int start = 0;
        int maxLength = 1;

        for (int center = 0; center < s.length(); center++) {
            // Odd-length palindromes: center is a single character.
            int[] oddRange = expandAroundCenter(s, center, center);
            if (oddRange[1] - oddRange[0] + 1 > maxLength) {
                start = oddRange[0];
                maxLength = oddRange[1] - oddRange[0] + 1;
            }
            // Even-length palindromes: center is between two characters.
            int[] evenRange = expandAroundCenter(s, center, center + 1);
            if (evenRange[1] - evenRange[0] + 1 > maxLength) {
                start = evenRange[0];
                maxLength = evenRange[1] - evenRange[0] + 1;
            }
        }
        return s.substring(start, start + maxLength);
    }

    // Expands outward from (left, right) while characters match; returns the final [left, right] bounds.
    private static int[] expandAroundCenter(String s, int left, int right) {
        while (left >= 0 && right < s.length() && s.charAt(left) == s.charAt(right)) {
            left--;
            right++;
        }
        // Step back one position since the loop overshoots on the mismatch/boundary.
        return new int[]{left + 1, right - 1};
    }

    public static void main(String[] args) {
        // Expected: "bab" or "aba" (length 3)
        System.out.println("Input: \"babad\"");
        System.out.println("Brute force output: " + longestPalindromeBruteForce("babad"));
        System.out.println("Optimized output: " + longestPalindromeOptimized("babad"));

        // Expected: "bb" (even-length palindrome)
        System.out.println("\nInput: \"cbbd\"");
        System.out.println("Optimized output: " + longestPalindromeOptimized("cbbd"));

        // Expected: "" (empty string, edge case)
        System.out.println("\nInput: \"\" (empty string)");
        System.out.println("Optimized output: \"" + longestPalindromeOptimized("") + "\"");
    }
}
