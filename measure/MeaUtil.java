/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.analysis.MeaBundle;
import com.platformda.iv.analysis.SyncMeaBundle;
import com.platformda.iv.api.CMU;
import com.platformda.iv.api.Capacity;
import com.platformda.iv.api.Compliance;
import com.platformda.iv.api.InputType;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.MeaContext;
import com.platformda.iv.api.Meter;
import com.platformda.iv.api.MeterProfile;
import com.platformda.iv.api.NodeBond;
import com.platformda.iv.api.Unit;
import com.platformda.utility.common.StringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Junyi
 */
public class MeaUtil {

    public static boolean isCVNotReferenceOnLow(MeaAnalysis analysis, NodeBond[] bonds) {
        NodeBond lpBond = MeaUtil.getLowPot(bonds);
        if (lpBond != null && !analysis.getRefNode().equalsIgnoreCase(lpBond.getNodeName())) {
            return true;
        }
        return false;
    }

    public static void shiftOnCVLowPot(MeaBias[] biases, double[] shifts, NodeBond[] bonds) {
        // for cv measurements, the low pot should be connected to ground
        // adjust for this:
        // find the low pot joint
        NodeBond lpBond = MeaUtil.getLowPot(bonds);
        if (lpBond != null) {// has a lp bond
            String lpNode = lpBond.getNodeName();
            // find the voltage value which is intended to be set to lpNode
            double lpValue = 0;
            for (int i = 0; i < biases.length; i++) {
                if (biases[i].getFrom().equals(lpNode) && InputType.VOLTAGE.equals(biases[i].getType())) {
                    lpValue = biases[i].get(0);
                    break;
                }
            }
            if (Double.compare(lpValue, 0) != 0) {
                // the intended value is not zero, but lp pot should be zero
                // so shift all voltages to satisfy this requirement
                // since all voltages now is from node to GND, so we should only shift all voltages a lpValue
                for (int i = 0; i < biases.length; i++) {
                    if (InputType.VOLTAGE.equals(biases[0].getType())) {
                        biases[i].getBundle(0).shift(-lpValue);
                        shifts[i] -= lpValue;
                    }
                }
            }
        }
    }

