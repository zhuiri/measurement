/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceInstanceUtil;
import com.platformda.datacore.DeviceProvider;
import com.platformda.datacore.DeviceSelector;
import com.platformda.datacore.DeviceType;
import com.platformda.iv.datacore.DeviceTypeManager;
import com.platformda.datacore.EntityDevice;
import com.platformda.iv.MeaData;
import com.platformda.iv.help.StringHelper;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author junyi
 */
public class MeaDeviceTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private MeaData meaData;
    private DeviceProvider provider;
    private String[] ignores;
    private DeviceSelector selector;
    boolean checkable;
    private String[] columnNames;
    boolean typePolarityVisible = false;

    public MeaDeviceTableModel(MeaData meaData) {
        this(meaData, meaData, null, false, null);
    }

    public MeaDeviceTableModel(MeaData meaData, DeviceProvider deviceProvider, DeviceSelector deviceSelector, boolean typePolarityVisible, String[] ignores) {
        this.meaData = meaData;
        this.provider = deviceProvider;
        this.selector = deviceSelector;
        this.typePolarityVisible = typePolarityVisible;
        this.ignores = ignores;
        checkable = deviceSelector != null;
        if (provider.getDeviceNumber() == 0) {
            DeviceType deviceType = provider.getDeviceType();
            columnNames = deviceType.getPrimaryInstanceNames();
        } else {
            columnNames = DeviceInstanceUtil.getExcludedInstanceNames(provider, ignores).toArray(new String[0]);
        }
    }

    public void deviceChanged() {
//        columnNames = DeviceInstanceUtil.getExcludedInstanceNames(provider, ignores).toArray(new String[0]);        
        if (provider.getDeviceNumber() == 0) {
            DeviceType deviceType = provider.getDeviceType();
            columnNames = deviceType.getPrimaryInstanceNames();
        } else {
            columnNames = DeviceInstanceUtil.getExcludedInstanceNames(provider, ignores).toArray(new String[0]);
        }

        fireTableStructureChanged();
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (!checkable) {
            columnIndex += 1;
        }
        switch (columnIndex) {
            case 0:
                return "Selected";
            case 1:
                return "ID";
            default:
                if (typePolarityVisible) {
                    if (columnIndex == 2) {
                        return "Device Type";
                    } else {
                        return columnNames[columnIndex - 3];
                    }
                } else {
                    return columnNames[columnIndex - 2];
                }
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (!checkable) {
            columnIndex += 1;
        }
        if (columnIndex == 0) {
            return Boolean.class;
        }

        if (typePolarityVisible) {
            if (columnIndex > 2) {
                return String.class;
            }
        } else {
            if (columnIndex > 1) {
                return String.class;
            }
        }

        return super.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (!checkable) {
            columnIndex += 1;
        }
        if (columnIndex == 1 || (typePolarityVisible && columnIndex == 2)) {
            return false;
        }
//        return true;
        return columnIndex == 0 || meaData != null;
    }
    

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (!checkable) {
            columnIndex += 1;
        }
        if (columnIndex == 0) {
            selector.setDeviceSelected(provider.getDevice(rowIndex), ((Boolean) value).booleanValue());
        } else {
            EntityDevice device = provider.getDevice(rowIndex);
            String str = value.toString();           
            Double d = 0.0;
            if(str.isEmpty()||str.equalsIgnoreCase("NAN")){
                d = Double.NaN;
            } else {
                if (!StringHelper.isDouble(str)) {
                    return;
                }
                d = Double.parseDouble(str);
            }
            
            if (typePolarityVisible) {
                device.setInstance(columnNames[columnIndex - 3], d);
            } else {
                device.setInstance(columnNames[columnIndex - 2], d);
            }
            meaData.updateDevice(device);
        }
    }

    @Override
    public int getColumnCount() {
        int count = columnNames.length + 1;

        if (checkable) {
            count++;
        }
        if (typePolarityVisible) {
            count++;
        }

        return count;
    }

    @Override
    public int getRowCount() {
        return provider.getDeviceNumber();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!checkable) {
            columnIndex += 1;
        }

        if (columnIndex == 0) {
            return selector.isDeviceSelected(provider.getDevice(rowIndex));
        }

        if (columnIndex == 1) {
            return provider.getDevice(rowIndex).getId();
        }

        columnIndex -= 2;

        if (typePolarityVisible) {
            if (columnIndex == 0) {
                return DeviceTypeManager.toString(provider.getDevice(rowIndex));
//                return provider.getDevice(rowIndex).getDevicePolarity().getName();
            }
            columnIndex--;
        }

        return provider.getDevice(rowIndex).getInstance(columnNames[columnIndex]);
    }
}
