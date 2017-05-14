package com.platformda.iv.analysis;

/**
 * Double Values, or Double List
 * 
 * Known subclass: ArrayDoubleBundle
 *
 */
public interface DoubleBundle {

    public int size();

    public double get(int index);

    public void shift(double shift);
}
