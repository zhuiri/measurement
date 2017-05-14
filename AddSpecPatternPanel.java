/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.spec.SpecPattern;
import com.platformda.utility.Iconable;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.provider.GenericSelectionPanel;
import com.platformda.utility.table.IconableCellRenderer;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import org.netbeans.swing.etable.ETable;

/**
 *
 * @author Junyi
 */
public class AddSpecPatternPanel extends JPanel {

    RoutinePattern routinePattern;
    //List<SpecPattern> specPatterns;
    List<SPIconable> spis = new ArrayList<SPIconable>();
    List<Iconable> iconables = new ArrayList<Iconable>();       
    GenericSelectionPanel<Iconable> selectionPanel;
    //
    DefaultComboBoxModel<SpecPattern> comboModel;
    JComboBox<SpecPattern> combo;
    SpecPatternUpdatePanel updatePanel;

    public AddSpecPatternPanel(RoutinePattern routinePattern) {
        this.routinePattern = routinePattern;
        initComponents();
    }

    private void initComponents() {
        List<SpecPattern> valids = new ArrayList<SpecPattern>();
        List<SpecPattern> specPatterns = routinePattern.getSpecPatterns();
        for (SpecPattern specPattern : specPatterns) {
            if (routinePattern.hasDeviceBond(specPattern)) {
                valids.add(specPattern);
            }
        }
         for (SpecPattern pattern : specPatterns) {
             SPIconable pi = new SPIconable();
             pi.pattern = pattern;
             pi.icon = TreeVarSelector.specValidIcon;
             spis.add(pi);
        }
        
        selectionPanel = new GenericSelectionPanel<Iconable>(iconables, "Pattern", "Patterns", false);
        selectionPanel.setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE2);
        
        IconableCellRenderer cellRenderer = new IconableCellRenderer();
        final ETable table = selectionPanel.getTable();
        TableColumnModel tcm = table.getColumnModel();     
        tcm.getColumn(1).setCellRenderer(cellRenderer);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int rowIndex = table.getSelectedRow();
                Object pattern = spis.get(rowIndex).getPattern();
                if (e.getClickCount() == 1) {
                    if (table.getSelectedRowCount() == 1) {
                        //todo
                        SpecPattern specPattern = (SpecPattern) pattern;
                        SpecPattern maybeUpdate =  updatePanel.updatedPatterns.get(specPattern.getName());
                        if(maybeUpdate!=null){
                            updatePanel.setSpecPattern(maybeUpdate);
                        }else{
                            updatePanel.setSpecPattern(specPattern);
                        }
                       
                    }                   
                }
            }
        });
        
        onDeviceBondSelected();
        

//        comboModel = new DefaultComboBoxModel<SpecPattern>(valids.toArray(new SpecPattern[valids.size()]));
//        combo = new JComboBox<SpecPattern>(comboModel);
//        combo.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                if (e.getStateChange() == ItemEvent.SELECTED) {
//                    updatePanel.setSpecPattern((SpecPattern) combo.getSelectedItem());
//                }
//            }
//        });

        updatePanel = new SpecPatternUpdatePanel(routinePattern);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Spec: "), BorderLayout.WEST);
        //topPanel.add(combo, BorderLayout.CENTER);
        topPanel.add(selectionPanel, BorderLayout.SOUTH);

//        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
////        centerPanel.add(new JLabel("Applied to: "), BorderLayout.NORTH);
//        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
//        centerPanel.add(treeScroll, BorderLayout.CENTER);

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(updatePanel, BorderLayout.CENTER);

        //updatePanel.setSpecPattern((SpecPattern) combo.getSelectedItem());
        if(spis.size()>0){
            //default select row 0
            table.getSelectionModel().setSelectionInterval(0, 0);
            table.scrollRectToVisible(table.getCellRect(0, 0, true));            
            //selectionPanel.select(new int[]{0});
            updatePanel.setSpecPattern((SpecPattern) spis.get(0).getPattern());            
        }        
    }
    
       public void onDeviceBondSelected() {
        iconables.clear();
        for (SPIconable pi : spis) {      
              pi.icon = TreeVarSelector.specValidIcon;
             iconables.add(pi);
        }
   
        selectionPanel.deselectAll();
    }

    public IndSpecPattern getIndSpecPattern() {
        return updatePanel.getIndSpecPattern();
    }
    
    public List<IndSpecPattern> getSelectedIndSpecPatterns(){
        List<Iconable> selected = selectionPanel.getSelected();
        List<IndSpecPattern> selectIndSpecPatterns = new ArrayList<>();
        //get updated pattens
        Map<String,SpecPattern> updatedPatterns = updatePanel.getUpdatedPatterns();
        
        for(int index = 0;index<selected.size();index++){
            SPIconable spi = (SPIconable)selected.get(index);
            IndSpecPattern indPattern = new IndSpecPattern();  
            if (updatedPatterns.get(spi.getPattern().getName()) != null) {
                indPattern.specPattern = updatedPatterns.get(spi.getPattern().getName());
            } else {
                indPattern.specPattern = spi.getPattern();
            }
            updatePanel.setSpecPattern(indPattern.specPattern);
            BaseVarProvider varProvider =  updatePanel.getVarProvider();
            BaseVarProvider.copyVars(varProvider, indPattern);
          
            
           //indPattern.specPattern = spi.getPattern();
            selectIndSpecPatterns.add(indPattern);
        }       
        
        return selectIndSpecPatterns;
    } 
    
    private class SPIconable implements Iconable {

        SpecPattern pattern;
        Icon icon;
        String name;

        public SpecPattern getPattern(){
            return pattern;
        }
        
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String getName() {
            return pattern.getName();
        }
    }
}
