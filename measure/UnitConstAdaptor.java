/**
 *
 */
package com.platformda.iv.measure;

import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.api.InputType;
import com.platformda.iv.api.Unit;
import com.platformda.iv.api.UnitConst;

/**
 *
 */
public class UnitConstAdaptor implements UnitConst {

    private Unit unit;
    private MeaBias bias;
    private double compliance;

    public UnitConstAdaptor(Unit terminal, MeaBias bias, double comp) {
        this.unit = terminal;
        this.bias = bias;
        this.compliance = comp;
    }

    @Override
    public double getCompliance() {
        return compliance;
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
    public double getValue() {
        return bias.get(0);
    }
}
