/**
 *
 */
package com.platformda.iv.measure;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * see WaferDieInfo.java
 *
 * Die is nothing more than a list of SubDies
 */
public class MeaDie {

    private List<MeaSubDie> subDies = new ArrayList();

    public MeaDie() {
    }

    public void addSubDie(MeaSubDie subDie) {
        this.subDies.add(subDie);
    }

    public boolean removeSubDie(MeaSubDie subDie) {
        return this.subDies.remove(subDie);
    }

    public List<MeaSubDie> getSubDies() {
        return subDies;
    }

    public MeaSubDie[] getSubDiesInArray() {
        return subDies.toArray(new MeaSubDie[subDies.size()]);
    }

    public int getNextSubDieIndex() {
        int maxIndex = -1;
        for (MeaSubDie subDie : subDies) {
            if (subDie.getSubDieIndex() > maxIndex) {
                maxIndex = subDie.getSubDieIndex();
            }
        }
        if (maxIndex == -1 && !subDies.isEmpty()) {
            // has no valid index
            return -1;
        }
        return maxIndex + 1;
    }

    public int getSubDieNumber() {
        return subDies.size();
    }

    public MeaSubDie getSubDie(int index) {
        if (index < 0 || index >= subDies.size()) {
            return null;
        }
        return (MeaSubDie) subDies.get(index);
    }

    public void clearSubDies() {
        subDies.clear();
    }

    public MeaSubDie getSubDieByName(String name) {
        for (MeaSubDie subDie : subDies) {
            if (subDie.getName().equalsIgnoreCase(name)) {
                return subDie;
            }
        }

        return null;
    }
//    public int getRoutineNumber() {
//        int number = 0;
//        for (MeaSubDie subDie : subDies) {
//            List<MeaDeviceGroup> groups = subDie.getGroups();
//            for (MeaDeviceGroup deviceGroup : groups) {
//                number += deviceGroup.getDeviceNumber() * deviceGroup.getRoutineNumber();
//            }
//        }
//        return number;
//    }
//    public int getDeviceNumber() {
//        int number = 0;
//
//        for (MeaSubDie subDie : subDies) {
//            List<MeaDeviceGroup> groups = subDie.getGroups();
//            for (MeaDeviceGroup deviceGroup : groups) {
//                number += deviceGroup.getDeviceNumber();
//            }
//        }
//
//        return number;
//    }
//
//    public MeaDevice getDevice(int index) {
//        int add = 0;
//        for (MeaSubDie subDie : subDies) {
//            List<MeaDeviceGroup> groups = subDie.getGroups();
//            for (MeaDeviceGroup deviceGroup : groups) {
//                int number = deviceGroup.getDeviceNumber();
//                if (index < add + number) {
//                    return deviceGroup.getDevice(index - add);
//                }
//                add += number;
//            }
//        }
//        return null;
//    }
}
