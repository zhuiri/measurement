/**
 *
 */
package com.platformda.iv.analysis;

/**
 */
public class ArrayMeaBundle extends MeaBundle {

    private double[] values = new double[0];

    public ArrayMeaBundle() {
    }

    public ArrayMeaBundle(double[] values) {
        this.values = values;
    }

    public void setValues(double[] values) {
        if (values == null) {
            this.values = new double[0];
        } else {
            this.values = values;
        }
    }

    @Override
    public MeaBundle reverse() {
        double[] v = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            v[i] = -values[i];
        }
        ArrayMeaBundle sweep = new ArrayMeaBundle();
        sweep.setValues(v);
        return sweep;
    }

    @Override
    public MeaBundle[] split(int pointNumber) {
        MeaBundle[] splits = new MeaBundle[values.length];
        for (int i = 0; i < splits.length; i++) {
            splits[i] = new ConstMeaBundle(values[i]);
        }
        return splits;
    }

    @Override
    public double get(int index) {
        return values[index];
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Object clone() {
        ArrayMeaBundle sweep = (ArrayMeaBundle) super.clone();
        sweep.values = (double[]) values.clone();
        return sweep;
    }

    @Override
    public void shift(double dist) {
        for (int i = 0; i < values.length; i++) {
            values[i] += dist;
        }
    }

    @Override
    public void scale(double factor) {
        for (int i = 0; i < values.length; i++) {
            values[i] *= factor;
        }
    }
}
