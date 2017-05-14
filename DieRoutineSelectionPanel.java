/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.MeaOptions;
import com.platformda.iv.MeaSpace;
import com.platformda.utility.Iconable;
import com.platformda.utility.provider.GenericSelector;
import com.platformda.utility.table.IconableCellRenderer;
import com.platformda.utility.ui.GUIUtil;
import com.platformda.utility.ui.JTableUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumnModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Junyi
 */
public class DieRoutineSelectionPanel extends JPanel implements GenericSelector<RoutineTuple>, DieHighlighter {

    boolean dieInGeometryView = true; // in table or in geometry
    MeaSpace meaSpace;
    //
    List<WaferDieInfo> dieInfos = new ArrayList<WaferDieInfo>();
    List<WaferDieInfo> selectedDieInfos = new ArrayList<WaferDieInfo>();
    List<RoutineTuple> routineTuples = new ArrayList<RoutineTuple>();
    List<RoutineTuple> selectedRoutineTuples = new ArrayList<RoutineTuple>();
    StatusProvider statusProvider;
    //
    // die in table
    DieTableModel dieTableModel;
    ETable dieTable;
    // die in geometry
    DieGeometrySelectionPanel dieGeometrySelectionPanel;
    //
    RoutineTupleTableModel routineTableModel;
    ETable routineTable;

    public DieRoutineSelectionPanel(MeaSpace meaSpace) {
        this(meaSpace, meaSpace.getWaferInfo().getDieInfos(), meaSpace.getMeaData().getRoutineTuples());
    }

    public DieRoutineSelectionPanel(MeaSpace meaSpace, List<WaferDieInfo> dieinfos, List<RoutineTuple> rts) {
        this.meaSpace = meaSpace;
        dieInfos.addAll(dieinfos);
        Collections.sort(dieInfos);
        Collections.reverse(dieInfos);

        routineTuples.addAll(rts);
        selectedRoutineTuples.addAll(rts);
        initComponents();
    }

    public void selectDie(MeaPlan plan, WaferDieInfo dieInfo) {
        selectedDieInfos.clear();
        selectedDieInfos.add(dieInfo);
        if (dieInGeometryView) {
            dieGeometrySelectionPanel.selectedDiesUpdated(selectedDieInfos);
            dieGeometrySelectionPanel.selectDieInfo(dieInfo);
        } else {
            dieTableModel.fireTableDataChanged();
            int index = dieInfos.indexOf(dieInfo);
            if (index >= 0) {
                JTableUtil.setSelectedIndex(dieTable, dieTableModel, index);
                dieTable.scrollRectToVisible(dieTable.getCellRect(index, 0, true));
            }
        }
    }

    public void update(List<WaferDieInfo> ds, List<RoutineTuple> rts, List<WaferDieInfo> sds, List<RoutineTuple> srts, StatusProvider statusProvider) {
        dieInfos.clear();
        dieInfos.addAll(ds);
        Collections.sort(dieInfos);
        Collections.reverse(dieInfos);

        routineTuples.clear();
        routineTuples.addAll(rts);

        selectedDieInfos.clear();
        selectedDieInfos.addAll(sds);

        selectedRoutineTuples.clear();
        selectedRoutineTuples.addAll(srts);

        this.statusProvider = statusProvider;
        if (dieInGeometryView) {
            dieGeometrySelectionPanel.update(ds, sds, statusProvider);
        } else {
            dieTableModel.setStatusProvider(statusProvider);
            dieTableModel.fireTableDataChanged();
        }
        dieHighlighted();
    }

    public void updateSelectedDies(List<WaferDieInfo> sds) {
        selectedDieInfos.clear();
        selectedDieInfos.addAll(sds);

        if (dieInGeometryView) {
            dieGeometrySelectionPanel.selectedDiesUpdated(sds);
        } else {
            dieTableModel.fireTableDataChanged();
            if (!sds.isEmpty()) {
                int index = dieInfos.indexOf(sds.get(0));
                if (index >= 0) {
                    JTableUtil.setSelectedIndex(dieTable, dieTableModel, index);
                    dieTable.scrollRectToVisible(dieTable.getCellRect(index, 0, true));
                }
            }
        }
    }

