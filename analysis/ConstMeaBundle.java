package com.platformda.iv.analysis;

public class ConstMeaBundle extends MeaBundle implements Sweep {

    private double value;

    public ConstMeaBundle(double value) {
        this.value = value;
    }

    void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public MeaBundle reverse() {
        return new ConstMeaBundle(-value);
    }

    @Override
    public MeaBundle[] split(int pointNumber) {
        return new MeaBundle[]{this};
    }

    @Override
    public double get(int index) {
        return value;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void shift(double dist) {
        value += dist;
    }

    @Override
    public double getStart() {
        return value;
    }

    @Override
    public double getStep() {
        return 0;
    }

    @Override
    public double getStop() {
        return value;
    }

    @Override
    public void scale(double factor) {
        value *= factor;
    }
}
