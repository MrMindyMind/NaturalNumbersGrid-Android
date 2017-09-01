package com.hotmail.maximglukhov.naturalnumbersgrid;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class FactorsUnitTest {

    @Test
    public void factorsCheck1_isCorrect() throws Exception {
        Set<Long> factors = Utils.factorize(100);
        Set<Long> correctFactors = new HashSet<>();
        correctFactors.add(2L);
        correctFactors.add(4L);
        correctFactors.add(5L);
        correctFactors.add(10L);
        correctFactors.add(20L);
        correctFactors.add(25L);
        correctFactors.add(50L);

        assertEquals(true, setEquals(factors, correctFactors));
    }

    @Test
    public void factorsCheck2_isCorrect() throws Exception {
        Set<Long> factors = Utils.factorize(1000);
        Set<Long> correctFactors = new HashSet<>();
        correctFactors.add(2L);
        correctFactors.add(4L);
        correctFactors.add(5L);
        correctFactors.add(8L);
        correctFactors.add(10L);
        correctFactors.add(20L);
        correctFactors.add(25L);
        correctFactors.add(40L);
        correctFactors.add(50L);
        correctFactors.add(100L);
        correctFactors.add(125L);
        correctFactors.add(200L);
        correctFactors.add(250L);
        correctFactors.add(500L);

        assertEquals(true, setEquals(factors, correctFactors));
    }

    @Test
    public void factorsCheck3_isCorrect() throws Exception {
        Set<Long> factors = Utils.factorize(159753);
        Set<Long> correctFactors = new HashSet<>();
        correctFactors.add(3L);
        correctFactors.add(11L);
        correctFactors.add(33L);
        correctFactors.add(47L);
        correctFactors.add(103L);
        correctFactors.add(141L);
        correctFactors.add(309L);
        correctFactors.add(517L);
        correctFactors.add(1133L);
        correctFactors.add(1551L);
        correctFactors.add(3399L);
        correctFactors.add(4841L);
        correctFactors.add(14523L);
        correctFactors.add(53251L);

        assertEquals(true, setEquals(factors, correctFactors));
    }

    @Test
    public void factorsCheck4_isCorrect() throws Exception {
        Set<Long> factors = Utils.factorize(3);
        Set<Long> correctFactors = new HashSet<>();

        assertEquals(true, setEquals(factors, correctFactors));
    }

    @Test
    public void factorsCheck5_isCorrect() throws Exception {
        Set<Long> factors = Utils.factorize(1949);
        Set<Long> correctFactors = new HashSet<>();

        assertEquals(true, setEquals(factors, correctFactors));
    }

    private boolean setEquals(Set set1, Set set2) {
        if (set1.size() != set2.size())
            return false;

        for (Object element1 : set1) {
            if (!set2.contains(element1))
                return false;
        }

        for (Object element2 : set2) {
            if (!set1.contains(element2))
                return false;
        }

        return true;
    }
}
