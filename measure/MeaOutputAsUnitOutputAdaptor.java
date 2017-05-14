/**
 *
 */
package com.platformda.iv.measure;

import com.platformda.iv.analysis.MeaOutput;
import com.platformda.iv.api.OutputType;
import com.platformda.iv.api.Unit;
import com.platformda.iv.api.UnitOutput;

/**
 * adapts an MeaOutput to a TermOutput
 *
 */
public class MeaOutputAsUnitOutputAdaptor implements UnitOutput {

    private Unit unit;
    private MeaOutput output;

    public MeaOutputAsUnitOutputAdaptor(Unit unit, MeaOutput output) {
        this.unit = unit;
        this.output = output;
    }

    @Override
    public String getName() {
        return output.getName();
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public OutputType getType() {
        return output.getType();
    }
}