    private JComponent initDiePanel() {
        if (dieInGeometryView) {
            dieGeometrySelectionPanel = new DieGeometrySelectionPanel(this, meaSpace.getWaferInfo(), dieInfos, selectedDieInfos, statusProvider);
            return dieGeometrySelectionPanel;
        } else {
            dieTableModel = new DieTableModel(dieInfos, selectedDieInfos);
            dieTable = new ETable(dieTableModel);
            dieTable.setDefaultRenderer(Iconable.class, new IconableCellRenderer());

            final JButton selDieByGeometryButton = new JButton("");
            selDieByGeometryButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                    "com/platformda/mea/resources/geometry.png")));
            selDieByGeometryButton.setToolTipText("Select die by geometry");
            selDieByGeometryButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<WaferDieInfo> copySelectedDies = new ArrayList<WaferDieInfo>();
                    copySelectedDies.addAll(selectedDieInfos);

                    DieGeometrySelectionPanel form = new DieGeometrySelectionPanel(null, meaSpace.getWaferInfo(), dieInfos, copySelectedDies, statusProvider);
                    form.setPreferredSize(new Dimension(880, 600));
                    DialogDescriptor desc = new DialogDescriptor(form,
                            "Select Die By Geometry", true, DialogDescriptor.OK_CANCEL_OPTION,
                            DialogDescriptor.OK_OPTION, null);
                    desc.setValid(true);
                    Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
                    if (result == NotifyDescriptor.OK_OPTION) {
//                        List<WaferDieInfo> selecteds = form.getSelectedDieInfos();
                        selectedDieInfos.clear();
                        selectedDieInfos.addAll(copySelectedDies);
                        dieTableModel.fireTableDataChanged();
                    }
                }
            });

            dieTable.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isMetaDown()) {
                        return;
                    }
                    dieHighlighted();
                }
            });
            dieTable.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    dieHighlighted();
                }
            });

            GUIUtil.GenericTableSelectionContext<WaferDieInfo> dieContext = new GUIUtil.GenericTableSelectionContext<WaferDieInfo>(dieInfos, selectedDieInfos, dieTable, dieTableModel);
            final JToolBar dieToolBar = GUIUtil.createTableSelectionToolBar(dieTable, dieContext, new JLabel("Select Dies: "));
            dieToolBar.addSeparator();
            dieToolBar.add(selDieByGeometryButton);

            JPanel dieTablePanel = new JPanel(new BorderLayout());
            JScrollPane dieTableScroll = new JScrollPane(dieTable);
            dieTablePanel.add(dieTableScroll, BorderLayout.CENTER);
            dieTablePanel.add(dieToolBar, BorderLayout.NORTH);

            return dieTablePanel;
        }
    }

    private void initComponents() {
        JComponent diePanel = initDiePanel();

        routineTableModel = new RoutineTupleTableModel(meaSpace.getMeaData(), routineTuples, this);
        routineTable = new ETable(routineTableModel);
        routineTable.setDefaultRenderer(Iconable.class, new IconableCellRenderer());
        JScrollPane tableScroll = new JScrollPane(routineTable);

        ETableColumnModel tcm = (ETableColumnModel) routineTable.getColumnModel();
        tcm.getColumn(0).setPreferredWidth(45);
        tcm.getColumn(1).setPreferredWidth(45);
        tcm.getColumn(2).setPreferredWidth(45);
        tcm.getColumn(3).setPreferredWidth(135);
        tcm.getColumn(4).setPreferredWidth(30);
        tcm.getColumn(5).setPreferredWidth(45);

        final JButton selRoutineInOutlineButton = new JButton("");
        selRoutineInOutlineButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/callTreeTab.png")));
        selRoutineInOutlineButton.setToolTipText("Select in tree");
        selRoutineInOutlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RoutineOutlineSelectionPanel form = new RoutineOutlineSelectionPanel(routineTuples, selectedRoutineTuples);
//                form.setPreferredSize(new Dimension(880, 600));
                DialogDescriptor desc = new DialogDescriptor(form,
                        "Select Routines In Tree", true, DialogDescriptor.OK_CANCEL_OPTION,
                        DialogDescriptor.OK_OPTION, null);
                desc.setValid(true);
                Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
                if (result == NotifyDescriptor.OK_OPTION) {
                    List<RoutineTuple> selecteds = form.getSelectedRoutineTuples();
                    selectedRoutineTuples.clear();
                    selectedRoutineTuples.addAll(selecteds);
                    routineTableModel.fireTableDataChanged();
                }
            }
        });

        GUIUtil.GenericTableSelectionContext<RoutineTuple> context = new GUIUtil.GenericTableSelectionContext<RoutineTuple>(routineTuples, selectedRoutineTuples, routineTable, routineTableModel);
        final JToolBar toolBar = GUIUtil.createTableSelectionToolBar(routineTable, context, new JLabel("Select Routines: "));
        toolBar.addSeparator();
        toolBar.add(selRoutineInOutlineButton);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(tableScroll, BorderLayout.CENTER);
        tablePanel.add(toolBar, BorderLayout.NORTH);

