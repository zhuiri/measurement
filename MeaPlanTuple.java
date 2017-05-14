/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.mea.measure.Measurer;
import com.platformda.mea.measure.MeasurerContext;

/**
 *
 * @author Junyi
 */
public class MeaPlanTuple {

    String name;
    Measurer measurer;
    MeasurerContext measurerContext;

    public MeaPlanTuple(String name, Measurer measurer, MeasurerContext measurerContext) {
        this.name = name;
        this.measurer = measurer;
        this.measurerContext = measurerContext;
    }
}
