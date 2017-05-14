/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.help.TableHelper;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.iv.measure.DeviceBondProvider;
import com.platformda.iv.tools.SettingManager;
//import com.platformda.iv.tools.GenericSelectionPanel;
import com.platformda.spec.SpecPattern;
import com.platformda.utility.Iconable;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.provider.GenericSelectionPanel;
import com.platformda.utility.table.ColoredIconableCellRenderer;
import com.platformda.utility.table.ColoredTableCellRenderer;
import com.platformda.utility.table.IconableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import org.netbeans.swing.etable.ETable;

/**
 *
 * @author Junyi
 */
public class AddPagePatternPanel extends JPanel  {
    
    DeviceBondProvider deviceBondProvider;
   // List<EntityPagePattern> pagePatterns;
    List<PPIconable> ppis = new ArrayList<PPIconable>();
    List<Iconable> iconables = new ArrayList<Iconable>();
    List<Color> colors = new ArrayList<Color>();
    
    GenericSelectionPanel<Iconable> selectionPanel;
    
    ///////////////////////
    RoutinePattern routinePattern;
    //
    DefaultComboBoxModel<EntityPagePattern> comboModel;
    JComboBox<EntityPagePattern> combo;
    AbstractPagePanel updatePanel;
    //for quick show
    Routine routine;
    String loadUI;
    int lastSelectRow;

//    public AddPagePatternPanel(RoutinePattern routinePattern) {
//        this.routinePattern = routinePattern;
//        initComponents();       
//    }
    
    public AddPagePatternPanel(RoutinePattern routinePattern,Routine routine) {
        lastSelectRow = 0;
        loadUI = SettingManager.getInsance().readValue("PagePatternUILoader");
        this.routinePattern = routinePattern;
        this.routine = routine;
        initComponents();       
    }

    private void initComponents() {
        List<EntityPagePattern> valids = new ArrayList<EntityPagePattern>();
        List<EntityPagePattern> pagePatterns = routinePattern.getPagePatterns();
        for (EntityPagePattern pagePattern : pagePatterns) {
            if (routinePattern.hasDeviceBond(pagePattern)) {
                valids.add(pagePattern);
            }
        }
        
        for (EntityPagePattern pattern : pagePatterns) {
            PPIconable pi = new PPIconable();
            pi.pattern = pattern;
            pi.icon = TreeVarSelector.pageValidIcon;
            ppis.add(pi);
        }
      
        selectionPanel = new GenericSelectionPanel<Iconable>(iconables, "Pattern", "Patterns", false);
        if(loadUI!=null&&loadUI.equalsIgnoreCase("true")){
            selectionPanel.setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE9);
        }else{
            selectionPanel.setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE2);
        }
        
        //set focuse for toolbars false
//        for(int index = 0;index<selectionPanel.getToolBar().getComponentCount();index++){
//            Component component = selectionPanel.getToolBar().getComponent(index);
//            if(component!=null){
//                component.setFocusable(false);
//            }
//        }
        
        IconableCellRenderer cellRenderer = new IconableCellRenderer();
        final ETable table = selectionPanel.getTable();
        TableColumnModel tcm = table.getColumnModel();     
        tcm.getColumn(1).setCellRenderer(cellRenderer);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //allow key up/down work
//        table.addKeyListener(new KeyListener(){
//            @Override
//            public void keyPressed(KeyEvent e) {
//                Object pattern = null;
//                if(e.getKeyCode() == KeyEvent.VK_DOWN){//|| e.getKeyCode() == KeyEvent.VK_PAGE_DOWN
//                    int rowIndex = table.getSelectedRow()+1;
//                    if(rowIndex == table.getRowCount()){
//                        return;
//                    }
//                    pattern = ppis.get(rowIndex).getPattern();              
//                    
//                }else if(e.getKeyCode() == KeyEvent.VK_UP){
//                     int rowIndex = table.getSelectedRow()-1;
//                    if(rowIndex == -1){
//                        return;
//                    }
//                    pattern = ppis.get(rowIndex).getPattern();                  
//                    
//                }
//                if (pattern != null) {
//                    EntityPagePattern pagePattern = (EntityPagePattern) pattern;
//                    EntityPagePattern maybeUpdate = updatePanel.getUpdatedPatterns().get(pagePattern.getName());
//                    updatePanel.setRawPatternName(pagePattern.getName());
//                    if (maybeUpdate != null) {
//                        updatePanel.setPagePattern(maybeUpdate);
//                    } else {
//                        updatePanel.setPagePattern(pagePattern);
//                    }
//                }
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {
//            }
//            @Override
//            public void keyTyped(KeyEvent e){
//                
//            }
//        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int rowIndex = table.getSelectedRow();
                Object pattern = ppis.get(rowIndex).getPattern();
                if (e.getClickCount() == 1) {
                    if (table.getSelectedRowCount() == 1) {
                        //todo
                        if (loadUI != null && loadUI.equalsIgnoreCase("true")) {
                            if (getErrorInfo() != null) {
                                TableHelper.selectTableRow(table, lastSelectRow);
                                table.setEnabled(false);
                                JPopupMenu popError = new JPopupMenu();
                                JMenuItem item = new JMenuItem(getErrorInfo());
                                item.setForeground(Color.red);
                                popError.add(item);
//                            popError.setForeground(Color.red);
                                popError.show(table, e.getX(), e.getY());
                                return;
                            } else {
                                updatePanel.doUpdate();
                            }
                        }
                       
                        
                        EntityPagePattern pagePattern =  (EntityPagePattern) pattern;                        
                        EntityPagePattern maybeUpdate =  updatePanel.getUpdatedPatterns().get(pagePattern.getName());
                        updatePanel.setRawPatternName(pagePattern.getName());
                        if(maybeUpdate!=null){                           
                            updatePanel.setPagePattern(maybeUpdate);
                        }else{
                            updatePanel.setPagePattern(pagePattern);
                        }
                        
                        lastSelectRow = rowIndex;
                        table.setEnabled(true);
                       
                    }
                   
                }

            }
        });
      
       
       onDeviceBondSelected();
        

