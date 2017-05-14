/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.api.NodeBond;
import com.platformda.iv.api.UnitBond;
import com.platformda.iv.measure.DeviceBond;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Junyi
 */
public class DeviceBondTableModel extends AbstractTableModel {

    public static String[] columnNames = new String[]{"Device Node", "Meter Node"};
    DeviceBond deviceBond;

    public DeviceBondTableModel(DeviceBond deviceBond) {
        this.deviceBond = deviceBond;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public int getRowCount() {
        return deviceBond.getBonds().length;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        NodeBond nodeBond = deviceBond.getBonds()[rowIndex];

        switch (columnIndex) {
            case 0:
                return nodeBond.getNodeName();
            default:
//                return nodeBond.getInstName() + " - " + nodeBond.getTerm().getName();
                return nodeBond.getInstTermName();
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (aValue == null) {
            return;
        }
        NodeBond nodeBond = deviceBond.getBonds()[rowIndex];
        if (columnIndex == 0) {
            nodeBond.setNodeName(aValue.toString());
        } else if (columnIndex == 1) {
            UnitBond bond = (UnitBond) aValue;
            nodeBond.setInstName(bond.getInstName());
            nodeBond.setUnit(bond.getUnit());
        }
    }
}
