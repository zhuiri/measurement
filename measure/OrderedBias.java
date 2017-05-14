/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.iv.analysis.MeaBias;

/**
 *
 * @author Junyi
 */
public class OrderedBias {

    static final int TYPE_SWEEP = 0;
    static final int TYPE_POINT = 1;
    static final int TYPE_SYNC = 2;
    //
    MeaBias bias;
    int type;

    public OrderedBias(MeaBias bias, int type) {
        this.bias = bias;
        this.type = type;
    }

    public static OrderedBias newSweep(MeaBias bias) {
        return new OrderedBias(bias, TYPE_SWEEP);
    }

    public static OrderedBias newPoint(MeaBias bias) {
        return new OrderedBias(bias, TYPE_POINT);
    }

    public static OrderedBias newSync(MeaBias bias) {
        return new OrderedBias(bias, TYPE_SYNC);
    }
}
