/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceType;
import com.platformda.iv.api.MeaEditor;
import com.platformda.iv.api.NodeBond;
import com.platformda.iv.api.UnitBond;
import com.platformda.iv.help.DialogHelper;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.utility.ui.JTableUtil;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;
import org.netbeans.swing.etable.ETable;

/**
 *
 * @author Junyi
 */
public class DeviceBondPanel extends JPanel implements ActionListener, MeaEditor {

    DeviceBond deviceBond;
    DeviceType deviceType;
    //
    DeviceBondTableModel tableModel;
    ETable table;
    //
    JButton addButton;
    JButton removeButton;
    //
    String[] nodes;
    UnitBond[] termBonds;

    public DeviceBondPanel(DeviceBond deviceBond) {
        this.deviceBond = deviceBond;
        deviceType = deviceBond.getDeviceType();
        initComponents();
    }

    private void initComponents() {
        nodes = deviceType.getNodeNames();

        List<UnitBond> bonds = InstrumentManager.getInstance().getTermBonds();
        termBonds = bonds.toArray(new UnitBond[bonds.size()]);

        tableModel = new DeviceBondTableModel(deviceBond);
        table = new ETable(tableModel) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                int rowInModel = table.convertRowIndexToModel(row);
                int columnInModel = table.convertColumnIndexToModel(column);
                if (columnInModel == 0) {
                    JComboBox comboBox = new JComboBox(nodes);
                    return new DefaultCellEditor(comboBox);
                } else if (columnInModel == 1) {
                    JComboBox comboBox = new JComboBox(termBonds);
                    return new DefaultCellEditor(comboBox);
                }
                return super.getCellEditor(row, column);
            }
        };
        JScrollPane scrollPane = new JScrollPane(table);

        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        JPanel buttonWrapPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonWrapPanel.add(buttonPanel, BorderLayout.NORTH);

        setLayout(new BorderLayout());

        add(scrollPane, BorderLayout.CENTER);
        add(buttonWrapPanel, BorderLayout.EAST);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == removeButton) {
            // TODO:
            int index = table.getSelectedRow();
            index = table.convertRowIndexToModel(index);
            if (index >= 0) {
                deviceBond.removeBond(index);
                tableModel.fireTableDataChanged();
                JTableUtil.setSelectedIndex(table, tableModel, index - 1);
            }
        } else if (source == addButton) {
            int size = deviceBond.getBonds().length;
            if(size>=4){
                return;
            }
            String node = nodes[size % nodes.length];
            UnitBond termBond = null;
            if (termBonds.length > 0) {
                termBond = termBonds[size % termBonds.length];
            }
            NodeBond bond = new NodeBond(termBond == null ? "" : termBond.getInstName(), termBond == null ? null : termBond.getUnit(), node);
            deviceBond.addBond(bond);
            tableModel.fireTableDataChanged();
        }
    }

    private void checkBonds(){
         //check bonds
        NodeBond [] bonds = deviceBond.getBonds();
        StringBuilder warnMsg = new StringBuilder();
        HashMap<String,Integer> map = new HashMap<>();
        for(int index =0;index<bonds.length;index++){
            NodeBond bond = bonds[index];
            if(bond !=null){
                String termName = bond.getInstTermName();
                Integer num = map.get(termName);
                if(num ==null){
                    map.put(termName, 1);
                }else{
                    map.put(termName, num+1);
                }                
            }
        }
        
        for(Map.Entry<String,Integer> entry:map.entrySet()){
            String key = entry.getKey();
            Integer value = entry.getValue();
            if(key.toUpperCase().contains("SMU")
                &&value>1){
                warnMsg.append("num of ")
                        .append(key)
                        .append("error")
                        .append("\n");
            }            
        }
        if(warnMsg.length()!=0){
            DialogHelper.getMessageDialog(warnMsg.toString());
        }
    }
    
    @Override
    public void stopEditing() {
        checkBonds();        
        TableCellEditor cellEditor = table.getCellEditor();
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }
    }
}
