/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.tools.TableModelInformerManager;
import com.platformda.utility.Iconable;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.common.VarProvider;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author junyi
 */
public class VarProviderEditingTableModel extends AbstractTableModel {

    String[] columnNames = new String[]{"Name", "Value"};
    VarProvider varProvider;
    List<String> varNames;
    //
    String referenceName;
    VarProvider referenceVP;
    VarIconProvider varIconProvider;
    String patternName;
    //pattern-vars map
    Map<String,VarProvider> updatedPatternVarsMap = new LinkedHashMap<>();

    
    public VarProviderEditingTableModel(String patternName,VarProvider varProvider, String referenceName, VarProvider referenceVP, VarIconProvider varIconProvider) {
        this( varProvider,  referenceName,  referenceVP,  varIconProvider);
        this.patternName = patternName;
    }
    
    // DeviceType
    // DevicePolarity
    // instances
    public VarProviderEditingTableModel(VarProvider varProvider, String referenceName, VarProvider referenceVP, VarIconProvider varIconProvider) {
        this.varProvider = varProvider;
        this.referenceName = referenceName;
        this.referenceVP = referenceVP;
        this.varIconProvider = varIconProvider;
        varNames = varProvider.getVarNames();
    }

    public void onVarChanged() {
        varNames = varProvider.getVarNames();
        fireTableStructureChanged();
    }

    public String getVarName(int rowIndex) {
        return varNames.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return varNames.size();
    }

    @Override
    public int getColumnCount() {
        if (referenceName != null && referenceVP != null) {
            return 3;
        }
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex < 2) {
            return columnNames[columnIndex];
        }

        return referenceName;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            if (varIconProvider != null) {
                return Iconable.class;
            } else {
                return super.getColumnClass(columnIndex);
            }
        }
        return super.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
//            Object varObj = varProvider.getVar(varNames.get(rowIndex));
//            if (varObj == null) {
//                return false;
//            }
//            if (referenceVP != null) {
//                Object refObj = referenceVP.getVar(varNames.get(rowIndex));
//                if (refObj != null) {
//                    return false;
//                }
//            }
//            return true;

            if (referenceVP != null) {
                return false;
            }
            return true;
        } else if (columnIndex == 2) {
            return true;
        }
        return false;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        VarProvider vp = varProvider;
        if (columnIndex == 2) {
            vp = referenceVP;
        }

        String str = value.toString();
        if (StringUtil.isValid(str)) {
            try {
                if (str.indexOf(",") != -1) {
                    String[] parts = str.split(",");
                    List<Double> values = new ArrayList<Double>();
                    for (int i = 0; i < parts.length; i++) {
                        values.add(Double.parseDouble(parts[i].trim()));
                    }
                    vp.setVar(varNames.get(rowIndex), values);
                } else {
                    Double d = Double.parseDouble(str);
                    vp.setVar(varNames.get(rowIndex), d);
                }
            } catch (NumberFormatException nfe) {
                vp.setVar(varNames.get(rowIndex), str);
            }
            //add into  pattern-vars map
            if(patternName!= null){
                VarProvider newVp = new BaseVarProvider();
                BaseVarProvider.copyVars(vp, newVp);
                updatedPatternVarsMap.put(patternName, newVp);
            }
            
            //inform quick show to change
            TableModelInformerManager.afterTableModelChanged();
           
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            String varName = varNames.get(rowIndex);
            if (varIconProvider != null) {
                return new IconableImpl(varName, varIconProvider.getIcon(varName));
            } else {
                return varNames.get(rowIndex);
            }
        }
        Object obj = null;
        if (columnIndex == 1) {
            if (patternName != null) {
                VarProvider oldVarProvider = updatedPatternVarsMap.get(patternName);
                if (oldVarProvider != null) {
                    //BaseVarProvider.copyVars(oldVarProvider, varProvider);
                    obj = oldVarProvider.getVar(varNames.get(rowIndex));
                }else{
                    obj = varProvider.getVar(varNames.get(rowIndex));
                }
            } else {
                obj = varProvider.getVar(varNames.get(rowIndex));
            }
            
        } else if (referenceVP != null) {
            obj = referenceVP.getVar(varNames.get(rowIndex));
        }
        if (obj == null) {
            return "";
        } else if (obj instanceof Number) {
            return String.valueOf(obj);
        } else if (obj instanceof List) {
            List list = (List) obj;
            return StringUtil.concatenate(list, ",");
        } else {
            return obj.toString();
        }
    }
    
    public Map<String,VarProvider> getUpdatedPatternVarsMap(){
        return this.updatedPatternVarsMap;
    }
    
    public void setPatternName(String patternName){
        this.patternName = patternName;
    }

    public static interface VarIconProvider {

        public Icon getIcon(String varName);
    }

    public static class IconableImpl implements Iconable {

        String name;
        Icon icon;

        public IconableImpl(String name, Icon icon) {
            this.name = name;
            this.icon = icon;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Icon getIcon() {
            return icon;
        }
    }
}
