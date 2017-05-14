/**
 *
 */
package com.platformda.iv.measure;

import com.platformda.datacore.DevicePolarity;
import com.platformda.datacore.DeviceType;
import com.platformda.datacore.EntityDevice;
import com.platformda.utility.common.StringUtil;
import java.util.Arrays;
import java.util.List;

/**
 * EntityDevice with pins
 *
 *
 */
public class MeaDevice implements Cloneable {

    MeaSubDie subDie;
    MeaDeviceGroup deviceGroup;
    //
    public EntityDevice device;
    private int[] pins;

    public MeaDevice(EntityDevice device) {
        this.device = device;
        pins = new int[device.getDeviceType().getNodeNames().length];
        Arrays.fill(pins, -1);
    }

    public long getId() {
        return device.getId();
    }

    public void setId(long id) {
        device.setId(id);
    }

    public DeviceType getDeviceType() {
        return device.getDeviceType();
    }

    public DevicePolarity getDevicePolarity() {
        return device.getDevicePolarity();
    }

    public EntityDevice getDevice() {
        return device;
    }

    public MeaSubDie getSubDie() {
        return subDie;
    }

    public void setSubDie(MeaSubDie subDie) {
        this.subDie = subDie;
    }

    public MeaDeviceGroup getDeviceGroup() {
        return deviceGroup;
    }

    public void setDeviceGroup(MeaDeviceGroup deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    public int getPin(String nodeName) {
        DeviceType type = device.getDeviceType();

        int index = StringUtil.indexOf(nodeName, type.getNodeNames());
        if (index >= 0) {
            return pins[index];
        }
        return -1;
    }

    public void setPin(String nodeName, int pin) {
        DeviceType type = device.getDeviceType();
        int index = StringUtil.indexOf(nodeName, type.getNodeNames());
        if (index >= 0) {
            pins[index] = pin;
        }
    }

    public int[] getPins() {
        return pins;
    }

    public void setPins(int[] pins) {
        this.pins = pins;
    }

    @Override
    public String toString() {
        return device.getPrettyString();
    }

    public String getPrettyString(List<String> excludes) {
        return device.getPrettyString(" ", excludes);
    }

    public void copyValueOf(MeaDevice other) {
        device.copyValueOf(other.device);
        pins = Arrays.copyOf(other.pins, other.pins.length);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        MeaDevice clone = (MeaDevice) super.clone();
        clone.device = (EntityDevice) this.device.clone();
        clone.pins = this.pins.clone();
        return clone;
    }
}
