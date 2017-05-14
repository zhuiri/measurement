/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceSelector;
import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.MeaData;
import com.platformda.iv.MeaOptions;
import com.platformda.spec.Spec;
import com.platformda.spec.SpecData;
import com.platformda.spec.SpecPattern;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.tree.CheckableTree;
import com.platformda.utility.tree.CheckableTreeModel;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.tree.CheckableTreeNodeChecker;
import com.platformda.utility.ui.JTableUtil;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.swing.etable.ETable;

/**
 *
 * @author Junyi
 */
public class SelectDUTPanel extends JPanel implements DeviceSelector, CheckableTreeNodeChecker {

    boolean multipleDeviceEnabled = false;
    MeaData meaData;
    List<DUTPack> packs = new ArrayList<DUTPack>();
    //
    Routine routine = null;
    DUTPack pack = null;
    MeaDeviceTableModel deviceTableModel;
    ETable deviceTable;
    protected CheckableTree pageTree;
    protected CheckableTreeModel pageTreeModel;
    protected CheckableTreeNode pageRootNode;
    SpecValueTableModel specTableModel;
    ETable specTable;
    //

    public SelectDUTPanel(MeaData meaData) {
        this.meaData = meaData;
        initComponents();
    }

    private void initComponents() {
        deviceTableModel = new MeaDeviceTableModel(meaData);
        deviceTable = new ETable(deviceTableModel);
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isMetaDown()) {
                    if (deviceTable.getSelectedRowCount() == 1) {
                        JPopupMenu popupMenu = new JPopupMenu();
//                        popupMenu.add(measureSpecAction);
//                        popupMenu.show(specTable, e.getX(), e.getY());
                    }
                    return;
                }
                deviceHighlighted();
            }
        });
        deviceTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                deviceHighlighted();
            }
        });
        JScrollPane deviceScroll = new JScrollPane(deviceTable);
        JPanel deviceWrapPanel = new JPanel(new BorderLayout());
//        deviceWrapPanel.add(deviceToolbarPanel, BorderLayout.NORTH);
        deviceWrapPanel.add(deviceScroll, BorderLayout.CENTER);

        pageTree = new CheckableTree();
        pageRootNode = new CheckableTreeNode("Page");
        pageTreeModel = new CheckableTreeModel(pageTree, pageRootNode);
        pageTree.setModel(pageTreeModel);
        pageTree.setToggleClickCount(2);
        pageTree.setRootVisible(false);
        pageTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        pageTreeModel.setTreeNodeCheckor(this);
        JScrollPane pageScroll = new JScrollPane(pageTree);
        JPanel pageWrapPanel = new JPanel(new BorderLayout());
//        pageWrapPanel.add(pageToolbarPanel, BorderLayout.NORTH);
        pageWrapPanel.add(pageScroll, BorderLayout.CENTER);

        specTableModel = new SpecValueTableModel();
        specTable = new ETable(specTableModel);
        JScrollPane specScroll = new JScrollPane(specTable);
        JPanel specWrapPanel = new JPanel(new BorderLayout());
