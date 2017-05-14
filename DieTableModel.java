/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.utility.Iconable;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Junyi
 */
public class DieTableModel extends AbstractTableModel {

    static String[] columnNames = {"Selected", "Status", "Index", "X", "Y"};
    List<WaferDieInfo> items;
    List<WaferDieInfo> selecteds;
    boolean checkable = false;
    StatusProvider statusProvider;

    public DieTableModel(List<WaferDieInfo> items) {
        this.items = items;
    }

    public DieTableModel(List<WaferDieInfo> items, List<WaferDieInfo> selecteds) {
        this.items = items;
        this.selecteds = selecteds;
        checkable = selecteds != null;
    }

    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length - (checkable ? 0 : 1);
    }

    @Override
    public String getColumnName(int column) {
        if (!checkable) {
            column += 1;
        }
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (!checkable) {
            columnIndex += 1;
        }
        if (columnIndex == 0) {
            return Boolean.class;
        }
        if (columnIndex == 1) {
            return Iconable.class;
        }

        return super.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (!checkable) {
            columnIndex += 1;
        }
        return columnIndex == 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        WaferDieInfo dieInfo = items.get(rowIndex);

        if (!checkable) {
            columnIndex += 1;
        }

        switch (columnIndex) {
            case 0:
                return selecteds.contains(dieInfo);
            case 1:
//                if (statusProvider != null) {
//                    IconableImpl impl = new IconableImpl(dieInfo.getName(), dieInfo.getIcon(statusProvider.getStatus(dieInfo)));
//                    return impl;
//                }
//                return dieInfo;

                if (statusProvider != null) {
                    int status = statusProvider.getStatus(dieInfo);
                    return WaferDieInfo.getIconable(status);
                }
                return WaferDieInfo.getIconable(dieInfo.getStatus());
            case 2:
                return dieInfo.getDieIndex();
            case 3:
                return dieInfo.getX();
            case 4:
//                return rt.meaDevice.toString();
                return dieInfo.getY();
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (!checkable) {
            return;
        }
        if (columnIndex == 0) {
            WaferDieInfo dieInfo = items.get(rowIndex);
            boolean selected = ((Boolean) value).booleanValue();
            if (selected) {
                selecteds.add(dieInfo);
            } else {
                selecteds.remove(dieInfo);
            }
        }
    }
}
