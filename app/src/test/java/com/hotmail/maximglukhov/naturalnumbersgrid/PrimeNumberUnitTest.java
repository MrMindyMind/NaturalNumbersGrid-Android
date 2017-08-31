package com.hotmail.maximglukhov.naturalnumbersgrid;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrimeNumberUnitTest {

    /**
     * Used for testing any prime number, make sure to use an actual prime for proper testing.
     */
    public static final long PRIME_NUMBER_TO_TEST = 3;

    /*
     * General primality check for any number test.
     */
    @Test
    public void primalityCheck_isCorrect() throws Exception {
        assertEquals("Number "+ PRIME_NUMBER_TO_TEST + " failed primality test.", true, Utils.isPrime(PRIME_NUMBER_TO_TEST));
    }

    /*
     * The following tests are for numbers in different ranges.
     */

    @Test
    public void primalityCheck1_isCorrect() throws Exception {
        assertEquals("Prime 3 failed primality test.", true, Utils.isPrime(3));
    }

    @Test
    public void primalityCheck2_isCorrect() throws Exception {
        assertEquals("Prime 13 failed primality test.", true, Utils.isPrime(13));
    }

    @Test
    public void primalityCheck3_isCorrect() throws Exception {
        assertEquals("Prime 113 failed primality test.", true, Utils.isPrime(113));
    }

    @Test
    public void primalityCheck4_isCorrect() throws Exception {
        assertEquals("Prime 1009 failed primality test.", true, Utils.isPrime(1009));
    }

    @Test
    public void primalityCheck5_isCorrect() throws Exception {
        assertEquals("Prime 10007 failed primality test.", true, Utils.isPrime(10007));
    }

    @Test
    public void primalityCheck6_isCorrect() throws Exception {
        assertEquals("Prime 100003 failed primality test.", true, Utils.isPrime(100003));
    }

    @Test
    public void primalityCheck7_isCorrect() throws Exception {
        assertEquals("Prime 1000003 failed primality test.", true, Utils.isPrime(1000003));
    }

    @Test
    public void primalityCheck8_isCorrect() throws Exception {
        assertEquals("Prime 10000019 failed primality test.", true, Utils.isPrime(10000019));
    }

    @Test
    public void primalityCheck9_isCorrect() throws Exception {
        assertEquals("Prime 100000007 failed primality test.", true, Utils.isPrime(100000007));
    }

    @Test
    public void primalityCheck10_isCorrect() throws Exception {
        assertEquals("Prime 1000000007 failed primality test.", true, Utils.isPrime(1000000007));
    }

    @Test
    public void primalityCheck11_isCorrect() throws Exception {
        assertEquals("Prime 10000000019 failed primality test.", true, Utils.isPrime(10000000019L));
    }

    @Test
    public void primalityCheck12_isCorrect() throws Exception {
        assertEquals("Prime 100000000003 failed primality test.", true, Utils.isPrime(100000000003L));
    }
}
