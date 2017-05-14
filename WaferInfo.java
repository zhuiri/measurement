package com.platformda.iv.admin;

import com.platformda.iv.api.Probe;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * holds the information of a wafer mapping file, such as the file path loaded
 * in Nucleus, (subsite names,) etc.
 *
 * see MeaDie for die structure or subdies
 *
 */
public class WaferInfo {

    String path = "";
    //	int dieNumber = 0;
    //	int subsiteNumber = 0;
    List<WaferDieInfo> dieInfos = new ArrayList();
//    String[] subsiteNames = new String[0];
    int order = Probe.ORDER_X_ASEND_Y_ASEND;
    //
//    double xStreet = 0.005;
//    double yStreet = 0.005;
//    double xDieSize = 0.03; // TODO: number
//    double yDieSize = 0.02; // TODO: number
//    double xGridShift = 0;
//    double yGridShift = -0.004;
//    double qualitySize = 0.19; // TODO: number
//    double diameter = 0.2;
    int refX = 4, refY = 6;

    public WaferInfo() {
    }

    public List<WaferDieInfo> getDieInfos() {
        return dieInfos;
    }

    public int getDieNumber() {
        return dieInfos.size();
    }

//    public String[] getSubsiteNames() {
//        return subsiteNames;
//    }
//
//    public void setSubsiteNames(String[] subsiteNames) {
//        this.subsiteNames = subsiteNames;
//    }
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public WaferDieInfo getDieInfo(int index) {
        return dieInfos.get(index);
    }

    public WaferDieInfo getDieInfoByDieIndex(int dieIndex) {
        // return dieInfos.get(dieIndex-1);
        for (WaferDieInfo dieInfo : dieInfos) {
            if (dieInfo.getDieIndex() == dieIndex) {
                return dieInfo;
            }
        }
        return null;
    }

    public WaferDieInfo getDieInfo(int x, int y) {
        for (WaferDieInfo dieInfo : dieInfos) {
            if (dieInfo.getX() == x && dieInfo.getY() == y) {
                return dieInfo;
            }
        }
//        for (Iterator<WaferDieInfo> itr = dieInfos.iterator(); itr.hasNext();) {
//            WaferDieInfo info = itr.next();
//            if (info.getX() == x && info.getY() == y) {
//                return info;
//            }
//        }
        return null;
    }

    public boolean contains(WaferDieInfo info) {
        return this.dieInfos.contains(info);
    }

    public void clearDieInfos() {
        dieInfos.clear();
    }

    public void addDieInfo(WaferDieInfo info) {
        this.dieInfos.add(info);
    }

    public void removeDieInfo(WaferDieInfo info) {
        this.dieInfos.remove(info);
    }

//    public double getDiameter() {
//        return diameter;
//    }
//
//    public double getQualitySize() {
//        return qualitySize;
//    }
    public int getRefX() {
        return refX;
    }

    public int getRefY() {
        return refY;
    }

//    public double getXDieSize() {
//        return xDieSize;
//    }
//
//    public double getXGridShift() {
//        return xGridShift;
//    }
//
//    public double getXStreet() {
//        return xStreet;
//    }
//
//    public double getYDieSize() {
//        return yDieSize;
//    }
//
//    public double getYGridShift() {
//        return yGridShift;
//    }
//
//    public double getYStreet() {
//        return yStreet;
//    }
//
//    public void setXStreet(double street) {
//        xStreet = street;
//    }
//
//    public void setYStreet(double street) {
//        yStreet = street;
//    }
//
//    public void setXDieSize(double dieSize) {
//        xDieSize = dieSize;
//    }
//
//    public void setYDieSize(double dieSize) {
//        yDieSize = dieSize;
//    }
//
//    public void setXGridShift(double gridShift) {
//        xGridShift = gridShift;
//    }
//
//    public void setYGridShift(double gridShift) {
//        yGridShift = gridShift;
//    }
//
//    public void setQualitySize(double qualitySize) {
//        this.qualitySize = qualitySize;
//    }
//
//    public void setDiameter(double diameter) {
//        this.diameter = diameter;
//    }
    public void setRefX(int refX) {
        this.refX = refX;
    }

    public void setRefY(int refY) {
        this.refY = refY;
    }

    public boolean hasDie(WaferDieInfo dieInfo) {
        for (Iterator itr = dieInfos.iterator(); itr.hasNext();) {
            WaferDieInfo info = (WaferDieInfo) itr.next();
            if (info.getX() == dieInfo.getX() && info.getY() == dieInfo.getY()
                    && info.getDieIndex() == dieInfo.getDieIndex()) {
                return true;
            }
        }

        return false;
    }
}