    public static OrderedBias[] getOrderedBiases(MeaBias[] meaBiases, Capacity capacity, MeasuringContext measuringContext, String refNode, boolean isCV, boolean splitToPoint) {
        //in case it would cause problem for casual change, we need to work with the cloned copy
        MeaBias[] clonedMeaBiases = new MeaBias[meaBiases.length];
        for (int i = 0; i < clonedMeaBiases.length; i++) {
            clonedMeaBiases[i] = (MeaBias) meaBiases[i].clone();
        }
        MeaBias[] biases = MeaUtil.reverseByReference(clonedMeaBiases, MeaAnalysis.GND);
        // mark the biases with labels: sweep, point or sync
        OrderedBias[] orderedBiases = new OrderedBias[biases.length];
        int maxSweep = capacity.getMaxSweep();
        if (splitToPoint) {
            for (int i = 0; i < biases.length; i++) {
                orderedBiases[i] = OrderedBias.newPoint(biases[i]);
            }
        } else {
            boolean isSyncAlready = false;
            if (biases.length > 1) {
                MeaBias meaBias1 = biases[1];
                MeaBundle meaBundle = meaBias1.getBundle(0);
                if (meaBundle instanceof SyncMeaBundle) {
                    isSyncAlready = true;
                }
            }

            if (isSyncAlready) {
                if (maxSweep > 0) {
//                    orderedBiases[0] = new OrderedBias(biases[0], OrderedBias.TYPE_SWEEP);
                    orderedBiases[0] = OrderedBias.newSweep(biases[0]);
                } else {
                    orderedBiases[0] = OrderedBias.newPoint(biases[0]);
                }
                orderedBiases[1] = OrderedBias.newSync(biases[1]);
                for (int i = 2; i < biases.length; i++) {
                    orderedBiases[i] = OrderedBias.newPoint(biases[i]);
                }
            } else {
                //count the sweep that is relative to a non-reference and non-gnd node
                int nonrefNumber = 0;
//            String refNode = analysis.getRefNode();
                for (MeaBias bias : biases) {
                    String to = bias.getTo();
                    if (!to.equalsIgnoreCase(refNode) && !to.equalsIgnoreCase(MeaAnalysis.GND)) {
                        nonrefNumber++;
                    }
                }
                // mark Bias with TYPE_POINT,TYPE_SWEEP,TYPE_SYNC
                if (nonrefNumber > 1 || (nonrefNumber == 1 && isCV)) {
                    //1. if the relative count is greater than 1, smash all to points
                    //2. CV measurement doesn't support relative sweep, smash all to points
                    //TODO can make this more efficient if no bias is relative to the first bias
                    measuringContext.bundleType = MeaContext.TYPE_LIST;
                    for (int i = 0; i < orderedBiases.length; i++) {
                        orderedBiases[i] = OrderedBias.newPoint(biases[i]);
                    }
                } else if (nonrefNumber == 1 && biases.length > 1 && !biases[1].getTo().equalsIgnoreCase(biases[0].getFrom())) {
                    //if the relative count 1 and the bias[1] is not relative to bias[0],
                    //smash biases from 2nd to points, the first bias is dependent or meter sweep limitation
                    if (maxSweep > 0) {
                        orderedBiases[0] = OrderedBias.newSweep(biases[0]);
                    } else {
                        orderedBiases[0] = OrderedBias.newPoint(biases[0]);
                    }
                    for (int i = 1; i < orderedBiases.length; i++) {
                        orderedBiases[i] = OrderedBias.newPoint(biases[i]);
                    }
                } else {
                    Map<MeaBias, MeaBias> dependMap = MeaUtil.buildDependMap(biases, MeaAnalysis.GND);
                    for (int i = maxSweep; i < orderedBiases.length; i++) {
                        orderedBiases[i] = OrderedBias.newPoint(biases[i]);
                    }
                    if (biases.length > 1 && biases[0].equals(dependMap.get(biases[1]))) {
                        // sweep 1 is synchronized with sweep 0 (is relative to sweep 0)
                        orderedBiases[0] = OrderedBias.newSweep(biases[0]);
                        orderedBiases[1] = OrderedBias.newSync(biases[1]);
                        for (int i = 2; i < biases.length; i++) {
                            orderedBiases[i] = OrderedBias.newPoint(biases[i]);
                        }
                    } else {
                        Collection<MeaBias> relatedBiases = dependMap.values();
                        List<MeaBias> biasList = Arrays.asList(biases);
                        int minPos = biasList.size();
                        for (MeaBias meaBias : relatedBiases) {
                            int pos = biasList.indexOf(meaBias);
                            if (pos < minPos) {
                                minPos = pos;
                            }
                        }
                        int len = Math.min(minPos, capacity.getMaxSweep());
                        for (int i = 0; i < len; i++) {
                            orderedBiases[i] = OrderedBias.newSweep(biases[i]);
                        }
                        for (int i = len; i < orderedBiases.length; i++) {
                            orderedBiases[i] = OrderedBias.newPoint(biases[i]);
                        }
                    }
                }
            }
        }

        // split huge bias to smaller ones according to:
        // 1. sweep limitation per level
        // 2. all following biases will be split to point if previous one was split
        int point = -1;
        for (int i = 0; i < orderedBiases.length; i++) {
            if (point != 1) {
                //don't change point number if previous one is set to 1
                point = capacity.getMaxPointBySweep(i);
            }
            OrderedBias orderedBias = orderedBiases[i];
            if (orderedBias.type == OrderedBias.TYPE_POINT || orderedBias.type == OrderedBias.TYPE_SYNC || splitToPoint) {
                point = 1;
            }

            MeaBias oldBias = orderedBias.bias;
            MeaBias newBias = new MeaBias(oldBias);
            newBias.clearBundles();
            List<MeaBundle> meaBundles = oldBias.getMeaBundles();
            for (MeaBundle bundle : meaBundles) {
                MeaBundle[] subs = bundle.split(point);
                for (MeaBundle sub : subs) {
                    newBias.addBundle(sub);
                }
            }
            orderedBias.bias = newBias;
        }

        return orderedBiases;
    }

    public static MeaBias[] reverseByReference(MeaBias[] biases, String refNode) {
        MeaBias[] results = Arrays.copyOf(biases, biases.length);
        List<String> nodes = new ArrayList<String>();
        nodes.add(refNode);
        boolean hasBias = false;
        boolean progress = false;
        do {
            hasBias = false;
            progress = false;
            for (int i = 0; i < biases.length; i++) {
                MeaBias meaBias = biases[i];
                if (meaBias == null) {
                    continue;
                }
                if (!meaBias.getType().isRelative()) {
                    biases[i] = null;
                    continue;
                }
                String to = meaBias.getTo();
                String from = meaBias.getFrom();
                if (StringUtil.contains(nodes, to)) {
                    biases[i] = null;
                    StringUtil.addDistinct(nodes, from);
                    progress = true;
                } else if (StringUtil.contains(nodes, from)) {
                    // reverse
                    results[i] = results[i].createReverse();
                    biases[i] = null;
                    StringUtil.addDistinct(nodes, to);
                    progress = true;
                } else {
                    hasBias = true;
                }
            }
        } while (hasBias && progress);

        return results;
    }

    /**
     * build a map to hold the relationships of the biases, the KEY is the bias
     * whose actual value is relative to another bias, the VALUE is the bias
     * that the KEY's value is relative to
     *
     * @param biases The biases to build the relationship
     * @param refNode
     * @return
     */
    public static Map<MeaBias, MeaBias> buildDependMap(MeaBias[] biases, String refNode) {
        HashMap<MeaBias, MeaBias> dependMap = new HashMap();
        for (MeaBias key : biases) {
            if (refNode.equals(key.getTo()) || !key.getType().isRelative()) {
                continue;
            }
            for (MeaBias value : biases) {
                if (!value.getType().isRelative()) {
                    continue;
                }
                if (value.getFrom().equals(key.getTo())) {
                    dependMap.put(key, value);
                    break;
                }
            }
        }
        return dependMap;
    }

