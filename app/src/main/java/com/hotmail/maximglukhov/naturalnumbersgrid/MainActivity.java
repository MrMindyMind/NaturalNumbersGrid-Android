package com.hotmail.maximglukhov.naturalnumbersgrid;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NumberCellGeneratorTask.NumberCellGeneratorListener {

    private static final String LOG_TAG = "MainActivity";

    /*
     * Defines default amount of columns (span) for grid layout.
     */
    private static final int DEFAULT_COLUMN_COUNT = 10;

    // TEMPORARY
    private static final int ADDITIONAL_ITEMS = 200;

    /*
     * RecyclerView for entire grid.
     */
    private RecyclerView mNumbersGridRecyclerView;

    /*
     * Grid number cells generator task (runs on separate thread).
     */
    private NumberCellGeneratorTask mGenerator;

    /*
     * Indicates that there's a pending task to complete (insertion/removal)
     */
    private boolean mIsLoading;

    /*
     * Indicates that extra memory was loaded for infinite scroll effect.
     */
    private boolean mIsOverDraft;

    /*
     * Indicate whether the grid is currently highlighted.
     */
    private boolean mIsHighlighted;
    /*
     * Holds reference to current common factors (long press on a cell).
     */
    private Set<Long> mSelectedFactors;

    private RecyclerView.OnScrollListener mRecyclerViewScrollListener =
            new RecyclerView.OnScrollListener() {

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx,
                               final int dy) {
            super.onScrolled(recyclerView, dx, dy);

            Log.d(LOG_TAG, "onScrolled :: isLoading=" + mIsLoading + " dx=" + dx + " dy=" + dy);

            // Don't do anything if we are still loading additional items.
            if (mIsLoading)
                return;

            final GridLayoutManager gridLayoutManager = (GridLayoutManager)
                    mNumbersGridRecyclerView.getLayoutManager();
            final NumberCellAdapter adapter = (NumberCellAdapter)
                    mNumbersGridRecyclerView.getAdapter();

            /*
             * Calculate visible items.
             */
            final int lastCompletelyVisibleItem = gridLayoutManager.findLastCompletelyVisibleItemPosition();
            final int firstCompletelyVisibleItem = gridLayoutManager.findFirstCompletelyVisibleItemPosition();

            final int itemCount = adapter.getData().size();

            Log.d(LOG_TAG, "onScrolled :: firstV:" + firstCompletelyVisibleItem + " lastV:" + lastCompletelyVisibleItem + " T:" + itemCount + " overdraft?" + mIsOverDraft);

            /*
             * Calculate thresholds for scrolling up (higher) and down (lower).
             */
            final int lowerThreshold;
            final int higherThreshold;
            if (mIsOverDraft) {
                // Set lower threshold as half of original item count + added items
                lowerThreshold = itemCount - (itemCount - ADDITIONAL_ITEMS) / 2;
                // Set high threshold as half of all items.
                higherThreshold = (itemCount - ADDITIONAL_ITEMS) / 2;
            } else {
                // Set lower and higher threshold as 50% of current items.
                lowerThreshold = itemCount / 2;
                higherThreshold = lowerThreshold;
            }

            Log.d(LOG_TAG, "onScrolled :: threshold:" + lowerThreshold + " thresholdPercentage:" + (lowerThreshold*100 / (float)itemCount));

            // Scroll down.
            if (dy > 0) {
                // Passed threshold.
                if (lastCompletelyVisibleItem >= lowerThreshold) {
                    Log.d(LOG_TAG, "onScrolled :: passed threshold on scroll down");
                    // Check if we already loaded extra cells.
                    if (mIsOverDraft) {
                        // Flag as no longer in overdraft since we are removing the cells.
                        mIsOverDraft = false;
                        // Flag as loading as we have another pending transaction.
                        mIsLoading = true;

                        Log.d(LOG_TAG, "onScrolled :: posting with removal of cells: 0-x");
                        // Post about update.
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                // Remove previous added items.
                                adapter.removeCells(0, ADDITIONAL_ITEMS);
                                Log.d(LOG_TAG, "onScrolled :: run :: notifying range removed (0-x).");
                                // Notify adapter.
                                adapter.notifyItemRangeRemoved(0, ADDITIONAL_ITEMS);
                                // Flag as no longer loading as our transaction has ended.
                                mIsLoading = false;
                                Log.d("MainActivity", "onScrolled :: run :: success.");
                                // Check if we are at the bottom of the current list and trigger additional loading if we are.
                                if (lastCompletelyVisibleItem >= (lowerThreshold - ADDITIONAL_ITEMS)) {
                                    Log.d("MainActivity", "onScrolled :: run :: running onScrolled once again.");
                                    onScrolled(recyclerView, dx, dy);
                                }
                            }
                        });

                        // Exit early to avoid adding more items.
                        return;
                    }

                    // Not at overdraft, simply add more items to the bottom.

                    // Flag as loading.
                    mIsLoading = true;

                    Log.d(LOG_TAG, "onScrolled :: starting generator.");
                    // Buffer additional items for infinite scrolling experience.
                    mGenerator.setRangeStart(adapter.getData().get(itemCount - 1)
                            .getValue() + 1);
                    mGenerator.setDirection(true);
                    mGenerator.start(ADDITIONAL_ITEMS);

                    // Flag that we currently have more items than usual, and they must be removed at some point before adding more items.
                    mIsOverDraft = true;
                }
            }

            // Scroll up
            if (dy < 0) {
                if (firstCompletelyVisibleItem <= higherThreshold) {
                    Log.d(LOG_TAG, "onScrolled :: passed threshold on scrolling up");

                    // Find lowest value in data.
                    final long lowest = adapter.getData().get(0).getValue();

                    // Check if we have extra cells.
                    if (mIsOverDraft) {
                        // Find first index of extra cells.
                        final int removeIndex = itemCount - ADDITIONAL_ITEMS;

                        // Flag that we no longer have extra cells.
                        mIsOverDraft = false;
                        // Flag as loading since we are about to post and have a pending transaction.
                        mIsLoading = true;
                        Log.d(LOG_TAG, "onScrolled :: posting overdraft removal.");
                        // Post about update.
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(LOG_TAG, "onScrolled :: run :: removing extra cells: (" + removeIndex + "-" + removeIndex + "+x).");
                                // Remove previous added items.
                                adapter.removeCells(removeIndex, ADDITIONAL_ITEMS);
                                Log.d(LOG_TAG, "onScrolled :: run :: notifying range removed (" + removeIndex + "-" + removeIndex + "+x).");
                                adapter.notifyItemRangeRemoved(removeIndex, ADDITIONAL_ITEMS);

                                // Flag as no longer loading.
                                mIsLoading = false;

                                Log.d(LOG_TAG, "onScrolled :: run :: success.");

                                // Check if we need to load more on the way up. This is true as long as the first item in data is not 0.
                                if (lowest > NumberCellGeneratorTask.MIN_NUMBER) {
                                    // Flag as loading.
                                    mIsLoading = true;

                                    Log.d(LOG_TAG, "onScrolled :: starting generator (negative).");
                                    // Buffer additional items for infinite scrolling experience.
                                    mGenerator.setRangeStart(lowest - 1);
                                    mGenerator.setDirection(false);
                                    mGenerator.start(ADDITIONAL_ITEMS);

                                    // Flag that we currently have more items than usual, and they must be removed at some point before adding more items.
                                    mIsOverDraft = true;
                                }

                            }
                        });

                        // Exit early to avoid adding more items (to the beginning).
                        return;
                    }

                    // Not in overdraft, check if we need to add any more items (first item is not 0).
                    if (lowest > NumberCellGeneratorTask.MIN_NUMBER) {
                        // Flag as loading.
                        mIsLoading = true;

                        Log.d(LOG_TAG, "onScrolled :: starting generator (opposite).");
                        // Buffer additional items for infinite scrolling experience
                        mGenerator.setRangeStart(lowest - 1);
                        mGenerator.setDirection(false);
                        mGenerator.start(ADDITIONAL_ITEMS);

                        // Flag that we currently have more items than usual, and they must be removed at some point before adding more items.
                        mIsOverDraft = true;
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create gesture detector for natural numbers grid.
        final GestureDetectorCompat numbersGridGestureDetector =
                new GestureDetectorCompat(MainActivity.this,
                        new GestureDetector.OnGestureListener() {

            @Override
            public boolean onDown(MotionEvent motionEvent) { return true; }

            @Override
            public void onShowPress(MotionEvent motionEvent) {}

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) { return true; }

            @Override
            public boolean onScroll(MotionEvent motionEvent,
                                    MotionEvent motionEvent1,
                                    float v, float v1) { return false; }

            @Override
            public void onLongPress(MotionEvent motionEvent) {
                Log.d(LOG_TAG, "OnGestureListener :: onLongPress :: e=" + motionEvent);
                // Find child's root view matching touch coordinates.
                View layout = mNumbersGridRecyclerView.findChildViewUnder(
                        motionEvent.getX(), motionEvent.getY());
                // Find adapter's position for this layout.
                int childPos = mNumbersGridRecyclerView
                        .getChildAdapterPosition(layout);
                // Get cell data for this child.
                NumberCellAdapter adapter = (NumberCellAdapter)
                        mNumbersGridRecyclerView.getAdapter();
                NumberCell cellData = adapter.getData().get(childPos);
                Log.d(LOG_TAG, "OnGestureListener :: onLongPress :: child=" + cellData + " factors=" + cellData.getFactors());

                // Avoid any operations for primes and 0,1,2,3. No cells should highlight for these values.
                if (!cellData.isPrime() && (cellData.getValue() > 3)) {
                    mSelectedFactors = cellData.getFactors();
                    toggleHighlightForVisibleItems(true, mSelectedFactors);
                    mIsHighlighted = true;
                }
            }

            @Override
            public boolean onFling(MotionEvent motionEvent,
                                   MotionEvent motionEvent1,
                                   float v, float v1) { return false; }
        });

        /*
         * Initialize grid RecyclerView.
         */
        mNumbersGridRecyclerView = (RecyclerView) findViewById(
                R.id.numbersGridRecyclerView);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(
                MainActivity.this, DEFAULT_COLUMN_COUNT,
                LinearLayoutManager.VERTICAL, false);
        mNumbersGridRecyclerView.setLayoutManager(gridLayoutManager);
        mNumbersGridRecyclerView.setAdapter(new NumberCellAdapter(DEFAULT_COLUMN_COUNT));
        mNumbersGridRecyclerView.addOnScrollListener(mRecyclerViewScrollListener);
        mNumbersGridRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                // Check if user has long pressed.
                numbersGridGestureDetector.onTouchEvent(e);

                if (e.getAction() == MotionEvent.ACTION_UP) {
                    // Return all cells to original highlight state.
                    if (mIsHighlighted) {
                        toggleHighlightForVisibleItems(false, null);
                        mSelectedFactors = null;
                        mIsHighlighted = false;
                    }
                } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    // Highlight new visible cells as user scrolls.
                    if (mIsHighlighted && (mSelectedFactors != null))
                        toggleHighlightForVisibleItems(true, mSelectedFactors);
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        /*
         * Initialize number generator.
         */
        mGenerator = new NumberCellGeneratorTask(0, 300, true, this,
                new Handler(getMainLooper()));
        mGenerator.start();
    }

    @Override
    public void onCellsReady(final NumberCell[] cells) {
        Log.d(LOG_TAG, "onCellsReady :: called with cells: "
                + Utils.getStringForArray(cells));

        final NumberCellAdapter adapter =
                (NumberCellAdapter) mNumbersGridRecyclerView.getAdapter();

        final int indexStart;
        if (mGenerator.getDirection()) {
            // Add cells to the end as the direction is positive (scrolling down).
            indexStart = adapter.getData().size() - 1;
        } else {
            // Insert cells to the start as the direction is negative (scrolling up).
            indexStart = 0;
        }

        Log.d(LOG_TAG, "onCellsReady :: posting with indexStart=" + indexStart);

        // Post task to ensure RecyclerView is ready.
        mNumbersGridRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "onCellsReady :: run :: inserting " + cells.length + " at index=" + indexStart);
                int added = adapter.insertCells(cells, indexStart, cells.length);
                Log.d(LOG_TAG, "onCellsReady :: run :: notifying range added (" + indexStart + "-" +(indexStart + added)+").");
                adapter.notifyItemRangeInserted(indexStart, added);

                // Flag as finished loading.
                mIsLoading = false;

                Log.d(LOG_TAG, "onCellsReady :: run :: success.");
            }
        });
    }

    /*
     * Helper methods to highlight all visible items with common factor.
     */
    private void toggleHighlightForVisibleItems(boolean toggle, Set<Long> factors) {
        if (mNumbersGridRecyclerView == null)
            return;

        // Iterate through all visible items.
        int childCount = mNumbersGridRecyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = mNumbersGridRecyclerView.getChildAt(i);
            if (child == null)
                continue;

            // Get cell view and data.
            NumberCellTextView cellView = child.findViewById(R.id.numberTextView);
            NumberCell childData = cellView.getCellData();
            if (toggle) {
                // Check if the number itself is a factor.
                if (factors.contains(childData.getValue())) {
                    cellView.highlight(true);
                } else {
                    // Check if any of the factors match.
                    if (Utils.hasCommonFactor(factors, childData.getFactors())) {
                        cellView.highlight(true);
                    }
                }
            } else {
                cellView.highlight(false);
            }
        }
    }
}
