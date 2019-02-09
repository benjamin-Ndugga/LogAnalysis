package org.test;

import java.util.Arrays;

/**
 *
 * @author benjamin
 */
public class TestClass {

    public static void main(String[] args) {
        int[] input = {4, 2, 9, 6, 23, 12, 34, 0, 1};
        bubble_srt(input);
    }

    public static void bubble_srt(int array[]) {
        int n = array.length;
        int rightIdx;

        for (int m = n; m >= 0; m--) {
            for (int leftIdx = 0; leftIdx < n - 1; leftIdx++) {
                rightIdx = leftIdx + 1;
                if (array[leftIdx] > array[rightIdx]) {
                    swapNumbers(leftIdx, rightIdx, array);
                }
            }
            printNumbers(array);
        }
    }

    private static void swapNumbers(int leftIdx, int rightIdx, int[] array) {

        int temp;
        temp = array[leftIdx];
        array[leftIdx] = array[rightIdx];
        array[rightIdx] = temp;
    }

    private static void printNumbers(int[] input) {

        for (int i = 0; i < input.length; i++) {
            System.out.print(input[i] + ", ");
        }
        System.out.println("\n");
    }

}
