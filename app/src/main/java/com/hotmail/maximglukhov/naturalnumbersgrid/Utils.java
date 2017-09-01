package com.hotmail.maximglukhov.naturalnumbersgrid;

import java.util.HashSet;
import java.util.Set;

public class Utils {

    /**
     * <p>Checks if a number is a given number is prime with O(sqrt(n)) run-time complexity.
     *
     * @param n Number to check.
     * @return True if prime, false otherwise.
     */
    public static boolean isPrime(long n) {
        // 0,1 are not primes.
        if (n < 2)
            return false;
        // 2,3 are primes.
        if (n < 4)
            return true;
        // Any number divisible by 2 or 3 is not a prime.
        if (((n % 2) == 0) || ((n % 3) == 0))
            return false;

        // Simple primality check algorithm for not very large numbers.
        long i = 5;
        while ((i * i) <= n) {
            if (((n % i) == 0) || ((n % (i + 2)) == 0))
                return false;

            i = i + 6;
        }

        return true;
    }

    /**
     * <p>Reverses array's order of items.</p>
     * @param array array to reverse.
     */
    public static void reverseArray(Object[] array) {
        int halfLength = array.length / 2;
        // Iterate only to half length to achieve actual reverse using swap.
        for (int i = 0; i < halfLength; i++) {
            swap(array, i, array.length - i - 1);
        }

    }

    /**
     * <p>Swaps two values in given array by their indices.</p>
     * @param array array to swap values for.
     * @param first first index.
     * @param second second index.
     */
    public static void swap(Object[] array, int first, int second) {
        Object tmp = array[first];
        array[first] = array[second];
        array[second] = tmp;
    }

    /**
     * <p>Builds string for given array.</p>
     * @param arr array to build string for.
     * @return string containing all if array's elements.
     */
    public static String getStringForArray(Object[] arr) {
        StringBuilder stringBuilder = new StringBuilder('[');
        // Iterate through all elements.
        for (Object element : arr) {
            // Append only non-null elements.
            if (arr != null) {
                stringBuilder.append(element);
                stringBuilder.append(',');
            }
        }
        // Remove last comma.
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        stringBuilder.append(']');

        return stringBuilder.toString();
    }

    /**
     * <p>Factorizes given number and returns all factors (excluding 1 and n).</p>
     * @param n number to factor.
     * @return set containing all factors of given number.
     */
    public static Set<Long> factorize(long n) {
        Set<Long> factors = new HashSet<>();

        long lim = (long) Math.sqrt(n);

        // Skip 1 (and n) as factors.
        for (long i = 2; i <= lim; i++) {
            if ((n % i) == 0) {
                factors.add(i);
                factors.add(n / i);
            }
        }

        return factors;
    }

    /**
     * <p>Compares given sets of factors. Checks if at least one common factor is found among the two sets.
     * <br>Returns true if at least one common factor is found. Returns false if no common factors at all.</p>
     * @param factors1 first set.
     * @param factors2 second set.
     * @return true if at least one common factor is found. Returns false if no common factors at all.
     */
    public static boolean hasCommonFactor(Set<Long> factors1, Set<Long> factors2) {
        if (factors1 == null || factors2 == null)
            return false;

        for (long factor : factors1) {
            if (factors2.contains(factor))
                return true;
        }

        for (long factor : factors2) {
            if (factors1.contains(factor))
                return true;
        }

        return false;
    }
}
