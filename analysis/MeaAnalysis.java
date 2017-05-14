package com.platformda.iv.analysis;

import com.platformda.datacore.DeviceType;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.admin.Routine;
import com.platformda.iv.api.Compliance;
import com.platformda.iv.api.InputType;
import com.platformda.iv.api.MeaRequest;
import com.platformda.iv.api.MeaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Analysis Request
 *
 * Analysis as can be seen from the app point of view
 */
public class MeaAnalysis implements Cloneable, MeaRequest {

    public static final String GND = "GND";
    private final DeviceType deviceType;
    Routine routine;
    EntityPagePattern pagePattern;
    private MeaType meaType;
    private String name;
    private String refNode;
    private double refValue; // TODO: remove
    private MeaBias[] biases = new MeaBias[0];
    private List<MeaOutput> outputs = new ArrayList();
    private List<AdvMeaOutput> advOutputs = new ArrayList();
    private Compliance[] compliances = new Compliance[0];
    //
    double[] xValues;
    double[] pValues;

    public MeaAnalysis(DeviceType deviceType, MeaType meaType, String name) {
        this.deviceType = deviceType;
        this.meaType = meaType;
        this.name = name;
        // TODO:
//        this.refNode = deviceType.getDefaultRefNode();
    }

    // create a new analysis with the same values to another
    public MeaAnalysis(final MeaAnalysis analysis) {
        this.deviceType = analysis.deviceType;
        this.meaType = analysis.meaType;
        this.name = analysis.name;
        this.refNode = analysis.refNode;
        for (int i = 0; i < analysis.outputs.size(); i++) {
            final MeaOutput output = (MeaOutput) analysis.outputs.get(i);
            addOutput(new MeaOutput(output));
        }
        for (int i = 0; i < analysis.advOutputs.size(); i++) {
            AdvMeaOutput output = (AdvMeaOutput) analysis.advOutputs.get(i);
            addAdvOutput(output);
        }
        this.refValue = analysis.refValue;
    }

    public Routine getRoutine() {
        return routine;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }

    public EntityPagePattern getPagePattern() {
        return pagePattern;
    }

    public void setPagePattern(EntityPagePattern pagePattern) {
        this.pagePattern = pagePattern;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    @Override
    public MeaType getMeaType() {
        return meaType;
    }

    public void setMeaType(MeaType meaType) {
        this.meaType = meaType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getRefNode() {
        return refNode;
    }

    public void setRefNode(String refNode) {
        this.refNode = refNode;
    }

    public void setBiases(MeaBias[] biases) {
        this.biases = biases;
    }

    public MeaBias[] getBiases() {
        return biases;
    }

    public boolean hasOutput(String name) {
        for (MeaOutput output : outputs) {
            if (output.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public void addOutput(MeaOutput output) {
        this.outputs.add(output);
    }

    public void addAdvOutput(AdvMeaOutput output) {
        this.advOutputs.add(output);
    }

    public MeaOutput[] getOutputsInArray() {
        return outputs.toArray(new MeaOutput[outputs.size()]);
    }

    public int getOutputNumber() {
        return outputs.size();
    }

    public MeaOutput getOutput(int index) {
        return outputs.get(index);
    }

    public int getAdvOutputNumber() {
        return advOutputs.size();
    }

    public AdvMeaOutput getAdvOutput(int index) {
        return advOutputs.get(index);
    }

    public Compliance[] getCompliances() {
        return compliances;
    }

    public void setCompliances(Compliance[] compliances) {
        this.compliances = compliances;
    }

    public double getRefValue() {
        return refValue;
    }

    public void setRefValue(double refValue) {
        this.refValue = refValue;
    }

    public boolean removeAdvOutput(AdvMeaOutput output) {
        return advOutputs.remove(output);
    }

    public boolean removeOutput(MeaOutput output) {
        return outputs.remove(output);
    }

    public void addBias(MeaBias bias) {
        MeaBias[] ms = new MeaBias[biases.length + 1];
        System.arraycopy(biases, 0, ms, 0, biases.length);
        ms[biases.length] = bias;
        this.biases = ms;
    }

    public boolean removeBias(MeaBias bias) {
        List sl = new ArrayList(Arrays.asList(biases));
        boolean r = sl.remove(bias);
        biases = new MeaBias[sl.size()];
        sl.toArray(biases);
        return r;
    }

    @Override
    public Object clone() {
        try {
            MeaAnalysis analysis = (MeaAnalysis) super.clone();
            analysis.advOutputs = new ArrayList();
            for (int i = 0; i < getAdvOutputNumber(); i++) {
                analysis.advOutputs.add((AdvMeaOutput) getAdvOutput(i).clone());
            }
            analysis.outputs = new ArrayList();
            for (int i = 0; i < getOutputNumber(); i++) {
                analysis.outputs.add((MeaOutput) getOutput(i).clone());
            }
            analysis.compliances = new Compliance[compliances.length];
            for (int i = 0; i < compliances.length; i++) {
                analysis.compliances[i] = (Compliance) compliances[i].clone();
            }
            analysis.biases = new MeaBias[biases.length];
            for (int i = 0; i < biases.length; i++) {
                analysis.biases[i] = (MeaBias) biases[i].clone();
            }
            return analysis;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * @return The sweeps needed for the measurement, this method will add the
     * refNode as a point sweep if it is not the GND node.
     */
    public MeaBias[] getMeasureBiases() {
        if (GND.equals(refNode)) {
            return biases;
        } else {
            // check first
            for (MeaBias bias : biases) {
                if (bias.getTo().equalsIgnoreCase(GND) && bias.getFrom().equalsIgnoreCase(refNode)) {
                    return biases;
                }
            }

            // should never hit
            MeaBias[] meaBiases = new MeaBias[biases.length + 1];
            System.arraycopy(biases, 0, meaBiases, 0, biases.length);
            MeaBias refBias = new MeaBias();
            refBias.setName("Ref");
            refBias.setType(InputType.VOLTAGE);
            refBias.setFrom(refNode);
            refBias.setTo(GND);
            refBias.addBundle(new ConstMeaBundle(refValue));
            meaBiases[biases.length] = refBias;
            return meaBiases;
        }
    }

    /**
     * scale relative signals in this analysis with the given factor
     *
     * @param factor
     */
    public void scaleRelative(double factor) {
        refValue *= factor;
        for (int i = 0; i < biases.length; i++) {
            if (biases[i].getType().isRelative()) {
                biases[i].scale(factor);
            }
        }
    }

    public void setXValues(double[] xValues) {
        this.xValues = xValues;
    }

    public void setPValues(double[] pValues) {
        this.pValues = pValues;
    }

    public int getXNumber() {
        if (xValues != null) {
            return xValues.length;
        }
        if (biases.length == 0) {
            return 0;
        }
        return biases[0].size();
    }

    public double getX(int index) {
        if (xValues != null) {
            return xValues[index];
        }
        return biases[0].get(index);
    }

    public int getPNumber() {
        if (pValues != null) {
            return pValues.length;
        }
        if (biases.length < 2) {
            return 1;
        }
        return biases[1].size();
    }

    public double getP(int index) {
        if (pValues != null) {
            return pValues[index];
        }
        if (biases.length > 1) {
            return biases[1].get(index);
        }
        return 0;
    }
}
