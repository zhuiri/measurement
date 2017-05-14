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

/**
 *
 * @author Junyi
 */
public class RoutineSelectorAdapter implements RoutineSelector {

    @Override
    public boolean isSelected(MeaSubDie subDie) {
        return true;
    }

    @Override
    public boolean isSelected(MeaDeviceGroup deviceGroup) {
        return true;
    }

    @Override
    public boolean isSelected(MeaDevice meaDevice) {
        return true;
    }

    @Override
    public boolean isSelected(Routine routine) {
        return true;
    }

    @Override
    public boolean isSelected(MeaDevice meaDevice, Routine routine) {
        return true;
    }

    @Override
    public boolean isSelected(Routine routine, EntityPagePattern pagePattern) {
        return false;
    }

    @Override
    public boolean isSelected(Routine routine, SpecPattern specPattern) {
        return false;
    }
}
