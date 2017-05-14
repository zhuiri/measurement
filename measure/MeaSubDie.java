/**
 *
 */
package com.platformda.iv.measure;

import com.platformda.datacore.DeviceType;
import com.platformda.iv.admin.Routine;
import com.platformda.utility.Iconable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 * A sub-die of devices
 */
public class MeaSubDie implements Iconable {

    String name;
    int subDieIndex = -1; // to avoid confusing with normal index of list
    String x; // x offset
    String y; // y offset
    private List<MeaDeviceGroup> groups = new ArrayList();

    public MeaSubDie(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getSubDieIndex() {
        return subDieIndex;
    }

    public void setSubDieIndex(int subDieIndex) {
        this.subDieIndex = subDieIndex;
    }

    public boolean hasRoutineTuple() {
        for (MeaDeviceGroup group : groups) {
            if (group.hasRoutineTuple()) {
                return true;
            }
        }
        return false;
    }

    public List<MeaDeviceGroup> getGroups() {
        return groups;
    }

    public void addGroup(MeaDeviceGroup group) {
        groups.add(group);
    }

    public boolean removeGroup(MeaDeviceGroup group) {
        return groups.remove(group);
    }

    public int getGroupNumber() {
        return groups.size();
    }

    public MeaDeviceGroup getGroup(int index) {
        return (MeaDeviceGroup) groups.get(index);
    }

    public MeaDeviceGroup getGroup(String name) {
        for (MeaDeviceGroup group : groups) {
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    public void fetchGroups(DeviceType deviceType, List<MeaDeviceGroup> results) {
        for (MeaDeviceGroup group : groups) {
            if (group.getDeviceType().equals(deviceType)) {
                results.add(group);
            }
        }
    }

    @Override
    public String toString() {
        if (subDieIndex >= 0) {
            return String.format("%s [%d]", name, subDieIndex);
        }
        return name;
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/subdie.png"));
    }

    public void fetchDevicesAndRoutines(List<MeaDevice> meaDevices, List<Routine> routines) {
//        List<MeaDevice> meaDevices = new ArrayList<MeaDevice>();
//        List<Routine> routines = new ArrayList<Routine>();
        for (MeaDeviceGroup deviceGroup : groups) {
            List<MeaDevice> devicesInDG = deviceGroup.getDevices();
            List<Routine> routinesInDG = deviceGroup.getRoutines();
            for (MeaDevice meaDevice : devicesInDG) {
                meaDevices.add(meaDevice);
            }
            for (Routine routine : routinesInDG) {
                routines.add(routine);
            }
        }
    }
}
