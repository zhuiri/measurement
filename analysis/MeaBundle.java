package com.platformda.iv.analysis;

/**
 * Measurement DoubleValues
 *
 */
public abstract class MeaBundle implements DoubleBundle, Cloneable {

    abstract public MeaBundle reverse();

    abstract public MeaBundle[] split(int point);

    abstract public void scale(double factor);

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // should never happen
            e.printStackTrace();
        }
        return null;
    }
}
