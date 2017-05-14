package com.platformda.iv.analysis;

import com.platformda.iv.api.OutputType;

public class MeaOutput implements Cloneable {

    private String node;
    private OutputType type;
    private String name;
    private boolean asOutput = true;

    public MeaOutput(MeaOutput output) {
        this(output.getName(), output.getNode(), output.getType());
    }

    public MeaOutput(String name, String node, OutputType type) {
        this.name = name;
        this.node = node;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNode(String nodeShortName) {
        this.node = nodeShortName;
    }

    public void setType(OutputType type) {
        this.type = type;
    }

    public String getNode() {
        return node;
    }

    public OutputType getType() {
        return type;
    }

    public boolean asOutput() {
        return asOutput;
    }

    public void setAsOutput(boolean asOutput) {
        this.asOutput = asOutput;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
