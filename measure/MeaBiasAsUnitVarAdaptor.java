/**
 *
 */
package com.platformda.iv.measure;

import com.platformda.iv.analysis.DoubleBundle;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.api.InputType;
import com.platformda.iv.api.Unit;
import com.platformda.iv.api.UnitVar;

/**
 * adapts an MeaBias to a UnitVar
 *
 */
public class MeaBiasAsUnitVarAdaptor implements UnitVar {

    private MeaBias bias;
    private Unit unit;
    private double comp;

    public MeaBiasAsUnitVarAdaptor(Unit unit, MeaBias bias, double compliance) {
        this.unit = unit;
        this.bias = bias;
        this.comp = compliance;
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public InputType getType() {
        return bias.getType();
    }

    @Override
    public double getCompliance() {
        return comp;
    }

    @Override
    public String getName() {
        return bias.getName();
    }

    @Override
    public DoubleBundle getBundle() {
        return bias.getBundle(0);
    }
}