//        specWrapPanel.add(specToolbarPanel, BorderLayout.NORTH);
        specWrapPanel.add(specScroll, BorderLayout.CENTER);

        boolean hasNoPage = true;
        boolean hasNoSpec = true;
        List<EntityDevice> devices = meaData.getDevices();
        for (EntityDevice device : devices) {
            Routine routine = meaData.getRoutineByDevice(device);
            if (!routine.getPagePatterns().isEmpty()) {
                hasNoPage = false;
            }
            if (!routine.getSpecPatterns().isEmpty()) {
                hasNoSpec = false;
            }
            DUTPack pack = new DUTPack(routine);
            packs.add(pack);
        }

        JPanel routineResultPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        if (!hasNoPage) {
            routineResultPanel.add(pageWrapPanel);
        }
        if (!hasNoSpec) {
            routineResultPanel.add(specWrapPanel);
        }

        JSplitPane bottomSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplitPane.setTopComponent(deviceWrapPanel);
        bottomSplitPane.setBottomComponent(routineResultPanel);
        bottomSplitPane.setResizeWeight(0.32);
        bottomSplitPane.setBorder(null);

        setLayout(new BorderLayout());
        add(bottomSplitPane, BorderLayout.CENTER);


        if (!devices.isEmpty()) {
            JTableUtil.setSelectedIndex(deviceTable, deviceTableModel, 0);
            deviceHighlighted();
        }

        setPreferredSize(MeaOptions.COMPONENT_META_SLIM_SIZE);
    }

    public void deviceHighlighted() {
        int deviceIndex = deviceTable.getSelectedRow();
        deviceIndex = deviceTable.convertRowIndexToModel(deviceIndex);

        if (deviceIndex >= 0) {
            EntityDevice device = meaData.getDevice(deviceIndex);
            routine = meaData.getRoutineByDevice(device);
            pack = packs.get(deviceIndex);
        } else {
            routine = null;
            pack = null;
        }

        buildPageTree();
        pageTreeModel.reload(pageRootNode);
        pageTree.updateUI();

        specTableModel.fireTableDataChanged();
    }

    public void buildPageTree() {
        pageRootNode.removeAllChildren();
        if (routine != null) {
            List<EntityPagePattern> pagePatterns = routine.pagePatterns;
            for (EntityPagePattern pattern : pagePatterns) {
                CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
                patternNode.setIcon(MeaSpacePanel.pageIcon);
                patternNode.setCheckable(true);
                patternNode.setChecked(pack.isSelected(routine, pattern));
                pageRootNode.add(patternNode);
            }
        }
        updatePageTreeIcons();
    }

    public void updatePageTreeIcons() {
        int number = pageRootNode.getChildCount();
        for (int i = 0; i < number; i++) {
            CheckableTreeNode node = (CheckableTreeNode) pageRootNode.getChildAt(i);
            EntityPagePattern pattern = (EntityPagePattern) node.getAssociatedObject();
            if (meaData.hasPage(routine.getDevice(), pattern)) {
                node.setIcon(MeaSpacePanel.pageMeaedIcon);
            } else {
                node.setIcon(MeaSpacePanel.pageIcon);
            }
        }

        pageTree.repaint();
    }

    public DUTPack getPack() {
        return pack;
    }

    public DUTPacks getPacks() {
        return new DUTPacks(packs);
    }
    public static String[] specColumnNames = new String[]{"Selected", "Spec", "Value"};

    @Override
    public boolean isDeviceSelected(EntityDevice device) {

        return false;
    }

    @Override
    public void setDeviceSelected(EntityDevice ed, boolean selected) {
    }

    @Override
    public void treeNodeChecked(CheckableTreeNode treeNode, boolean bool) {
        Object obj = treeNode.getAssociatedObject();

        if (obj != null && obj instanceof EntityPagePattern) {
            EntityPagePattern pagePattern = (EntityPagePattern) obj;
            if (bool) {
                pack.addPagePattern(pagePattern);
            } else {
                pack.removePagePattern(pagePattern);
            }
        }
    }

    final class SpecValueTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            if (routine == null) {
                return 0;
            }
            return routine.specPatterns.size();
        }

        @Override
        public int getColumnCount() {
            return specColumnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return specColumnNames[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            }

            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SpecPattern specPattern = routine.specPatterns.get(rowIndex);
            if (columnIndex == 0) {
                return pack.isSelected(routine, specPattern);
            }

            if (columnIndex == 1) {
                return specPattern.getName();
            }
            Spec spec = meaData.getSpecByName(specPattern.getName());
            SpecData specData = meaData.getSpecData(routine.getDevice(), spec);
            if (specData != null) {
//                    return specData.getValue();
                return StringUtil.getConciseString(specData.getValue());
            }
            return "";
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            SpecPattern specPattern = routine.specPatterns.get(rowIndex);
            if (columnIndex == 0) {
                boolean bool = ((Boolean) value).booleanValue();
                if (bool) {
                    pack.addSpecPattern(specPattern);
                } else {
                    pack.removeSpecPattern(specPattern);
                }
            }
        }
    }
}
