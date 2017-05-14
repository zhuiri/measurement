/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceType;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.datacore.pattern.EntityPagePatternGroup;
import com.platformda.ep.VarContext;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.iv.measure.DeviceBondProvider;
import com.platformda.spec.BaseSpecPattern;
import com.platformda.spec.SpecPattern;
import com.platformda.spec.SpecPatternGroup;
import com.platformda.spec.SpecPatternProvider;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.common.VarProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Junyi
 */
public class RoutinePattern extends BaseVarProvider implements SpecPatternProvider, VarContext, DeviceBondProvider {

    protected DeviceType deviceType;
    List<EntityPagePattern> pagePatterns = new ArrayList<EntityPagePattern>();
    List<SpecPattern> specPatterns = new ArrayList<SpecPattern>();
    // bond related
    DeviceBond defaultBond = null;
    List<DeviceBond> deviceBonds = new ArrayList<DeviceBond>();
//    Map<EntityPagePattern, DeviceBond> pageBonds = new HashMap<EntityPagePattern, DeviceBond>();
//    Map<SpecPattern, DeviceBond> specBonds = new HashMap<SpecPattern, DeviceBond>();
    Map<String, DeviceBond> bondMap = new HashMap<String, DeviceBond>();

    public RoutinePattern(DeviceType deviceType) {
        this.deviceType = deviceType;
//        DeviceBond bond = new DeviceBond("Default");
//        bond.setDeviceType(deviceType);
//        deviceBonds.add(bond);
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public SpecPattern getSpecPattern(String name) {
        for (SpecPattern pattern : specPatterns) {
            if (pattern.getName().equalsIgnoreCase(name)) {
                return pattern;
            }
        }
        return null;
    }

    public List<SpecPattern> getNeutralSpecPatterns() {
        // TODO:
        return null;
    }

    public void loadPagePattern(EntityPagePatternGroup patternGroup) {
//        syncVars(patternGroup);
//        List<EntityPagePattern> patterns = patternGroup.getPatterns();
//        pagePatterns.addAll(patterns);
        List<String> vars = new ArrayList();
        List<EntityPagePattern> patterns = patternGroup.getPatterns();
        for (EntityPagePattern pattern : patterns) {
            if (pattern.getDeviceType() == deviceType) {
                pagePatterns.add(pattern);
                vars.clear();
                pattern.fetchVars(vars);
                for (String var : vars) {
                    this.setVar(var, patternGroup.getVar(var));
                }
            }
        }
    }

    public void loadSpecPattern(SpecPatternGroup patternGroup) {
//        syncVars(patternGroup);
        List<SpecPattern> patterns = patternGroup.getPatterns();
        List<String> vars = new ArrayList();
        for (SpecPattern pattern : patterns) {
            if (pattern.getDeviceType() == deviceType) {
                specPatterns.add(pattern);
                BaseSpecPattern bsp = (BaseSpecPattern) pattern;
                vars.clear();
                pattern.fetchSimulationVarNames(vars);
                for (String var : vars) {
                    this.setVar(var, patternGroup.getVar(var));
                }
            }
        }
    }

//    public void addSpecPattern(SpecPattern specPattern, List<SpecPattern> addedDepends) {
//        if (!specPatterns.contains(specPattern)) {
//            specPatterns.add(specPattern);
//        }
//        List<SpecPattern> depends = specPattern.getAllDepends();
//        if (depends != null) {
//            for (SpecPattern depend : depends) {
//                if (!specPatterns.contains(depend)) {
//                    specPatterns.add(depend);
//                    if (addedDepends != null) {
//                        addedDepends.add(depend);
//                    }
////                                CheckableTreeNode checkableTreeNode = CheckableTreeUtil.getTreeNode(tree, depend);
////                                checkableTreeNode.setChecked(true);
//                }
//            }
//        }
//    }
    public void syncVars(VarProvider other) {
        List<String> existingVarNames = this.getVarNames();
        List<String> specVarNames = other.getVarNames();

        for (String varName : specVarNames) {
            if (existingVarNames.contains(varName)) {
                other.setVar(varName, this.getVar(varName));
            } else {
                this.setVar(varName, other.getVar(varName));
            }
        }
    }

    // utility method after adding or removing page/spec pattern
    public void updateVars(VarProvider varProvider) {
        List<String> vars = new ArrayList();
        for (EntityPagePattern pattern : pagePatterns) {
            pattern.fetchVars(vars);
        }
        for (SpecPattern pattern : specPatterns) {
            pattern.fetchSimulationVarNames(vars);
        }

        List<String> existingVarNames = getVarNames();
        for (String var : existingVarNames) {
            if (!StringUtil.contains(vars, var)) {
                removeVar(var);
            }
        }
        for (String var : vars) {
            if (!StringUtil.contains(existingVarNames, var)) {
                if (varProvider != null) {
                    setVar(var, varProvider.getVar(var));
                } else {
                    setVar(var, 0);
                }
            }
        }
    }

    @Override
    public Object getVarValue(String key) {
        return getVar(key);
    }

    public List<SpecPattern> getSpecPatterns() {
        return specPatterns;
    }

    @Override
    public int getSpecPatternNumber() {
        return specPatterns.size();
    }

    @Override
    public SpecPattern getSpecPattern(int index) {
        return specPatterns.get(index);
    }

    @Override
    public SpecPattern getSpecPatternByName(String name) {
        for (SpecPattern specPattern : specPatterns) {
            if (specPattern.getName().equalsIgnoreCase(name)) {
                return specPattern;
            }
        }
        return null;
    }

    public List<DeviceBond> getDeviceBonds() {
        return deviceBonds;
    }

    public void removeDeviceBond(DeviceBond deviceBond) {
        deviceBonds.remove(deviceBond);

        List<String> removed = new ArrayList<String>();
        for (Map.Entry<String, DeviceBond> entry : bondMap.entrySet()) {
            String key = entry.getKey();
            DeviceBond bond = entry.getValue();
            if (bond == deviceBond) {
                removed.add(key);
            }
        }
        for (String key : removed) {
            bondMap.remove(key);
        }
    }

    public DeviceBond getDeviceBondByName(String deviceBondName) {
        for (DeviceBond deviceBond : deviceBonds) {
            if (deviceBond.getName().equalsIgnoreCase(deviceBondName)) {
                return deviceBond;
            }
        }
        return null;
    }

    public DeviceBond getDefaultBond() {
        return defaultBond;
    }

    public void setDefaultBond(DeviceBond defaultBond) {
        this.defaultBond = defaultBond;
    }

    public boolean hasDeviceBond(String keyInLower) {
//        return bondMap.containsKey(keyInLower);
        return defaultBond != null;
    }

    public DeviceBond getDeviceBond(String keyInLower) {
//        return bondMap.get(keyInLower);
        DeviceBond bond = bondMap.get(keyInLower);
        if (bond == null) {
            return defaultBond;
        }
        return bond;
    }

    @Override
    public boolean hasDeviceBond(EntityPagePattern pagePattern) {
//        return bondMap.containsKey(pagePattern.getName().toLowerCase());
        return defaultBond != null;
    }

    @Override
    public DeviceBond getDeviceBond(EntityPagePattern pagePattern) {
//        return bondMap.get(pagePattern.getName().toLowerCase());
        DeviceBond bond = bondMap.get(pagePattern.getName().toLowerCase());
        if (bond == null) {
            return defaultBond;
        }
        return bond;
    }

    @Override
    public void setDeviceBond(EntityPagePattern pagePattern, DeviceBond deviceBond) {
        bondMap.put(pagePattern.getName().toLowerCase(), deviceBond);
    }

    @Override
    public boolean hasDeviceBond(SpecPattern specPattern) {
//        return bondMap.containsKey(specPattern.getName().toLowerCase());
        return defaultBond != null;
    }

    @Override
    public DeviceBond getDeviceBond(SpecPattern specPattern) {
//        return bondMap.get(specPattern.getName().toLowerCase());
        DeviceBond bond = bondMap.get(specPattern.getName().toLowerCase());
        if (bond == null) {
            return defaultBond;
        }
        return bond;
    }

    @Override
    public void setDeviceBond(SpecPattern specPattern, DeviceBond deviceBond) {
        bondMap.put(specPattern.getName().toLowerCase(), deviceBond);
    }

    public List<EntityPagePattern> getPagePatterns() {
        return pagePatterns;
    }
    
    public EntityPagePattern getPagePattern(int index){
        return pagePatterns.get(index);
    }

    public EntityPagePattern getPagePattern(String pageName) {
        for (EntityPagePattern pagePattern : pagePatterns) {
            if (pagePattern.getName().equalsIgnoreCase(pageName)) {
                return pagePattern;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.deviceType != null ? this.deviceType.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoutinePattern other = (RoutinePattern) obj;
        if (this.deviceType != other.deviceType && (this.deviceType == null || !this.deviceType.equals(other.deviceType))) {
            return false;
        }
        return true;
    }
}
