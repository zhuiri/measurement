/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceType;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.iv.measure.DeviceBondProvider;
import com.platformda.spec.SpecPattern;
import com.platformda.utility.Iconable;
import com.platformda.utility.provider.GenericSelectionPanel;
import com.platformda.utility.table.ColoredIconableCellRenderer;
import com.platformda.utility.table.ColoredTableCellRenderer.ColorContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.table.TableColumnModel;
import org.netbeans.swing.etable.ETable;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Junyi
 */
public class SetConnectionInTablePanel extends JPanel implements ColorContext {

    DeviceType deviceType;
    DeviceBondProvider deviceBondProvider;
    List<EntityPagePattern> pagePatterns;
    List<SpecPattern> specPatterns;
    List<PPIconable> ppis = new ArrayList<PPIconable>();
    List<SPIconable> spis = new ArrayList<SPIconable>();
    List<Iconable> iconables = new ArrayList<Iconable>();
    List<Color> colors = new ArrayList<Color>();
    //
    DefaultComboBoxModel<DeviceBond> deviceBondComboModel = new DefaultComboBoxModel<DeviceBond>();
    final JComboBox<DeviceBond> deviceBondCombo = new JComboBox<DeviceBond>(deviceBondComboModel);
    //
    GenericSelectionPanel<Iconable> selectionPanel;
    //
    DeviceBond initDeviceBond;
    boolean showAll = true;
    JToggleButton viewTotalButton;

    public SetConnectionInTablePanel(DeviceType deviceType, DeviceBondProvider deviceBondProvider, List<EntityPagePattern> pagePatterns, List<SpecPattern> specPatterns, DeviceBond deviceBond) {
        this.deviceType = deviceType;
        this.deviceBondProvider = deviceBondProvider;
        this.pagePatterns = pagePatterns;
        this.specPatterns = specPatterns;
        this.initDeviceBond = deviceBond;
        initComponents();
    }

    private void initComponents() {
        RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(deviceType);
        List<DeviceBond> deviceBonds = routinePattern.getDeviceBonds();
        for (DeviceBond deviceBond : deviceBonds) {
            deviceBondComboModel.addElement(deviceBond);
        }

        for (EntityPagePattern pattern : pagePatterns) {
            PPIconable pi = new PPIconable();
            pi.pattern = pattern;

            DeviceBond bond = deviceBondProvider.getDeviceBond(pattern);
            if (bond == null) {
                pi.icon = TreeVarSelector.pageIcon;
            } else {
                pi.icon = TreeVarSelector.pageValidIcon;
            }
            ppis.add(pi);
        }
        for (SpecPattern pattern : specPatterns) {
            SPIconable pi = new SPIconable();
            pi.pattern = pattern;

            DeviceBond bond = deviceBondProvider.getDeviceBond(pattern);
            if (bond == null) {
                pi.icon = TreeVarSelector.specIcon;
            } else {
                pi.icon = TreeVarSelector.specValidIcon;
            }
            spis.add(pi);
        }
        selectionPanel = new GenericSelectionPanel<Iconable>(iconables, "Pattern", "Patterns", false);


        ColoredIconableCellRenderer cellRenderer = new ColoredIconableCellRenderer(this);
        ETable table = selectionPanel.getTable();
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(1).setCellRenderer(cellRenderer);

        deviceBondCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    onDeviceBondSelected((DeviceBond) deviceBondCombo.getSelectedItem());
                }
            }
        });

        final Action viewTotalAction = new AbstractAction("") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                showAll = !showAll;
                viewTotalButton.setSelected(showAll);
                onDeviceBondSelected((DeviceBond) deviceBondCombo.getSelectedItem());
            }
        };

        viewTotalButton = new JToggleButton(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/all.png")));
        viewTotalButton.setAction(viewTotalAction);
        viewTotalButton.setSelected(showAll);
        viewTotalButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/all.png")));
        viewTotalButton.setText("");
        viewTotalAction.putValue(Action.SHORT_DESCRIPTION, "Hide/Show Pattern Specified");
//        JPanel toolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.LEADING, new JLabel("Applied to: "), viewTotalButton);

        JToolBar toolBar = selectionPanel.getToolBar();
        toolBar.addSeparator();
        toolBar.add(viewTotalButton);


        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Connection: "), BorderLayout.WEST);
        topPanel.add(deviceBondCombo, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
//        centerPanel.add(new JLabel("Applied to: "), BorderLayout.NORTH);
//        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        centerPanel.add(selectionPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout(5, 5));
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        if (initDeviceBond != null) {
            deviceBondCombo.setSelectedItem(initDeviceBond);
        }
        onDeviceBondSelected((DeviceBond) deviceBondCombo.getSelectedItem());
        setPreferredSize(MeaOptions.COMPONENT_SLIM_SIZE);
    }

    public void onDeviceBondSelected(DeviceBond deviceBond) {
        iconables.clear();
        colors.clear();
        for (PPIconable pi : ppis) {
            DeviceBond bond = deviceBondProvider.getDeviceBond(pi.pattern);
            if (bond == null) {
                pi.icon = TreeVarSelector.pageIcon;
            } else {
                pi.icon = TreeVarSelector.pageValidIcon;
            }
            if (showAll || bond != null) {
                iconables.add(pi);
                // if pattern's current bond is selected bond, make it blue
                if (bond == deviceBond) {
                    colors.add(Color.blue);
                } else {
                    colors.add(null);
                }

            }
        }
        for (SPIconable pi : spis) {
            DeviceBond bond = deviceBondProvider.getDeviceBond(pi.pattern);
            if (bond == null) {
                pi.icon = TreeVarSelector.specIcon;
            } else {
                pi.icon = TreeVarSelector.specValidIcon;
            }
            if (showAll || bond != null) {
                iconables.add(pi);
                // if pattern's current bond is selected bond, make it blue
                if (bond == deviceBond) {
                    colors.add(Color.blue);
                } else {
                    colors.add(null);
                }

            }
        }
        selectionPanel.deselectAll();
    }

    public void ok() {
        DeviceBond deviceBond = (DeviceBond) deviceBondCombo.getSelectedItem();
        if (deviceBond != null) {
            List selected = selectionPanel.getSelected();
            for (Object object : selected) {
                if (object instanceof PPIconable) {
                    PPIconable pi = (PPIconable) object;
                    deviceBondProvider.setDeviceBond(pi.pattern, deviceBond);
                } else if (object instanceof SPIconable) {
                    SPIconable pi = (SPIconable) object;
                    deviceBondProvider.setDeviceBond(pi.pattern, deviceBond);
                }
            }
        }
    }

    @Override
    public Color getForeground(int row, int column) {
        if (row < colors.size()) {
            return colors.get(row);
        }

        return null;
    }

    @Override
    public Color getBackground(int row, int column) {
        return null;
    }

    public class PPIconable implements Iconable {

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
    }

    public class SPIconable implements Iconable {

        SpecPattern pattern;
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
    }
}
