package com.platformda.iv.analysis;

import com.platformda.iv.api.InputType;
import java.util.ArrayList;
import java.util.List;

/**
 * Measurement values bias, composite of a list of MeaBundle
 *
 * @author Junyi
 */
public class MeaBias implements Cloneable {

    private String name;
    private String from;
    private String to;
    private InputType type;
    private List<MeaBundle> meaBundles = new ArrayList<MeaBundle>();

    public MeaBias(MeaBias bias) {
        this.name = bias.name;
        this.from = bias.from;
        this.to = bias.to;
        this.type = bias.type;
        this.meaBundles.addAll(bias.meaBundles);
    }

    public MeaBias() {
    }

    public MeaBias createReverse() {
        MeaBias bias = new MeaBias();
        bias.type = type;
        bias.from = to;
        bias.to = from;
        bias.name = name;
        for (int i = 0; i < meaBundles.size(); i++) {
            bias.meaBundles.add(meaBundles.get(i).reverse());
        }
        return bias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InputType getType() {
        return type;
    }

    public void setType(InputType type) {
        this.type = type;
    }

    public List<MeaBundle> getMeaBundles() {
        return meaBundles;
    }

    public int getBundleNumber() {
        return meaBundles.size();
    }

    public void clearBundles() {
        meaBundles.clear();
    }

    public MeaBundle getBundle(int index) {
        return meaBundles.get(index);
    }

    public void addBundle(MeaBundle bundle) {
        meaBundles.add(bundle);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String node1) {
        this.from = node1;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String node2) {
        this.to = node2;
    }

    public int size() {
        int size = 0;
        for (int i = 0; i < getBundleNumber(); i++) {
            MeaBundle bundle = getBundle(i);
            size += bundle.size();
        }
        return size;
    }

    public double get(int index) {
        int bundleIndex = 0;
        while (getBundle(bundleIndex).size() < index + 1) {
            index -= getBundle(bundleIndex).size();
            bundleIndex++;
        }
        return getBundle(bundleIndex).get(index);
    }

    @Override
    public Object clone() {
        try {
            MeaBias bias = (MeaBias) super.clone();
            bias.meaBundles = new ArrayList();
            for (int i = 0; i < meaBundles.size(); i++) {
                MeaBundle rawBundle = getBundle(i);
                bias.meaBundles.add((MeaBundle) rawBundle.clone());
            }
            return bias;
        } catch (CloneNotSupportedException e) {
            // should never happen
            e.printStackTrace();
        }
        return null;
    }

    public boolean removeBundle(MeaBundle bundle) {
        return meaBundles.remove(bundle);
    }

    void scale(double factor) {
        for (MeaBundle bundle : meaBundles) {
            bundle.scale(factor);
        }
    }
}
