/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Junyi
 */
public class MeaJob {

    MeaPlan plan; // can be null?
    String name;
    List<WaferDieInfo> dieInfos = new ArrayList<WaferDieInfo>();
    List<RoutineTuple> routineTuples = new ArrayList<RoutineTuple>();
    // 
    boolean asDefault = false;

    public MeaJob(MeaPlan plan, boolean selecAllDie) {
        this.plan = plan;

        name = plan.getName();
        if (selecAllDie) {
            dieInfos.addAll(plan.getDieInfos());
        }
        routineTuples.addAll(plan.getRoutineTuples());
    }

    public String getName() {
        return name;
    }

    public boolean isAsDefault() {
        return asDefault;
    }

    public void setAsDefault(boolean asDefault) {
        this.asDefault = asDefault;
    }

    public MeaPlan getPlan() {
        return plan;
    }

    public void setPlan(MeaPlan plan) {
        this.plan = plan;
    }

    public List<WaferDieInfo> getDieInfos() {
        return dieInfos;
    }

    public List<RoutineTuple> getRoutineTuples() {
        return routineTuples;
    }

    public boolean isValid() {
        return !dieInfos.isEmpty() && !routineTuples.isEmpty();
    }
}
