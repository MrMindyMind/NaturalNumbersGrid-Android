package com.hotmail.maximglukhov.naturalnumbersgrid;

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
}
