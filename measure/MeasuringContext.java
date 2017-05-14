/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.datacore.DevicePolarity;
import com.platformda.datacore.DeviceType;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.api.MeaContext;
import com.platformda.iv.api.MeaType;
import com.platformda.iv.api.NodeBond;
import com.platformda.utility.common.Console;

/**
 *
 * @author Junyi
 */
public class MeasuringContext implements MeaContext {

    DeviceType deviceType;
    DevicePolarity devicePolarity;
    NodeBond[] bonds;
    MeaType meaType;
    int bundleType = MeaContext.TYPE_SWEEP;
    Console console;
    EntityPagePattern pagePattern;

    @Override
    public EntityPagePattern getPagePattern(){
        return pagePattern;
    }
    
    @Override
    public DeviceType getDeviceType() {
        return deviceType;
    }

    @Override
    public DevicePolarity getDevicePolarity() {
        return devicePolarity;
    }

    @Override
    public NodeBond[] getBonds() {
        return bonds;
    }

    @Override
    public MeaType getMeaType() {
        return meaType;
    }

    @Override
    public int getBundleType() {
        return bundleType;
    }

    @Override
    public Console getConsole() {
        return console;
    }
}