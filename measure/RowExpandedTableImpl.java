package com.platformda.iv.measure;

import java.util.Arrays;

public class RowExpandedTableImpl implements RowExpandedTable {

    private double[][] values = null;

    public RowExpandedTableImpl(int row, int column) {
        values = new double[row][column];
        for (int i = 0; i < row; i++) {
            Arrays.fill(values[i], Double.NaN);
        }
    }

    @Override
    public double getValue(int row, int column) {
        try {
            return values[row][column];
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    @Override
    public boolean setValue(int row, int column, double value) {
        if (row < 0 || row >= values.length) {
            return false;
        }
        if (column < 0 || column >= values[row].length) {
            return false;
        }
        values[row][column] = value;
        return true;
    }

    @Override
    public int getColumnNumber() {
        return values.length > 0 ? values[0].length : 0;
    }

    @Override
    public int getRowNumber() {
        return values.length;
    }
}
