package com.hotmail.maximglukhov.naturalnumbersgrid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NumberCellGeneratorTask.NumberCellGeneratorListener {

    private static final String LOG_TAG = "MainActivity";

    /*
     * Defines default amount of columns (span) for grid layout.
     */
    private static final int DEFAULT_COLUMN_COUNT = 10;

    /*
     * Defines default buffer size.
     */
    private static final int DEFAULT_BUFFER_SIZE = 100;

    private static final int REQUEST_CODE_ACTIVITY_SETTINGS = 0;

    /*
     * Buffer size for loading extra cells into memory beforehand.
     */
    private int mBufferSize = DEFAULT_BUFFER_SIZE;
    /*
     * Extra cells is calculated as (bufferSize * spanCount).
     */
    private int mExtraCells;

    /*
     * Popup box for factors displaying.
     */

    private FactorsPopupBox mFactorsPopupBox;

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
     * Holds reference to all highlighted views.
     * This is required because some views might no longer be visible when holding & scrolling,
     * and resulting in their background not updating properly.
     */
    private ArrayList<NumberCellTextView> mHighlightedCells;
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
                lowerThreshold = itemCount - (itemCount - mExtraCells) / 2;
                // Set high threshold as half of all items.
                higherThreshold = (itemCount - mExtraCells) / 2;
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
                                adapter.removeCells(0, mExtraCells);
                                Log.d(LOG_TAG, "onScrolled :: run :: notifying range removed (0-x).");
                                // Notify adapter.
                                adapter.notifyItemRangeRemoved(0, mExtraCells);
                                // Flag as no longer loading as our transaction has ended.
                                mIsLoading = false;
                                Log.d("MainActivity", "onScrolled :: run :: success.");
                                // Check if we are at the bottom of the current list and trigger additional loading if we are.
                                if (lastCompletelyVisibleItem >= (lowerThreshold - mExtraCells)) {
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
                    mGenerator.start(mExtraCells);

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
                        final int removeIndex = itemCount - mExtraCells;

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
                                adapter.removeCells(removeIndex, mExtraCells);
                                Log.d(LOG_TAG, "onScrolled :: run :: notifying range removed (" + removeIndex + "-" + removeIndex + "+x).");
                                adapter.notifyItemRangeRemoved(removeIndex, mExtraCells);

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
                                    mGenerator.start(mExtraCells);

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
                        mGenerator.start(mExtraCells);

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

        mHighlightedCells = new ArrayList<>();

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

                /*
                 * Code below handles popup box position calculating.
                 * The goal is displaying the box on top of the cell properly, and centered right above it.
                 * Handles edge cases where box might be obscured by screen boundaries, and even user's finger.
                 */

                // Get span count to check if popup box is displayed under user's finger (first row).
                int spanCount = ((GridLayoutManager) mNumbersGridRecyclerView.
                        getLayoutManager()).getSpanCount();

                // Get view's location on screen.
                int[] childLocation = new int[2];
                layout.getLocationInWindow(childLocation);
                // Get dimensions for popup box.
                Point boxDimens = mFactorsPopupBox.getMeasuredDimensions();
                // Get screen dimensions to avoid obscuring by bezels.
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                // Initialize popup box location as view's location on screen.
                PointF location = new PointF(childLocation[0], childLocation[1]);
                // Check if popup box is below first row, only then place it above (this is the part where user's finger could obscure the box).
                if (childPos >= spanCount)
                    location.y = Math.max(location.y - (layout.getHeight() + boxDimens.y), 0);
                // Update horizontal location to be centered above selected cell, or at least 0.
                location.x = Math.max(location.x - (boxDimens.x/2.0f) + layout.getWidth() / 2.0f, 0);
                // Check if popup box will be out of screen bounds and move it to the left.
                float diff = metrics.widthPixels - (location.x + boxDimens.x);
                // NOTE: despite using addition, diff is negative. Resulting in box moving to the left.
                if (diff < 0)
                    location.x += diff;
                // Finally, show the box at its proper location on screen.
                mFactorsPopupBox.show(location, cellData);
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

            /*
             * Keep reference of previous y pos for delta y calculation.
             */
            private float oldY = -1.0f;

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

                        // Set old y as -1 to reset delta calculation.
                        oldY = -1.0f;
                    }

                    mFactorsPopupBox.hide();
                } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    // Highlight new visible cells as user scrolls.
                    if (mIsHighlighted && (mSelectedFactors != null)) {
                        toggleHighlightForVisibleItems(true, mSelectedFactors);

                        /*
                         * Code below makes sure popup box stays above selected cell.
                         */

                        if (oldY < 0) {
                            // Initialize y (first movement on screen since reset or at all).
                            oldY = e.getY();
                        } else {
                            // Calculate delta so we can move the popup box later.
                            float dy = e.getY() - oldY;
                            // Check if the result of moving the box will stay above 0 (visible).
                            boolean isBelowTop = ((mFactorsPopupBox.getLocation().y + dy) > 0);
                            // Get the very first visible cell position.
                            int firstVisible = gridLayoutManager.findFirstCompletelyVisibleItemPosition();
                            // Check if the result of moving the box will make it no longer above selected cell.
                            boolean isTopScrollable = (dy < 0 || firstVisible > 0);

                            if (isBelowTop && isTopScrollable) {
                                // Finally, move the box by delta y.
                                mFactorsPopupBox.moveY(dy);
                                // Note: only updating oldY when we make any movement(*). This makes the box move smoothly rather than jumping around.
                                oldY = e.getY();
                            }

                            // (*) Update oldY when we can't scroll up, because if user tries to scroll down, popup box should move along.
                            if (!isTopScrollable)
                                oldY = e.getY();
                        }
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        mFactorsPopupBox = new FactorsPopupBox(MainActivity.this,
                (ViewGroup) findViewById(R.id.factorsBoxLayout));

        /*
         * Initialize number generator.
         */
        mGenerator = new NumberCellGeneratorTask(0, 300, true, this,
                new Handler(getMainLooper()));

        syncPreferences();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemSettings:
                Intent intent = new Intent(MainActivity.this,
                        SettingsActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ACTIVITY_SETTINGS);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG," onActivityResult :: requestCode=" + requestCode + " resultCode=" + resultCode);

        switch (requestCode) {
            case REQUEST_CODE_ACTIVITY_SETTINGS:
                if (resultCode == RESULT_OK) {
                    // Preferences have been updated, apply changes.
                    syncPreferences();
                }
                break;
        }
    }

    /*
     * Helper methods to highlight all visible items with common factor.
     */
    private void toggleHighlightForVisibleItems(boolean toggle, Set<Long> factors) {
        if (mNumbersGridRecyclerView == null)
            return;

        if (toggle) {
            // Iterate through all visible items.
            int childCount = mNumbersGridRecyclerView.getChildCount();
            boolean highlight;
            for (int i = 0; i < childCount; i++) {
                highlight = false;
                View child = mNumbersGridRecyclerView.getChildAt(i);
                if (child == null)
                    continue;

                // Get cell view and data.
                NumberCellTextView cellView = child
                        .findViewById(R.id.numberTextView);
                NumberCell childData = cellView.getCellData();
                // Check if the number itself is a factor.
                if (factors.contains(childData.getValue())) {
                    highlight = true;
                } else {
                    // Check if any of the factors match.
                    if (Utils.hasCommonFactor(factors,
                            childData.getFactors())) {
                        highlight = true;
                    }
                }

                if (highlight) {
                    cellView.highlight(true);
                    mHighlightedCells.add(cellView);
                }
            }
        } else {
            // Disable highlight for all cells.
            for (NumberCellTextView cellTextView : mHighlightedCells) {
                cellTextView.highlight(false);
            }

            mHighlightedCells.clear();
        }
    }

    /*
     * Helper method to synchronize preferences.
     */
    private void syncPreferences() {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);

        int spanCount = preferences.getInt(getString(
                R.string.preference_columns_key), DEFAULT_COLUMN_COUNT);

        int bufferSize = preferences.getInt(getString(
                R.string.preference_buffer_key), DEFAULT_BUFFER_SIZE);

        // Apply data from preference.
        setSpanAndBufferSize(spanCount, bufferSize);
    }

    private void setSpanAndBufferSize(int spanCount, int bufferSize) {
        int currentSpanCount = ((GridLayoutManager) mNumbersGridRecyclerView
                .getLayoutManager()).getSpanCount();

        // Check if any setting has changed.
        boolean isSpanCountChanged = (currentSpanCount != spanCount);
        boolean isBufferSizeChanged = (bufferSize != mBufferSize);

        // Ensure extra cells is updated.
        if (mExtraCells != (spanCount * bufferSize))
            isBufferSizeChanged = true;

        if (isSpanCountChanged || isBufferSizeChanged) {
            /*
             * Apply buffer size and span.
             */
            ((GridLayoutManager)
                    mNumbersGridRecyclerView.getLayoutManager())
                    .setSpanCount(spanCount);

            mBufferSize = bufferSize;

            // Calculate updated extra cells.
            mExtraCells = mBufferSize * spanCount;

            /*
             * Changing either buffer size or span count changes amount of extra
             * cells to load, therefore everything must be reloaded.
             */

            // Reset flags.
            mIsHighlighted = false;
            mIsOverDraft = false;
            mIsLoading = false;

            // Create new adapter.
            mNumbersGridRecyclerView.setAdapter(
                    new NumberCellAdapter(spanCount));
            // Redraw RecyclerView.
            mNumbersGridRecyclerView.invalidate();
            // Start generating first numbers.
            mGenerator.start(0, 300, true);
        }
    }

    /*
     * Helper method for span count updating
     */
    private void setSpanCount(int spanCount) {
        setSpanAndBufferSize(spanCount, mBufferSize);
    }

    private void setBufferSize(int bufferSize) {
        setSpanAndBufferSize(((GridLayoutManager) mNumbersGridRecyclerView
                .getLayoutManager()).getSpanCount(), bufferSize);
    }
}
