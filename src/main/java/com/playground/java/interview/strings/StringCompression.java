package com.playground.java.interview.strings;

import java.util.Arrays;

/**
 * PATTERN: Strings / Two Pointers (In-place Write)
 * PRIORITY: P1 - Frequently Asked
 * PROBLEM STATEMENT: Compress a character array in-place by replacing runs of repeated
 * characters with the character followed by its count, only if that makes it shorter.
 */
public class StringCompression {

    // ================= PROBLEM =================
    // You get a character array. You need to compress consecutive repeated characters
    // into "character + count" form, modifying the array in-place, and return the new length.
    // Example: chars = ['a','a','b','c','c','c','c','c','a','a','a'] -> compressed
    // in-place to ['a','2','b','c','5','a','3'], returned length = 7 (meaning "a2bc5a3").
    // Note: a count of 1 is not written (just the character alone), e.g. "b" stays "b" not "b1".
    //
    // ================= SIMPLE APPROACH =================
    // Build a separate StringBuilder. Walk through the array, count how many times
    // each character repeats consecutively, and append "char + count" (or just
    // "char" if count is 1) to the StringBuilder. At the end, copy the StringBuilder's
    // characters back into the original array.
    //
    // ================= WHY IT'S NOT ENOUGH =================
    // This uses an extra StringBuilder (and its internal char buffer), which is
    // O(n) extra space. The problem is typically posed specifically to test whether
    // you can do the compression truly in-place with O(1) extra space.
    //
    // ================= OPTIMIZED APPROACH =================
    // Use two pointers: "read" scans through the array finding runs of the same
    // character, and "write" marks where the next compressed character/count should
    // be written, always at or behind "read" (since compressed output is never
    // longer than the input for runs >= 1).
    // For each run: write the character at the write pointer, then if the run length
    // is more than 1, write each digit of the count as separate characters.
    // Only compress if it doesn't produce something identical or worse (in this
    // classic version, we always write character+count for repeats > 1 and just the
    // character for single occurrences, which is always the same length or shorter).
    //
    // ================= WHY THIS DATA STRUCTURE =================
    // No extra data structure needed - the two-pointer technique reuses the same
    // array as both input and output since the write pointer never runs ahead of
    // the read pointer (compressed output length is always <= original run length).
    // This avoids allocating any extra buffer.
    //
    // ================= EDGE CASES =================
    // - Single character array: stays as-is, length 1.
    // - No repeated characters at all (e.g., "abc"): stays the same, count of 1 each, not written.
    // - Entire array is the same character repeated many times (e.g., 12 a's -> "a12").
    // - Counts with multiple digits (e.g., a run of 12 must write '1' then '2' as two separate characters).
    // - Empty array: nothing to compress, length 0.
    //
    // ================= COMPLEXITY =================
    // Time Complexity: O(n) for the optimized approach - each character is visited
    // once by the read pointer. Brute force is also O(n) but with extra overhead
    // from the StringBuilder and the final copy-back step.
    // Space Complexity: O(1) for the optimized in-place approach (ignoring the
    // output which reuses the input array). Brute force is O(n) for the StringBuilder.
    //
    // ================= INTERVIEW FOLLOW-UPS =================
    // - Why is it guaranteed that the write pointer never overtakes the read pointer?
    // - How do you correctly write a multi-digit count (like 12) as separate characters?
    // - What if a run's compressed form would actually be longer than the run itself - when could that happen?
    // - How would you decompress the compressed array back to the original string?
    // - What if the input could contain digits as actual characters (ambiguity with counts)?
    // - How would you handle Unicode or multi-byte characters in the array?
    // - Could you solve this without knowing the final length in advance, using a dynamic buffer instead?

    // Brute force: build result in a StringBuilder, then copy back. O(n) time, O(n) space.
    public static int compressBruteForce(char[] chars) {
        if (chars == null || chars.length == 0) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < chars.length) {
            char current = chars[i];
            int count = 0;
            // Count how many times the current character repeats consecutively.
            while (i < chars.length && chars[i] == current) {
                count++;
                i++;
            }
            sb.append(current);
            if (count > 1) {
                sb.append(count);
            }
        }
        // Copy the compressed characters back into the original array.
        for (int j = 0; j < sb.length(); j++) {
            chars[j] = sb.charAt(j);
        }
        return sb.length();
    }

    // Optimized: two pointers, in-place, O(1) extra space.
    public static int compressOptimized(char[] chars) {
        if (chars == null || chars.length == 0) {
            return 0;
        }
        int write = 0;  // where the next compressed output goes
        int read = 0;   // scans through runs of the same character

        while (read < chars.length) {
            char current = chars[read];
            int runStart = read;
            // Advance read while the character stays the same.
            while (read < chars.length && chars[read] == current) {
                read++;
            }
            int runLength = read - runStart;

            // Write the character itself.
            chars[write++] = current;

            // If the run is longer than 1, write each digit of the count separately.
            if (runLength > 1) {
                for (char digit : String.valueOf(runLength).toCharArray()) {
                    chars[write++] = digit;
                }
            }
        }
        return write;
    }

    public static void main(String[] args) {
        char[] chars1 = {'a', 'a', 'b', 'c', 'c', 'c', 'c', 'c', 'a', 'a', 'a'};
        // Expected: length 7, array starts with a2bc5a3
        System.out.println("Input: aabcccccaaa");
        int len1 = compressOptimized(chars1);
        System.out.println("Optimized output: " + new String(chars1, 0, len1) + " (length=" + len1 + ")");

        char[] chars2 = {'a', 'b', 'c'};
        // Expected: length 3, "abc" (no repeats, count 1 not written)
        System.out.println("\nInput: abc (no repeats)");
        int len2 = compressBruteForce(chars2);
        System.out.println("Brute force output: " + new String(chars2, 0, len2) + " (length=" + len2 + ")");

        char[] chars3 = new char[12];
        Arrays.fill(chars3, 'a');
        // Expected: length 3, "a12" (multi-digit count)
        System.out.println("\nInput: 12 a's in a row (multi-digit count)");
        int len3 = compressOptimized(chars3);
        System.out.println("Optimized output: " + new String(chars3, 0, len3) + " (length=" + len3 + ")");
    }
}
