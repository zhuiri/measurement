/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.spec.SpecPattern;

/**
 *
 * @author Junyi
 */
public interface DeviceBondProvider {

    public boolean hasDeviceBond(EntityPagePattern pagePattern);

    public DeviceBond getDeviceBond(EntityPagePattern pagePattern);

    public void setDeviceBond(EntityPagePattern pagePattern, DeviceBond deviceBond);

    public boolean hasDeviceBond(SpecPattern specPattern);

    public DeviceBond getDeviceBond(SpecPattern specPattern);

    public void setDeviceBond(SpecPattern specPattern, DeviceBond deviceBond);
}
