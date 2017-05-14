package com.platformda.iv.analysis;

/**
 * A sweep that is synchronized with another sweep
 */
public interface SyncSweep extends Sweep {

    public Sweep getSweep();

    public double getRatio();

    public double getOffset();
}
