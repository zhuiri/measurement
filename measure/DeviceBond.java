package com.platformda.iv.measure;

import com.platformda.datacore.DeviceType;
import com.platformda.iv.admin.EntityInstrument;
import com.platformda.iv.admin.InstrumentManager;
import com.platformda.iv.api.NodeBond;
import com.platformda.utility.common.StringUtil;
import java.util.Arrays;
import java.util.Set;

/**
 */
public class DeviceBond {

    public String name;
    DeviceType deviceType;
//    String[] matches = new String[0];
    private NodeBond[] nodeBonds = new NodeBond[0];
    boolean valid = true;

    public DeviceBond(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public void addBond(NodeBond bond) {
        NodeBond[] added = Arrays.copyOf(nodeBonds, nodeBonds.length + 1);
        added[nodeBonds.length] = bond;
        nodeBonds = added;
    }

    public void removeBond(int index) {
        NodeBond[] copy = Arrays.copyOf(nodeBonds, nodeBonds.length - 1);
        for (int i = index; i < copy.length; i++) {
            copy[i] = nodeBonds[i + 1];
        }
        nodeBonds = copy;
    }

    public NodeBond[] getBonds() {
        return nodeBonds;
    }

    public void setBonds(NodeBond[] bonds) {
        this.nodeBonds = bonds;
    }

    @Override
    public String toString() {
        return name;
    }

    public void fetchInstruments(Set<EntityInstrument> instruments) {
        InstrumentManager instrumentManager = InstrumentManager.getInstance();
        for (NodeBond nodeBond : nodeBonds) {
            String instName = nodeBond.getInstName();
            if (StringUtil.isValid(instName)) {
                EntityInstrument ei = instrumentManager.getInstrument(instName);
                if (ei != null) {
                    instruments.add(ei);
                }
            }
        }
    }
}
