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
public interface RoutineSelector {

    public boolean isSelected(Routine routine);

    public boolean isSelected(Routine routine, EntityPagePattern pagePattern);

    public boolean isSelected(Routine routine, SpecPattern specPattern);
    
    public boolean isSelected(MeaSubDie subDie);

    public boolean isSelected(MeaDeviceGroup deviceGroup);

    public boolean isSelected(MeaDevice meaDevice);


    public boolean isSelected(MeaDevice meaDevice, Routine routine);


}
