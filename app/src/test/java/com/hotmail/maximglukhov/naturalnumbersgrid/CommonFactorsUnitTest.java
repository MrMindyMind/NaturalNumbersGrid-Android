package com.hotmail.maximglukhov.naturalnumbersgrid;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class CommonFactorsUnitTest {

    @Test
    public void commonFactorsCheck1_isCorrect() throws Exception {
        Set<Long> factors1 = Utils.factorize(100);
        Set<Long> factors2 = Utils.factorize(20);

        assertEquals(true, Utils.hasCommonFactor(factors1, factors2));
    }

    @Test
    public void commonFactorsCheck2_isCorrect() throws Exception {
        Set<Long> factors1 = Utils.factorize(159654);
        Set<Long> factors2 = Utils.factorize(10);

        assertEquals(true, Utils.hasCommonFactor(factors1, factors2));
    }

    @Test
    public void commonFactorsCheck3_isCorrect() throws Exception {
        Set<Long> factors1 = Utils.factorize(500);
        Set<Long> factors2 = Utils.factorize(100);

        assertEquals(true, Utils.hasCommonFactor(factors1, factors2));
    }

    @Test
    public void commonFactorsCheck4_isCorrect() throws Exception {
        Set<Long> factors1 = Utils.factorize(52);
        Set<Long> factors2 = Utils.factorize(4);

        assertEquals(true, Utils.hasCommonFactor(factors1, factors2));
    }

    @Test
    public void commonFactorsCheck5_isCorrect() throws Exception {
        Set<Long> factors1 = Utils.factorize(999);
        Set<Long> factors2 = Utils.factorize(33);

        assertEquals(true, Utils.hasCommonFactor(factors1, factors2));
    }

    @Test
    public void commonFactorsCheck6_isCorrect() throws Exception {
        Set<Long> factors1 = Utils.factorize(7);
        Set<Long> factors2 = Utils.factorize(11);

        assertEquals(false, Utils.hasCommonFactor(factors1, factors2));
    }

    @Test
    public void commonFactorsCheck7_isCorrect() throws Exception {
        Set<Long> factors1 = Utils.factorize(90);
        Set<Long> factors2 = Utils.factorize(13);

        assertEquals(false, Utils.hasCommonFactor(factors1, factors2));
    }
}
