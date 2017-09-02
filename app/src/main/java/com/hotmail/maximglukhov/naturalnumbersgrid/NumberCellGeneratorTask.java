package com.hotmail.maximglukhov.naturalnumbersgrid;

import android.os.Handler;
import android.support.annotation.IntRange;
import android.util.Log;

/**
 * <p>Task for generating natural numbers with primality tested.<br>Implemented as a worker thread.</p>
 */
public class NumberCellGeneratorTask implements Runnable {

    private static final String LOG_TAG = "NumberCellGeneratorTask";

    /*
     * Minimum number to generate.
     */
    public static final int MIN_NUMBER = 0;

    /*
     * Minimum number range.
     */
    public static final int MIN_RANGE = 1;

    /*
     * Defines name for task's thread.
     */
    private static final String THREAD_NAME = "NumberCellGeneratorTask";

    /**
     * <p>Interface definition for a callback to be invoked when cells are ready.<br>
     */
    public interface NumberCellGeneratorListener {
        /**
         * <p>Callback for delivering ready cell results.<br>
         *     Called when given range of cells have been generated with value assigned and primality tested.</p>
         * @param cells Ready cells.
         */
        void onCellsReady(NumberCell[] cells);
    }

    /*
     * Listener for implementing callback.
     */
    private NumberCellGeneratorListener mListener;

    /*
     * Current running thread for the task.
     */
    private Thread mCurrentThread;

    /*
     * Reference to UI Thread for delivering results.
     */
    private final Handler mUiHandler;

    /*
     * Defines whether the task is running or at idle.
     */
    private boolean mIsGenerating;

    /*
     * Defines number generation range.
     */
    private long mRangeStart;
    private int mRange;

    /*
     * Defines whether to generate increasing numbers or decreasing numbers.
     */
    private boolean mIsPositive;

    /**
     * <p>Constructor for {@link NumberCell} generating task.</p>
     * @param start first number to generate up to given range.
     * @param range range of numbers to generate from given number.
     * @param positive generation direction - increase (true) or decrease (false).
     * @param listener listener to deliver results to.
     * @param uiHandler UI Handler to post results to.
     */
    public NumberCellGeneratorTask(@IntRange(from = MIN_NUMBER) long start,
                                   @IntRange(from = MIN_RANGE) int range,
                                   boolean positive,
                                   NumberCellGeneratorListener listener,
                                   Handler uiHandler) {
        mListener   = listener;
        mUiHandler  = uiHandler;

        setRange(start, range, positive);
    }

    @Override
    public void run() {
        // Set thread as background thread.
        android.os.Process.setThreadPriority(
                android.os.Process.THREAD_PRIORITY_BACKGROUND);

        final NumberCell[] cells = new NumberCell[mRange];
        for (int i = 0; i < mRange; i++) {
            // Stop generating below 0.
            if (mRangeStart == -1 && !mIsPositive) {
                // Indicate the previous cell is the last valid one.
                cells[i] = null;
                break;
            }

            // Generate new cell with primality tested.
            NumberCell cell = new NumberCell(mIsPositive ? mRangeStart++ : mRangeStart--);
            cell.updatePrimality();
            cell.factorize();
            cells[i] = cell;
        }

        // DEBUG: print generated cells
        Log.d(LOG_TAG, "run :: finished generating cells: " + Utils.getStringForArray(cells));

        if (!mIsPositive) {
            // Change direction of cells in the array to increasing.
            Utils.reverseArray(cells);

            // DEBUG: print flipped array.
            Log.d(LOG_TAG, "run :: finished generating cells (flipped): " + Utils.getStringForArray(cells));
        }

        // Post results to listener.
        if (mListener != null) {
            // Run on UI Thread.
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.onCellsReady(cells);
                }
            });
        }

        // Stop current thread.
        idle();
    }

    /**
     * <p>Sets number generation task range parameters.</p>
     * @param start first number to generate up to given range.
     * @param range range of numbers to generate.
     * @param positive generation direction - increase (true) or decrease (false).
     */
    public void setRange(@IntRange(from = MIN_NUMBER) long start,
                         @IntRange(from = MIN_RANGE) int range,
                         boolean positive) {
        if (start < MIN_NUMBER)
            start = MIN_NUMBER;

        if (range < MIN_RANGE)
            range = MIN_RANGE;

        mRangeStart = start;
        mRange      = range;
        mIsPositive = positive;
    }

    /**
     * <p>Sets number generation range</p>
     * @param range range to generate up to from current number.
     */
    public void setRange(@IntRange(from = MIN_RANGE) int range) {
        setRange(mRangeStart, range, mIsPositive);
    }

    /**
     * <p>Gets number generation range.</p>
     * @return range to generate up to from current number.
     */
    public int getRange() {
        return mRange;
    }

    /**
     * <p>Sets range start number.</p>
     * @param start
     */
    public void setRangeStart(@IntRange(from = MIN_NUMBER) long start) {
        setRange(start, mRange, mIsPositive);
    }

    /**
     * <p>Gets range start.<br>This is the first number to be generated when task will run again.</p>
     * @return range start.
     */
    public long getRangeStart() {
        return mRangeStart;
    }

    /**
     * <p>Sets generation direction.<br>
     * Set to true for increasing numbers or false for decreasing numbers.</p>
     * @param positive generation direction - increase (true) or decrease (false).
     */
    public void setDirection(boolean positive) {
        setRange(mRangeStart, mRange, positive);
    }

    /**
     * <p>Gets generation direction.</p>
     * @return true if direction is increasing or false if decreasing.
     */
    public boolean getDirection() {
        return mIsPositive;
    }

    /**
     * <p>Checks if generator is current running.</p>
     * @return true if generating, false otherwise.
     */
    public boolean isGenerating() {
        return mIsGenerating;
    }

    /**
     * <p>Sets listener for delivering number generation results.</p>
     * @param listener implementing listener.
     */
    public void setListener(NumberCellGeneratorListener listener) {
        mListener = listener;
    }

    /**
     * <p>Gets current assigned listener.</p>
     * @return current assigned listener.
     */
    public NumberCellGeneratorListener getListener() {
        return mListener;
    }

    /**
     * <p>Starts generation task.<br>Does nothing if already generating.</p>
     * @param rangeStart first number to generate.
     * @param range range of numbers to generate.
     * @param direction generation direction - increase (true) or decrease (false).
     */
    public void start(long rangeStart, int range, boolean direction) {
        if (!mIsGenerating) {
            mIsGenerating = true;

            setRange(rangeStart, range, direction);

            mCurrentThread = new Thread(this, THREAD_NAME);
            mCurrentThread.start();
        }
    }

    /**
     * <p>Starts generation task for given range. Continues from last generated value + 1
     * <br>Does nothing if already generating.</p>
     * @param range range to generate from last generated value + 1
     */
    public void start(int range) {
        start(mRangeStart, range, mIsPositive);
    }

    /**
     * <p>Starts generation task. Continues from last generated value + 1 up to last range.
     * <br>Does nothing if already generating.</p>
     */
    public void start() {
        start(mRangeStart, mRange, mIsPositive);
    }

    /**
     * <p>Sets task as idle.</p>
     */
    public void idle() {
        // Flag as idling.
        mIsGenerating = false;

        if (mCurrentThread != null && Thread.currentThread()
                != mUiHandler.getLooper().getThread()) {
            // Join spawned thread.
            boolean retry = true;
            while (retry) {
                try {
                    mCurrentThread.join();
                    retry = false;
                    mCurrentThread = null;
                } catch (InterruptedException e) {
                    // Keep trying to join.
                }
            }
        }
    }

}
