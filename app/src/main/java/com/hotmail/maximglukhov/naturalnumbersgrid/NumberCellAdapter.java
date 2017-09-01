package com.hotmail.maximglukhov.naturalnumbersgrid;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Adapter for {@link android.support.v7.widget.RecyclerView} displaying grid of natural numbers.<br>
 * Shows prime numbers with red background.</p>
 */
public class NumberCellAdapter
        extends RecyclerView.Adapter<NumberCellAdapter.ViewHolder> {

    private static final String LOG_TAG = "NumberCellAdapter";

    /*
     * Data structure for cells represented by this adapter.
     */
    private List<NumberCell> mNumberCells;

    /*
     * Defines amount of columns in grid.
     */
    private int mSpanCount;

    private boolean mIsLoading;
    private boolean mIsScrollingDown;

    /**
     * <p>Constructor for this adapter.</p>
     */
    public NumberCellAdapter(int spanCount) {
        mNumberCells = new ArrayList<>();

        mSpanCount = spanCount;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create (default) view from layout.
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.number_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Apply number cell data to number cell text view.
        holder.numberTextView.setCellData(mNumberCells.get(position));
    }

    @Override
    public int getItemCount() {
        return mNumberCells.size();
    }

    /**
     * <p>Inserts cells at given index up to given range. Stops at first null cell in array (if encountered).</p>
     * @param cells cells to insert.
     * @param start index to insert at (inclusive).
     * @param range range (excluding start + range).
     * @return amount of successfully inserted cells.
     */
    public int insertCells(NumberCell[] cells, int start, int range) {
        // Validate input.
        if (cells == null)
            return 0;

        // Add to end of list.
        int cellListSize = mNumberCells.size();
        if (start == cellListSize - 1)
            start = cellListSize;

        int count = 0;
        for (int i = 0; i < range; i++) {
            // Ensure cells amount matches range.
            if (i == cells.length)
                break;

            // Reached cells termination (happens when we scroll to 0).
            if (cells[i] == null)
                break;

            // Insert cell.
            mNumberCells.add(start++, cells[i]);
            count++;
        }

        Log.d(LOG_TAG, "insertCells :: post insertCells cells (" + cellListSize + "):" + mNumberCells);

        return count;
    }

    /**
     * <p>Gets list of {@link NumberCell} represented by this adapter.</p>
     * @return
     */
    public List<NumberCell> getData() {
        return mNumberCells;
    }

    /**
     * <p>Removes given amount of cells from given index.</p>
     * @param startIndex start index to remove elements from.
     * @param amount amount of elements to remove.
     */
    public void removeCells(int startIndex, int amount) {
        int cellsListSize = mNumberCells.size();

        // Validate input.
        if ((startIndex >= cellsListSize) || (amount >= cellsListSize)
                || ((startIndex + amount) >= cellsListSize))
            return;

        Log.d(LOG_TAG, "removeCells :: removing from i=" + startIndex + ", amount=" + amount);
        if (startIndex == 0) {
            mNumberCells = mNumberCells.subList(amount, mNumberCells.size());
        } else {
            List<NumberCell> lowerList = mNumberCells.subList(0, startIndex);
            lowerList.addAll(mNumberCells.subList(startIndex + amount + 1, mNumberCells.size()));
            mNumberCells = lowerList;
        }

        Log.d("NA", "post removal cells(" + mNumberCells.size() + "):" + mNumberCells);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        /*
         * Holder for cell text view.
         */
        private NumberCellTextView numberTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            numberTextView = itemView.findViewById(R.id.numberTextView);
        }
    }
}
