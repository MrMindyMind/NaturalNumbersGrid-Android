package com.hotmail.maximglukhov.naturalnumbersgrid;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;
import java.util.Set;

/**
 * <p>Popup Box displays factors for given number.
 * <br>Can be placed anywhere on screen and works for all natural numbers.</p>
 */
public class FactorsPopupBox {

    /*
     * Factors title and text.
     */
    private TextView mFactorsTitleTextView;
    private TextView mFactorsTextView;

    /*
     * Holds reference to strings from resource.
     */
    private String mTitleString;
    private String mPrimeString;
    private String mZeroNumberString;
    private String mNoFactorsString;

    /*
     * Holds reference to root layout (for view references).
     */
    private ViewGroup mRoot;

    /**
     * <p>Constructor for Popup Box.</p>
     * @param context context.
     * @param root root view which includes title and text view.
     */
    public FactorsPopupBox(Context context, ViewGroup root) {
        init(context, root);
    }

    private void init(Context context, ViewGroup root) {
        mRoot                   = root;
        mFactorsTitleTextView   = root.findViewById(R.id.factorsTitleTextView);
        mFactorsTextView        = root.findViewById(R.id.factorsTextView);

        mTitleString        = context.getString(R.string.number_factors_title);
        mPrimeString        = context.getString(R.string.number_prime_factors);
        mZeroNumberString   = context.getString(R.string.number_0_factors);
        mNoFactorsString    = context.getString(R.string.number_no_factors);
    }

    /**
     * <p>Moves popup box by delta x.</p>
     * @param dx delta x to move by.
     */
    public void moveX(float dx) {
        mRoot.setX(mRoot.getX() + dx);
    }

    /**
     * <p>Moves popup box by delta y.</p>
     * @param dy delta y to move by.
     */
    public void moveY(float dy) {
        mRoot.setY(mRoot.getY() + dy);
    }

    /**
     * <p>Gets popup box' location on screen.</p>
     * @return coordinates on screen.
     */
    public PointF getLocation() {
        return new PointF(mRoot.getX(), mRoot.getY());
    }

    /**
     * <p>Measures popup box and returns dimensions.</p>
     * @return point containing width and height (x=width, y=height).
     */
    public Point getMeasuredDimensions() {
        mRoot.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        Point dimens = new Point();
        dimens.x = mRoot.getMeasuredWidth();
        dimens.y = mRoot.getMeasuredHeight();
        return dimens;
    }

    /**
     * <p>Shows popup box on screen for given number.</p>
     * @param location location on screen to show box.
     * @param number number to show factors for.
     * @param factors set containing number's factors.
     * @param isPrime indicate whether this number is a prime or not.
     */
    public void show(PointF location, long number, Set<Long> factors,
                     boolean isPrime) {
        if (factors == null)
            return;

        // Format title.
        String title = String.format(Locale.getDefault(),
                mTitleString, number, number);

        StringBuilder factorsString = new StringBuilder();
        if (number == 0) {
            // Show special string for zero (All natural numbers, so N).
            factorsString.append(mZeroNumberString);
        } else if (!isPrime) {
            if (factors == null || factors.isEmpty()) {
                // Show special string when no factors available (happens for number=1).
                factorsString.append(mNoFactorsString);
            } else {
                // Chain factors in a string.
                for (long factor : factors) {
                    factorsString.append(factor).append(", ");
                }
                // Remove last comma.
                factorsString.deleteCharAt(factorsString.length() - 2);
            }
        } else {
            // Show special string for primes (no factors).
            factorsString.append(mPrimeString);
        }

        // Update title and text.
        mFactorsTitleTextView.setText(title);
        mFactorsTextView.setText(factorsString.toString());

        // Set on screen location.
        mRoot.setX(location.x);
        mRoot.setY(location.y);

        // Finally, display box.
        mRoot.setVisibility(View.VISIBLE);
    }

    /**
     * <p>Shows popup box on screen for given number.
     * <br>Calculates factors and checks primality for given number.</p>
     * @param location location on screen to show box.
     * @param number number to show factors for.
     */
    public void show(PointF location, long number) {
        Set<Long> factors = null;
        boolean isPrime = Utils.isPrime(number);
        // Check if factors should be calculated.
        if (!isPrime)
            factors = Utils.factorize(number);

        show(location, number, factors, isPrime);
    }

    /**
     * <p>Shows popup box on screen for given number.
     * <br>Preferred method of showing as primality should be tested and factors calculated..</p>
     * @param location location on screen to show box.
     * @param cell cell to show.
     */
    public void show(PointF location, NumberCell cell) {
        show(location, cell.getValue(), cell.getFactors(), cell.isPrime());
    }

    /**
     * <p>Hides popup box from screen.</p>
     */
    public void hide() {
        if (mRoot.getVisibility() != View.GONE)
            mRoot.setVisibility(View.GONE);
    }
}
