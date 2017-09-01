package com.hotmail.maximglukhov.naturalnumbersgrid;

import android.support.annotation.IntRange;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>Data class for {@link android.support.v7.widget.RecyclerView} containing cells, displaying all natural numbers.</p>
 */
public class NumberCell {

    /*
     * Cell integer value.
     */
    private final long mValue;

    /*
     * Defining if this value is a prime number.
     */
    private boolean mIsPrime;

    /*
     * Set containing all factors for the value assigned to this cell.
     */
    private Set<Long> mFactors;

    /**
     * <p>Constructor for NumberCell.</p>
     * @param value value to assign to this cell.
     */
    public NumberCell(@IntRange(from = 0) final long value) {
        mValue = value < 0 ? 0 : value;

        mFactors = new HashSet<>();
    }

    /**
     * <p>Tests if the value assigned to this cell is a prime using {@link Utils#isPrime(long)}.
     * <br>Updates primality property according to test result.</p>
     */
    public void updatePrimality() {
        mIsPrime = Utils.isPrime(mValue);
    }

    /**
     * <p>Calculates all factors for value assigned to this cell, excluding 1 and the value itself.
     */
    public void factorize() {
        if (!isPrime()) {
            mFactors = Utils.factorize(mValue);
        } else {
            mFactors.clear();
        }
    }

    /**
     * <p>Gets all factors for value assigned to this cell.
     * <br>May not be updated if {@link #updatePrimality()} and {@link #factorize()} were never called.
     * <br>Returns empty set for prime numbers.</p>
     * @return
     */
    public Set<Long> getFactors() {
        return mFactors;
    }

    /**
     * <p>Gets the current primality status for this value.<br>May not be updated if {@link #updatePrimality()} was never called.</p>
     * @return true if considered a prime, false otherwise.
     */
    public boolean isPrime() {
        return mIsPrime;
    }

    /**
     * <p>Gets assigned value to this cell.</p>
     * @return assigned value.
     */
    public long getValue() {
        return mValue;
    }

    @Override
    public String toString() {
        return Long.toString(mValue) + "(prime=" + mIsPrime + ')';
    }
}
