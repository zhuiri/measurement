package com.platformda.iv.analysis;

public class SyncMeaBundle extends MeaBundle implements SyncSweep {

    private SweepMeaBundle sweep;
    private double ratio = 1;
    private double offset = 0;

    public SyncMeaBundle(SweepMeaBundle sweep) {
        this.sweep = sweep;
    }

    @Override
    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    @Override
    public MeaBundle reverse() {
        //TODO explain
        return this;
    }

    @Override
    public MeaBundle[] split(int pointNumber) {
        return new MeaBundle[]{this};
    }

    @Override
    public double get(int index) {
        return sweep.get(index) * ratio + offset;
    }
    
    public SweepMeaBundle getSweepBundle(){
        return this.sweep;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public Object clone() {
        SyncMeaBundle sweep = (SyncMeaBundle) super.clone();
        sweep.sweep = (SweepMeaBundle) this.sweep.clone();
        return sweep;
    }

    @Override
    public Sweep getSweep() {
        return sweep;
    }

    @Override
    public double getStart() {
        return sweep.getStart() * ratio + offset;
    }

    @Override
    public double getStep() {
        return sweep.getStep();
    }

    @Override
    public double getStop() {
        return sweep.getStop() * ratio + offset;
    }

    @Override
    public void shift(double shift) {
        offset += shift;
    }

    @Override
    public void scale(double factor) {
        sweep.scale(factor);
    }
}
