/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.function.BiConsumer;

/**
 *
 * @author Benjamin
 */
public class ArithmeticLambdas {

    public static void main(String[] args) {
        int[] numbers = {3, 45, 6, 5, 34, 3, 2, 3, 1};

        int key = 0;

        process(numbers, key, (v, k) -> System.out.println(v / k));
    }

    private static void process(int num[], int key, BiConsumer<Integer, Integer> biConsumer) {
        for (int i : num) {
            biConsumer.accept(i, key);
        }
    }
}
