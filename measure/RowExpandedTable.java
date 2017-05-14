package com.platformda.iv.measure;

/**
 * A table store the measured data for a AppAnalysis Structure of this table:
 * let biasIndices[i] be the bias index for the i-th sweep let base[i] be:
 * base[0] = 1; base[i] = sweep-size[i - 1] * base[ i - 1 ] The data for the
 * biasIndices is stored at row = SUM( biasIndices[i] * base[i] ) if the output
 * is a double, the i-th output is stored in the i-th column. if the output is
 * several doubles(e.g. complex number), each component of a output is saved
 * next to each other. and each output is saved Sequentially
 */
public interface RowExpandedTable {

    public double getValue(int row, int column);

    public boolean setValue(int row, int column, double value);

    // bias[0] x bias[1] x bias[2] ...
    public int getRowNumber();

    // output number
    public int getColumnNumber();
}
