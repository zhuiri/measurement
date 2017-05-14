/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.EntityDevice;
import com.platformda.iv.measure.MeaDevice;

/**
 * device measured/measuring, with lot/wafer/die prop
 *
 * @author Junyi
 */
public class MeaedDevice extends EntityDevice {

    MeaDevice device; // device prototype
    int dieIndex;

    public MeaDevice getDevice() {
        return device;
    }

    public void setDevice(MeaDevice device) {
        this.device = device;
    }

    public int getDieIndex() {
        return dieIndex;
    }

    public void setDieIndex(int dieIndex) {
        this.dieIndex = dieIndex;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MeaedDevice other = (MeaedDevice) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
