/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.measure.MeaDevice;
import com.platformda.iv.measure.MeaDeviceGroup;
import com.platformda.iv.measure.MeaSubDie;
import com.platformda.spec.SpecPattern;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Junyi
 */
public class DUTPacks implements RoutineSelector {

    List<DUTPack> packs;// = new ArrayList<DUTPack>();

    public DUTPacks(List<DUTPack> packs) {
        this.packs = packs;
    }

    @Override
    public boolean isSelected(Routine routine) {
        for (DUTPack pack : packs) {
            if (pack.isSelected(routine)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSelected(Routine routine, EntityPagePattern pagePattern) {
        for (DUTPack pack : packs) {
            if (pack.isSelected(routine, pagePattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSelected(Routine routine, SpecPattern specPattern) {
        for (DUTPack pack : packs) {
            if (pack.isSelected(routine, specPattern)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSelected(MeaSubDie subDie) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSelected(MeaDeviceGroup deviceGroup) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSelected(MeaDevice meaDevice) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSelected(MeaDevice meaDevice, Routine routine) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
