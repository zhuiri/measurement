/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.api.Probe;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * // TODO:
 *
 * @author Junyi
 */
public class DieGeometryTableModel extends AbstractTableModel {

    WaferInfo waferInfo;
    //
    int[] xValues;
    int[] yValues;

    public DieGeometryTableModel(WaferInfo waferInfo) {
        this.waferInfo = waferInfo;
        update();
    }

    private void update() {
        List<WaferDieInfo> allDieInfos = waferInfo.getDieInfos();
        List<Integer> distincts = DieGeometrySelectionPanel.getDistinctXValues(allDieInfos);
        xValues = new int[distincts.size()];
        for (int i = 0; i < xValues.length; i++) {
            xValues[i] = distincts.get(i);
        }
        distincts = DieGeometrySelectionPanel.getDistinctYValues(allDieInfos);
        yValues = new int[distincts.size()];
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = distincts.get(i);
        }
        DieGeometrySelectionPanel.reorder(xValues, yValues, Probe.ORDER_X_ASEND_Y_ASEND, waferInfo.getOrder());
    }

    public void onOrderChanged(int preOrder, int order) {
        DieGeometrySelectionPanel.reorder(xValues, yValues, preOrder, order);
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        if (yValues == null) {
            return 0;
        }
        return yValues.length;
//            return ySweepConsts.length + 1;
    }

    @Override
    public int getColumnCount() {
        if (xValues == null) {
            return 0;
        }
        return xValues.length + 1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
//            if (columnIndex != 0) {
//                return Boolean.class;
//            }
        return super.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "";
        }
        int x = xValues[columnIndex - 1];
        return String.format("x=%d", x);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            int y = yValues[rowIndex];
            return String.format("y=%d", y);
        }

        WaferDieInfo dieInfo = waferInfo.getDieInfo(xValues[columnIndex - 1], yValues[rowIndex]);
        if (dieInfo == null) {
            return null;
        }
        return dieInfo.getDieIndex();
    }
}
