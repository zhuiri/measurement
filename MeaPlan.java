/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.MeaData;
import com.platformda.iv.measure.MeaDevice;
import com.platformda.utility.common.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jdom2.Element;

/**
 * die to test
 *
 * @author Junyi
 */
public class MeaPlan implements StatusProvider {

    String name;
    List<WaferDieInfo> dieInfos = new ArrayList<WaferDieInfo>();
    List<RoutineTuple> routineTuples = new ArrayList<RoutineTuple>();
    int[] statuses;
    int status = WaferDieInfo.STATUS_NOT_MEAED;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WaferDieInfo> getDieInfos() {
        return dieInfos;
    }

    public List<RoutineTuple> getRoutineTuples() {
        return routineTuples;
    }

    public int indexOf(WaferDieInfo dieInfo) {
        return dieInfos.indexOf(dieInfo);
    }

    public void initStatuses() {
        statuses = new int[dieInfos.size()];
        Arrays.fill(statuses, WaferDieInfo.STATUS_NOT_MEAED);
    }

    public int[] getStatuses() {
        return statuses;
    }

    public void setStatuses(int[] statuses) {
        this.statuses = statuses;
    }

    public void setStatus(int index, int status) {
        if (statuses != null && index >= 0 && index < statuses.length) {
            statuses[index] = status;
        }
    }

    @Override
    public int getStatus(WaferDieInfo dieInfo) {
        int index = dieInfos.indexOf(dieInfo);
        if (statuses != null && index >= 0 && index < statuses.length) {
            return statuses[index];
        }
        return dieInfo.getStatus();
    }

    public void updateStatus() {
        boolean hasMea = false;
        boolean hasNotMea = false;
        for (int s : statuses) {
            if (s == WaferDieInfo.STATUS_PART_MEAED) {
                status = s;
                return;
            } else if (s == WaferDieInfo.STATUS_MEAED) {
                hasMea = true;
            } else {
                hasNotMea = true;
            }
            if (hasNotMea && hasMea) {
                status = WaferDieInfo.STATUS_PART_MEAED;
                return;
            }
        }
        if (hasNotMea) {
            if (hasMea) {
                status = WaferDieInfo.STATUS_PART_MEAED;
            } else {
                status = WaferDieInfo.STATUS_NOT_MEAED;
            }
        } else {
            status = WaferDieInfo.STATUS_MEAED;
        }
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return name;
    }

    public Element asElement(String name) {
        Element elem = new Element(name);
        elem.setAttribute("name", this.name);

        StringBuilder builder = new StringBuilder();
        for (WaferDieInfo dieInfo : dieInfos) {
            builder.append(dieInfo.getDieIndex());
            builder.append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        elem.setAttribute("dieindice", builder.toString());

        for (RoutineTuple rt : routineTuples) {
            Element routineElem = new Element("routine");
            elem.addContent(routineElem);

            routineElem.setAttribute("deviceid", String.valueOf(rt.meaDevice.getId()));
            routineElem.setAttribute("routineid", String.valueOf(rt.routine.getId()));
        }

        return elem;
    }

    public void fromElement(MeaData meaData, WaferInfo waferInfo, Element elem) {
        name = elem.getAttributeValue("name");

        String indiceStr = elem.getAttributeValue("dieindice");
        if (!indiceStr.isEmpty()) {
            String[] parts = indiceStr.split(",");
            int[] indice = StringUtil.convertStringArrayToIntArray(parts);
            for (int index : indice) {
                WaferDieInfo dieInfo = waferInfo.getDieInfoByDieIndex(index);
                if (dieInfo != null) {
                    dieInfos.add(dieInfo);
                }
            }
        }
        initStatuses();

        List<Element> rtElems = elem.getChildren("routine");
        for (Element rtElem : rtElems) {
            long deviceId = Long.parseLong(rtElem.getAttributeValue("deviceid"));
            MeaDevice meaDevice = meaData.getMeaDevice(deviceId);

            long routineId = Long.parseLong(rtElem.getAttributeValue("routineid"));

            Routine routine = meaData.getRoutineById(routineId);
            RoutineTuple rt = meaData.getRoutineTuple(meaDevice, routine);

            if (rt != null) {
                routineTuples.add(rt);
            }
        }
    }
}