//        comboModel = new DefaultComboBoxModel<EntityPagePattern>(valids.toArray(new EntityPagePattern[valids.size()]));
//        combo = new JComboBox<EntityPagePattern>(comboModel);
//        combo.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                if (e.getStateChange() == ItemEvent.SELECTED) {
//                    updatePanel.setPagePattern((EntityPagePattern) combo.getSelectedItem(),routine);
//                }
//            }
//        });
        String loadUI = SettingManager.getInsance().readValue("PagePatternUILoader");
        if (loadUI != null && loadUI.equalsIgnoreCase("true")) {
            updatePanel = new PagePatternUpdateUI();//routinePattern
        } else {
            updatePanel = new PagePatternUpdatePanel(routinePattern);
        }
        

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Page: "), BorderLayout.WEST);
        //topPanel.add(combo, BorderLayout.CENTER);
        
        topPanel.add(selectionPanel,BorderLayout.SOUTH);

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(updatePanel, BorderLayout.CENTER);

        //updatePanel.setPagePattern((EntityPagePattern) combo.getSelectedItem(),routine);
        if (ppis.size() > 0) {
            //default select row 0
           TableHelper.selectTableRow(table, 0);
            // selectionPanel.select(new int[]{0});
            updatePanel.setPagePattern((EntityPagePattern) ppis.get(0).getPattern(),routine);
        }       
        
    }
    
      public void onDeviceBondSelected() {
        iconables.clear();
        colors.clear();
        for (PPIconable pi : ppis) {      
              pi.icon = TreeVarSelector.pageValidIcon;
            //pi.icon = TreeVarSelector.pageValidIcon;
             iconables.add(pi);
        }
   
        selectionPanel.deselectAll();
    }
      
    public EntityPagePattern getPagePattern(){
        return updatePanel.getPagePattern();
    }

    public IndPagePattern getIndPagePattern() {       
        return updatePanel.getIndPagePattern();
    }
    
    public List<IndPagePattern> getSelectedIndPagePatterns(){
        List<Iconable> selected = selectionPanel.getSelected();
        List<IndPagePattern> selectIndPagePatterns = new ArrayList<>();
        //get updated pattens
        Map<String,EntityPagePattern> updatedPatterns = updatePanel.getUpdatedPatterns();
        
        for(int index = 0;index<selected.size();index++){
            PPIconable ppi = (PPIconable)selected.get(index);
            //selectPagePatterns.add(ppi.getPattern());
            IndPagePattern indPattern = new IndPagePattern();
            
            //get current select pattern
            //to allow always get latest current pattern
            EntityPagePattern curPattern = updatePanel.getPagePattern();
            if (curPattern.getName().equalsIgnoreCase(ppi.getPattern().getName())) {
                indPattern.pagePattern = curPattern;
            } else {
                if (updatedPatterns.get(ppi.getPattern().getName()) != null) {
                    indPattern.pagePattern = updatedPatterns.get(ppi.getPattern().getName());

                } else {
                    if (loadUI != null && loadUI.equalsIgnoreCase("true")) {
                        //save pattern without var
                        updatePanel.setPagePattern(ppi.getPattern());
                        indPattern.pagePattern = updatePanel.getPagePattern();

                    } else {
                        indPattern.pagePattern = ppi.getPattern();
                        //set pattern for copy value
                        updatePanel.setPagePattern(indPattern.pagePattern);
                    }
                }
            }
            
            
            BaseVarProvider varProvider = updatePanel.getVarProvider();
            BaseVarProvider.copyVars(varProvider, indPattern);
         
            selectIndPagePatterns.add(indPattern);
        }       
        
        return selectIndPagePatterns;
    } 
    
    public String getErrorInfo() {
        if (updatePanel == null) {
            return null;
        }

        return updatePanel.getErrorInfo();
    }
    
//      @Override
//    public Color getForeground(int row, int column) {
//        if (row < colors.size()) {
//            return colors.get(row);
//        }
//
//        return null;
//    }
//
//    @Override
//    public Color getBackground(int row, int column) {
//        return null;
//    }
    
     private class PPIconable implements Iconable {

        EntityPagePattern pattern;
        Icon icon;
        String name;

        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public String getName() {
            return pattern.getName();
        }
        
        public String toString() {
            return pattern.getName();
        }
        
        public EntityPagePattern getPattern(){
            return pattern;
        }
    }
}