//            setLayout(new BorderLayout());
//            add(treeScroll, BorderLayout.CENTER);

        setLayout(new GridLayout(2, 1, 2, 2));
        add(diePanel);
        add(tablePanel);

        setPreferredSize(MeaOptions.COMPONENT_META_WIDE_SIZE);
    }

    public void dieHighlighted() {
        if (dieInGeometryView) {
            routineTableModel.fireTableDataChanged();
        } else {
            int index = dieTable.getSelectedRow();
            index = dieTable.convertRowIndexToModel(index);
            if (index >= 0) {
                WaferDieInfo dieInfo = dieInfos.get(index);
                routineTableModel.setDieInfo(dieInfo);
            } else {
                routineTableModel.setDieInfo(null);
            }
            routineTableModel.fireTableDataChanged();
        }
    }

    @Override
    public void dieHighlighted(WaferDieInfo dieInfo) {
        routineTableModel.setDieInfo(dieInfo);
        routineTableModel.fireTableDataChanged();
    }

    public void setSelection(List<WaferDieInfo> dies, List<RoutineTuple> rts) {
        selectedDieInfos.clear();
        selectedDieInfos.addAll(dies);
        selectedRoutineTuples.clear();
        selectedRoutineTuples.addAll(rts);

        if (dieInGeometryView) {
            dieGeometrySelectionPanel.selectedDiesUpdated(selectedDieInfos);
        } else {
            dieTableModel.fireTableDataChanged();
        }
        routineTableModel.fireTableDataChanged();
    }

    @Override
    public boolean isSelected(RoutineTuple t) {
        return selectedRoutineTuples.contains(t);
    }

    @Override
    public void setSelected(RoutineTuple t, boolean selected) {
        if (selected) {
            selectedRoutineTuples.add(t);
        } else {
            selectedRoutineTuples.remove(t);
        }
    }

    public List<WaferDieInfo> getSelectedDies() {
//        final List<WaferDieInfo> selected = new ArrayList<WaferDieInfo>();
//        CheckableTreeUtil.fetchChecked(tree, true, true, false, selected);
//        return selected;

        Collections.sort(selectedDieInfos);
        Collections.reverse(selectedDieInfos);

        return selectedDieInfos;
    }

    public List<RoutineTuple> getSelectedRoutineTuples() {
        return selectedRoutineTuples;
    }
}
