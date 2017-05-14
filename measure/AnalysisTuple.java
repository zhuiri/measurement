/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.analysis.MeaOutput;
import com.platformda.iv.api.Capacity;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.Meter;
import com.platformda.iv.api.NodeBond;

/**
 *
 * @author Junyi
 */
public class AnalysisTuple {

    Meter mainMeter;
    Meter[] extraMeters;
    RowExpandedTable table;
    //
    MeaAnalysis analysis;
    OrderedBias[] orderedBiases;
    MeaOutput[] outputs;
    NodeBond[] bonds;
    Capacity capacity;
    //
    int[] bases;
    int[] startIndice;
    int outputStartIndex;

    public void initMeters(Meter mainMeter, Instrument[] meters) {
        this.mainMeter = mainMeter;
        extraMeters = new Meter[meters.length - 1];
        int index = 0;
        for (int i = 0; i < meters.length; i++) {
            if (meters[i].equals(mainMeter)) {
                continue;
            }
            extraMeters[index++] = (Meter) meters[i];
        }
    }

    public int[] buildBases() {
        int[] bases = new int[orderedBiases.length];
        bases[0] = 1;
        for (int i = 1; i < orderedBiases.length; i++) {
            bases[i] = bases[i - 1] * orderedBiases[i - 1].bias.size();
        }
        return bases;
    }

    public int size() {
        int size = 1;
        for (OrderedBias orderedBias : orderedBiases) {
            size *= orderedBias.bias.size();
        }
        return size;
    }

    public MeaBias[] getMeaBiases() {
        MeaBias[] biases = new MeaBias[orderedBiases.length];
        for (int i = 0; i < orderedBiases.length; i++) {
            biases[i] = orderedBiases[i].bias;
        }
        return biases;
    }
}