    /**
     * return the meter which has the term
     *
     * @param meters
     * @param unit
     * @return
     */
    public static Meter getMeter(Meter[] meters, Unit unit) {
        for (int i = 0; i < meters.length; i++) {
            MeterProfile profile = (MeterProfile) meters[i].getProfile();
            for (int j = 0; j < profile.getUnitNumber(); j++) {
                Unit t = profile.getUnit(j);
                if (t.equals(unit)) {
                    return meters[i];
                }
            }
        }
        return null;
    }

    public static Instrument getInstrument(Instrument[] meters, Unit unit) {
        for (Instrument inst : meters) {
            MeterProfile profile = (MeterProfile) inst.getProfile();
            int unitNumber = profile.getUnitNumber();
            for (int j = 0; j < unitNumber; j++) {
                Unit t = profile.getUnit(j);
                if (t.equals(unit)) {
                    return inst;
                }
            }
        }
        return null;
    }

    public static double getCompliance(String node, InputType type, Compliance[] comps) {
        for (Compliance comp : comps) {
            if (comp.getNode().equals(node)) {
                if (type.isVoltage()) {
                    return comp.getIcompliance();
                } else if (type.isCurrent()) {
                    return comp.getVcompliance();
                }
            }
        }
        return 0;
    }

    /**
     * does the meter contain the term?
     *
     * @param meter
     * @param unit
     * @return
     */
    public static boolean hasUnit(Meter meter, Unit unit) {
        MeterProfile profile = (MeterProfile) meter.getProfile();
        int unitNumber = profile.getUnitNumber();
        for (int i = 0; i < unitNumber; i++) {
            if (profile.getUnit(i).equals(unit)) {
                return true;
            }
        }
        return false;
    }

    public static NodeBond getLowPot(NodeBond[] bonds) {
        for (NodeBond bond : bonds) {
            Unit unit = bond.getUnit();
            if (unit instanceof CMU) {
                CMU cmu = (CMU) unit;
                if (cmu.getCMUType() == CMU.CMU_LOW) {
                    return bond;
                }
            }
        }
        return null;
    }

    /**
     * NOTE, there may be more than one term connected to the same node, this
     * method just return the first one
     *
     * @param node
     * @return
     */
    public static Unit getUnit(String node, NodeBond[] bonds) {
        for (NodeBond bond : bonds) {
            if (bond.getNodeName().equals(node)) {
                return bond.getUnit();
            }
        }
        return null;
    }

    public static Unit getUnit(String node, NodeBond[] bonds, Meter meter) {
        for (NodeBond bond : bonds) {
            if (bond.getNodeName().equals(node)) {
//                return bond.getTerm();
                Unit unit = bond.getUnit();
                if (hasUnit(meter, unit)) {
                    return unit;
                }
            }
        }
        return null;
    }

    public static Unit[] getUnits(String node, NodeBond[] bonds) {
        List<Unit> units = new ArrayList();
        for (NodeBond bond : bonds) {
            if (node.equalsIgnoreCase(bond.getNodeName())) {
                units.add(bond.getUnit());
            }
        }
        return units.toArray(new Unit[units.size()]);
    }

    public static List<Unit> getUnitsInList(String node, NodeBond[] bonds) {
        List<Unit> units = new ArrayList();
        for (NodeBond bond : bonds) {
            if (node.equalsIgnoreCase(bond.getNodeName())) {
                units.add(bond.getUnit());
            }
        }
        return units;
    }

    /**
     * @param unit
     * @return bias for the term, or null if no bias
     */
    public static MeaBias getUnitBias(Unit unit, NodeBond[] bonds, MeaBias[] biases) {
        for (NodeBond bond : bonds) {
            if (bond.getUnit().equals(unit)) {
                // find node first
                String node = bond.getNodeName();
                for (MeaBias bias : biases) {
                    if (bias.getFrom().equals(node)) {
                        return bias;
                    }
                }
            }
        }
        return null;
    }

    public static MeaBias getBias(String node, MeaBias[] biases) {
        for (MeaBias bias : biases) {
            if (bias.getFrom().equals(node)) {
                return bias;
            }
        }
        return null;
    }

    public static double getShift(MeaBias bias, Map<MeaBias, MeaBias> dependMap) {
        double shift = 0;
        while (true) {
            bias = (MeaBias) dependMap.get(bias);
            if (bias == null) {
                break;
            }
            shift += bias.get(0);
        }
        return shift;
    }

    public static double[] getShifts(OrderedBias[] orderedBiases, MeaBias[] biases) {
        double[] shifts = new double[biases.length];
        Map<MeaBias, MeaBias> dependMap = MeaUtil.buildDependMap(biases, MeaAnalysis.GND);
        for (int i = 0; i < shifts.length; i++) {
            if (i == 1 && orderedBiases[i].type == OrderedBias.TYPE_SYNC) {
                continue;
            }
            shifts[i] = MeaUtil.getShift(biases[i], dependMap);
        }
        return shifts;
    }
}
