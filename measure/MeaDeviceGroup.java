/**
 *
 */
package com.platformda.iv.measure;

import com.platformda.datacore.DevicePolarity;
import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceType;
import com.platformda.datacore.DeviceTypeManager;
import com.platformda.datacore.EntityDevice;
import com.platformda.iv.MeaData;
import com.platformda.iv.admin.EntityInstrument;
import com.platformda.iv.admin.Routine;
import com.platformda.iv.admin.RoutineSelector;
//import com.platformda.iv.admin.RoutineTupleFilter;
import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.spec.SpecRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Devices in a group share one same DUT and pattern in a measurement
 * DeviceGroup is identified by name
 *
 */
public class MeaDeviceGroup implements DeviceTag {

    private String name;
    private DeviceType deviceType;
    private DevicePolarity devicePolarity;
    private List<MeaDevice> devices = new ArrayList();
    private List<Routine> routines = new ArrayList();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNameByDeviceType() {
        StringBuilder builder = new StringBuilder();

        builder.append(name);
        builder.append(" [");
        builder.append(DeviceTypeManager.toString(this));
        builder.append("]");
        return builder.toString();
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public DevicePolarity getDevicePolarity() {
        return devicePolarity;
    }

    public void setDevicePolarity(DevicePolarity devicePolarity) {
        this.devicePolarity = devicePolarity;
    }

    public boolean hasRoutineTuple() {
        if (devices.isEmpty() || routines.isEmpty()) {
            return false;
        }
        return true;
    }

    public List<MeaDevice> getDevices() {
        return devices;
    }

    public void addDevice(MeaDevice device) {
        devices.add(device);
    }

    public MeaDevice removeDevice(int index) {
        return devices.remove(index);
    }

    public int getDeviceNumber() {
        return devices.size();
    }

    public MeaDevice getDevice(int index) {
        return devices.get(index);
    }

    public MeaDevice getMeaDeviceByDevice(EntityDevice device) {
        for (MeaDevice meaDevice : devices) {
            if (meaDevice.getDevice() == device) {
                return meaDevice;
            }
        }
        return null;
    }

    public List<Routine> getRoutines() {
        return routines;
    }

    public int getRoutineNumber() {
        return routines.size();
    }

    public Routine getRoutine(int index) {
        return routines.get(index);
    }

    public boolean removeRoutine(Routine routine) {
        return routines.remove(routine);
    }

    public void addRoutine(Routine routine) {
        this.routines.add(routine);
    }

    // TODO: remove
    public void setRoutines(Routine[] routines) {
        this.routines.clear();
        this.routines.addAll(Arrays.asList(routines));
    }

    public boolean contains(Routine routine) {
        return routines.contains(routine);
    }

    @Override
    public String toString() {
        return name;
    }

//    public void fetchMatrices(Set<EntityInstrument> matrices, RoutineTupleFilter routineTupleFilter) {
//        for (Routine routine : routines) {
//            if (routineTupleFilter.isSelected(routine)) {
//                routine.fetchMatrices(matrices);
//            }
//        }
//    }

    public void fetchInstruments(Set<EntityInstrument> instruments, RoutineSelector routineTupleFilter) {
        for (Routine routine : routines) {
            if (routineTupleFilter.isSelected(routine)) {
                routine.fetchInstruments(instruments, routineTupleFilter);
            }
        }
    }

    // NOTE: IV vs CV, and CV vs CV, never share same DeviceBond
    public void groupSpecByBond(Map<DeviceBond, List<SpecRequest>> specMap, MeaData meaData, RoutineSelector routineTupleFilter) {
        for (Routine routine : routines) {
            if (routineTupleFilter.isSelected(routine)) {
                routine.groupSpecByBond(specMap, meaData, routineTupleFilter);
            }
        }
    }

    public void groupPageByBond(Map<DeviceBond, List<MeaAnalysis>> analysisMap, DevicePolarity devicePolarity, RoutineSelector routineTupleFilter) {
        for (Routine routine : routines) {
            if (routineTupleFilter.isSelected(routine)) {
                routine.groupPageByBond(analysisMap, devicePolarity, routineTupleFilter);
            }
        }
    }

    public void groupNonStressPageByBond(Map<DeviceBond, List<MeaAnalysis>> analysisMap, DevicePolarity devicePolarity, RoutineSelector routineTupleFilter) {
        for (Routine routine : routines) {
            if (routineTupleFilter.isSelected(routine) ) {//&& !routine.hasStress()
                routine.groupPageByBond(analysisMap, devicePolarity, routineTupleFilter);
            }
        }
    }
}
