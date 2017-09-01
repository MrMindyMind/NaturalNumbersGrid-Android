package com.hotmail.maximglukhov.naturalnumbersgrid;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * <p>Custom view displaying cell with an integer in the center of it.
 * <br>Changes background according to number's primality property. Also allows highlight (different background)
 * if required.</p>
 */
public class NumberCellTextView extends AppCompatTextView {

    /**
     * <p>Defines cell's background type (affects color).
     * <br>{@link #TYPE_COMPOSITE} - background for non-prime numbers (uses {@link R.drawable#cell_background_composite})
     * <br>{@link #TYPE_PRIME} - background for non-prime numbers (uses {@link R.drawable#cell_background_prime})
     * <br>{@link #TYPE_HIGHLIGHT} - background for non-prime numbers (uses {@link R.drawable#cell_background_highlight})</p>
     */
    public enum CellBackgroundType {
        TYPE_COMPOSITE,
        TYPE_PRIME,
        TYPE_HIGHLIGHT
    }

    /*
     * Data class holding information for this cell view.
     */
    private NumberCell mCellData;

    /*
     * Defines whether the background is currently highlighted.
     */
    private boolean mIsHighlighted;

    public NumberCellTextView(Context context, @Nullable AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NumberCellTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberCellTextView(Context context) {
        this(context, null);
    }

    /**
     * <p>Sets cell's data.</p>
     * @param data data to set.
     */
    public void setCellData(NumberCell data) {
        mCellData = data;

        // Update text to match data.
        setText(Long.toString(data.getValue()));

        // Update background to match type.
        if (data.isPrime())
            setCellBackground(CellBackgroundType.TYPE_PRIME);
        else
            setCellBackground(CellBackgroundType.TYPE_COMPOSITE);
    }

    /**
     * <p>Gets cell's data.</p>
     * @return cell's data.
     */
    public NumberCell getCellData() {
        return mCellData;
    }

    /**
     * <p>Changes cells background color according to toggle.
     * <br>Setting true changes background to {@link R.drawable#cell_background_highlight}.
     * <br>Setting false changes background to cell's {@link CellBackgroundType}.</p>
     * @param toggle toggle highlight.
     */
    public void highlight(boolean toggle) {
        mIsHighlighted = toggle;
        if (toggle) {
            // Highlight background.
            setCellBackground(CellBackgroundType.TYPE_HIGHLIGHT);
        } else {
            // Return to original background.
            if (mCellData.isPrime())
                setCellBackground(CellBackgroundType.TYPE_PRIME);
            else
                setCellBackground(CellBackgroundType.TYPE_COMPOSITE);
        }
    }

    /**
     * <p>Toggles cell's background highlight.
     * <br>If highlighted, calls {@link #highlight(boolean)} with false. Calls it with true otherwise.</p>
     */
    public void toggleHighlight() {
        if (mIsHighlighted) {
            highlight(false);
        } else {
            highlight(true);
        }
    }

    /*
     * Helper method to set cell's background according to type.
     */
    private void setCellBackground(CellBackgroundType type) {
        switch (type) {
            case TYPE_COMPOSITE:
                setBackgroundResource(R.drawable.cell_background_composite);
                break;
            case TYPE_PRIME:
                setBackgroundResource(R.drawable.cell_background_prime);
                break;
            case TYPE_HIGHLIGHT:
                setBackgroundResource(R.drawable.cell_background_highlight);
                break;
        }
    }
}
