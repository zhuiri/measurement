/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceTagImpl;
import com.platformda.datacore.DeviceType;
import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.datacore.pattern.EntityPagePatternGroup;
import com.platformda.datacore.pattern.PagePatternLoader;
import com.platformda.iv.MeaDAO;
import com.platformda.iv.MeaData;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.MeaSpace;
//import com.platformda.iv.measure.StressPattern;
import com.platformda.iv.api.MeaEditor;
import com.platformda.iv.measure.AnalysisUtil;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.iv.measure.MeaDevice;
import com.platformda.iv.measure.MeaDeviceGroup;
import com.platformda.iv.measure.MeaDie;
import com.platformda.iv.measure.MeaSubDie;
import com.platformda.spec.Spec;
import com.platformda.spec.SpecPattern;
import com.platformda.spec.SpecPatternGroup;
import com.platformda.spec.SpecPatternLoader;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.tree.CheckableTree;
import com.platformda.utility.tree.CheckableTreeModel;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.tree.CheckableTreeNodeChecker;
import com.platformda.utility.tree.CheckableTreeUtil;
import com.platformda.utility.ui.GUIUtil;
import com.platformda.utility.ui.JTableUtil;
import com.platformda.utility.ui.JTreeUtil;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.swing.etable.ETable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Junyi
 */
public class DeviceGroupPanel extends JPanel implements ActionListener, MeaDeviceTableModel.MeaDeviceUpdater, MeaEditor {

    MeaSpace meaSpace;
    MeaData meaData;
    MeaDie die;
    MeaSubDie subDie;
    MeaDeviceGroup deviceGroup;
    MeaDeviceTableModel deviceTableModel;
    ETable deviceTable;
    //
    DefaultListModel<Routine> routineListModel;
    JList<Routine> routineList;
    //
    Routine selectedRoutine;
    JPanel bottomTotalPanel;
    StressPatternPanel stressPatternPanel;
    //
    IndPatternEditPanel patternPanel;
    //
    protected CheckableTree routineTree;
    protected CheckableTreeModel routineTreeModel;
    protected CheckableTreeNode routineRootNode;
    //
    JButton addDeviceButton = new JButton("Add");
    JButton removeDeviceButton = new JButton("Remove");
    JButton addInstanceButton = new JButton("Add Instance");
    JButton removeInstanceButton = new JButton("Remove Instance");
    //
    JMenuItem editItem = new JMenuItem("Edit...");
    JMenuItem setConnectionItem = new JMenuItem("Set Connection...");
    JMenuItem removeItem = new JMenuItem("Remove...");
    JMenuItem copyItem = new JMenuItem("Copy...");
    //
    CheckableTreeNode pageNode = new CheckableTreeNode("Page");
    CheckableTreeNode specNode = new CheckableTreeNode("Spec");
    Action addRoutineAction = new AbstractAction("Add Routine...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            addRoutine();
        }
    };
    Action removeRoutineAction = new AbstractAction("Remove Routine...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = routineList.getSelectedIndex();
            if (index >= 0) {
                String msg = "Do you want to remove selected rouine?";
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove Routine", NotifyDescriptor.YES_NO_OPTION);
                Object result = DialogDisplayer.getDefault().notify(nd);
                if (result != NotifyDescriptor.YES_OPTION) {
                    return;
                }
                Routine routine = routineListModel.get(index);
                deviceGroup.removeRoutine(routine);
                routineListModel.remove(index);
                routineList.updateUI();
                routineList.setSelectedIndex(index - 1);
            }
        }
    };
    Action copyRoutineAction = new AbstractAction("Copy...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = routineList.getSelectedIndex();
            if (index >= 0) {
                Routine routine = routineListModel.get(index);
                copyRoutine(routine);
            }
        }
    };
    Action addPatternAction = new AbstractAction("Add Page/Spec...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = routineList.getSelectedIndex();
            if (index >= 0) {
                Routine routine = routineListModel.get(index);
                addToRoutine(routine);
            }
        }
    };
    Action loadPagePatternAction = new AbstractAction("Load Page Pattern...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = routineList.getSelectedIndex();
            if (index >= 0) {
                Routine routine = routineListModel.get(index);
                DeviceType deviceType = (DeviceType) routine.getDeviceType();
                String defaultPath = null;
                File defaultSettingsFile = InstalledFileLocator.getDefault().locate("etc", MeaSpace.CODE_BASE, false);
                if (defaultSettingsFile != null) {
                    defaultPath = defaultSettingsFile.getAbsolutePath();
                }
                File file = LoadSaveUtil.openFile("Load Page Pattern", JFileChooser.FILES_ONLY, IRPanel.KEY_PATTERN_PAGE, defaultPath, false);
                if (file != null) {
                    EntityPagePatternGroup patternGroup = null;
                    try {
                        PagePatternLoader loader = new PagePatternLoader();
                        DeviceTag dt = new DeviceTagImpl(deviceType, null);

                        patternGroup = loader.load(file.getAbsolutePath(), dt);
                        List<EntityPagePattern> patterns = patternGroup.getPatterns();
                        for (EntityPagePattern pattern : patterns) {
                            if (pattern.getDeviceType() == deviceType) {
                                EntityPagePattern existing = routine.getPagePattern(pattern.getName());
                                if (existing != null) {
                                    MeaDAO dao = meaSpace.getDao();
                                    selectedRoutine.updatePagePattern(dao, existing);
                                    existing.copyValueOf(pattern);
                                } else {
                                    routine.pagePatterns.add(pattern);
                                }
                            }
                        }
                        routine.updateVars(patternGroup);
                        routineSelected(routine);
                    } catch (Exception ex) {
                        StatusDisplayer.getDefault().setStatusText("Failed to load page patterns.");
                    }
                }
            }
        }
    };
    Action loadSpecPatternAction = new AbstractAction("Load Spec Pattern...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = routineList.getSelectedIndex();
            if (index >= 0) {
                Routine routine = routineListModel.get(index);
                DeviceType deviceType = routine.getDeviceType();
                String defaultPath = null;
                File defaultSettingsFile = InstalledFileLocator.getDefault().locate("etc", MeaSpace.CODE_BASE, false);
                if (defaultSettingsFile != null) {
                    defaultPath = defaultSettingsFile.getAbsolutePath();
                }
                File file = LoadSaveUtil.openFile("Load Spec Pattern", JFileChooser.FILES_ONLY, IRPanel.KEY_PATTERN_SPEC, defaultPath, false);
                if (file != null) {
                    SpecPatternGroup patternGroup = null;
                    try {
                        patternGroup = SpecPatternLoader.load(deviceType, file.getAbsolutePath());
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        StatusDisplayer.getDefault().setStatusText("Failed to load spec patterns.");
                    }
                    if (patternGroup == null) {
                        return;
                    }
                    List<SpecPattern> patterns = patternGroup.getPatterns();
                    List<String> vars = new ArrayList();
                    for (SpecPattern pattern : patterns) {
                        if (pattern.getDeviceType() == deviceType) {
                            SpecPattern existing = routine.getSpecPatternByName(pattern.getName());
                            if (existing != null) {
                                MeaDAO dao = meaSpace.getDao();
                                MeaData meaData = meaSpace.getMeaData();
                                Spec spec = meaData.getSpecByName(pattern.getName());
                                if (spec != null) {
                                    selectedRoutine.removeSpec(dao, null);
                                }
                                routine.specPatterns.remove(existing);
                                routine.specPatterns.add(pattern);
                            } else {
                                routine.specPatterns.add(pattern);
                            }
                        }
                    }
                    routine.updateVars(patternGroup);
                    routineSelected(routine);
                }
            }
        }
    };
    Action editStressAction = new AbstractAction("Edit Stress...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int index = routineList.getSelectedIndex();
            if (index >= 0) {
                Routine routine = routineListModel.get(index);
                StressPattern stressPattern = routine.getStressPattern();
                if (stressPattern != null) {
                    StressPatternPanel form = new StressPatternPanel(routine, stressPattern);
                    DialogDescriptor desc = new DialogDescriptor(form,
                            "Edit Stress", true, DialogDescriptor.OK_CANCEL_OPTION,
                            DialogDescriptor.OK_OPTION, null);
                    Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
                    if (result == NotifyDescriptor.OK_OPTION) {
                        form.stopEditing();
                        // remove results
                        routine.clearData(meaSpace.getDao());
                        routine.resetStress();
                    }
                }
            }
        }
    };
    Action editPatternAction = new AbstractAction("Edit...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = routineTree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof IndPattern) {
                    IndPattern pagePattern = (IndPattern) obj;
                    editPattern(treeNode, pagePattern);
                }
            }
        }
    };
    Action removePatternAction = new AbstractAction("Remove...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = routineTree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof IndPagePattern) {
                    IndPagePattern ipp = (IndPagePattern) obj;
                    String msg = "Do you want to remove selected page?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove Page", NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        return;
                    }

                    patternPanel.setEmpty();
                    MeaDAO dao = meaSpace.getDao();
                    EntityPagePattern pagePattern = ipp.getPattern();
                    selectedRoutine.removePagePattern(dao, pagePattern);

//                    treeNode.removeFromParent();
//                    routineTreeModel.reload();
//                    JTreeUtil.expandTree(routineTree);
                    routineSelected(selectedRoutine);
                } else if (obj != null && obj instanceof SpecPattern) {
                    IndSpecPattern isp = (IndSpecPattern) obj;
                    SpecPattern specPattern = (SpecPattern) obj;
                    String msg = "Do you want to remove selected spec?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove Spec", NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                    patternPanel.setEmpty();
                    List<SpecPattern> removes = new ArrayList<SpecPattern>();
                    removes.add(specPattern);
                    List<SpecPattern> specPatterns = selectedRoutine.getSpecPatterns();
                    for (SpecPattern sp : specPatterns) {
                        if (sp != specPattern) {
                            List<SpecPattern> allDepends = sp.getAllDepends();
                            if (allDepends.contains(specPattern)) {
                                removes.add(sp);
                            }
                        }
                    }

                    MeaDAO dao = meaSpace.getDao();
                    MeaData meaData = meaSpace.getMeaData();
                    for (SpecPattern sp : removes) {
                        Spec spec = meaData.getSpecByName(sp.getName());
                        if (spec != null) {
                            selectedRoutine.removeSpec(dao, spec);
                        }
                    }
                    selectedRoutine.removeIndSpecPattern(isp);
                    routineSelected(selectedRoutine);
                }
            }
        }
    };

    public DeviceGroupPanel(MeaSpace meaSpace, MeaDie die, MeaSubDie subDie, MeaDeviceGroup deviceGroup) {
        this.meaSpace = meaSpace;
        this.meaData = meaSpace.getMeaData();
        this.die = die;
        this.subDie = subDie;
        this.deviceGroup = deviceGroup;
        initComponents();
    }

    private void initComponents() {
        deviceTableModel = new MeaDeviceTableModel(deviceGroup, this);
        deviceTable = new ETable(deviceTableModel);
        JScrollPane deviceTableScroll = new JScrollPane(deviceTable);
        JPanel deviceButtonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        deviceButtonPanel.add(addDeviceButton);
        deviceButtonPanel.add(removeDeviceButton);
        deviceButtonPanel.add(addInstanceButton);
        deviceButtonPanel.add(removeInstanceButton);
        JPanel deviceTablePanel = new JPanel(new BorderLayout());
        deviceTablePanel.add(deviceTableScroll, BorderLayout.CENTER);
        deviceTablePanel.add(deviceButtonPanel, BorderLayout.SOUTH);
        addDeviceButton.addActionListener(this);
        removeDeviceButton.addActionListener(this);
        addInstanceButton.addActionListener(this);
        removeInstanceButton.addActionListener(this);

        List<Routine> routines = deviceGroup.getRoutines();
        routineListModel = new DefaultListModel<Routine>();
        for (Routine routine : routines) {
            routineListModel.addElement(routine);
        }
        routineList = new JList<Routine>(routineListModel);
        JScrollPane routineListScroll = new JScrollPane(routineList);
        routineList.setVisibleRowCount(4);
        routineList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    routineSelected(routineList.getSelectedValue());
                }
            }
        });
        MouseListener routineListMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.isMetaDown()) {
                    if (e.getClickCount() == 1) {
                        // TODO:
                        int index = routineList.getSelectedIndex();
                        if (index >= 0) {
                            Routine routine = routineListModel.get(index);
                            JPopupMenu popupMenu = new JPopupMenu();
                            popupMenu.add(addPatternAction);
//                            popupMenu.add(loadPagePatternAction);
//                            popupMenu.add(loadSpecPatternAction);
//                            popupMenu.add(setConnectionAction);
                            popupMenu.add(copyRoutineAction);
//                            if (routine.getStressPattern() != null) {
//                                popupMenu.addSeparator();
//                                popupMenu.add(editStressAction);
//                            }
                            popupMenu.show(routineList, e.getX(), e.getY());
                        }
                    }
                }
            }
        };
        routineList.addMouseListener(routineListMouseListener);

        JButton addButton = new JButton();
        addButton.setAction(addRoutineAction);
        addButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/add.gif")));
        addButton.setText("");
        addRoutineAction.putValue(Action.SHORT_DESCRIPTION, "Add Routine...");
        JButton removeButton = new JButton();
        removeButton.setAction(removeRoutineAction);
        removeButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/remove.gif")));
        removeButton.setText("");
        removeRoutineAction.putValue(Action.SHORT_DESCRIPTION, "Remove Routine...");
        JToolBar toolbar = new JToolBar();
        toolbar.add(addButton);
        toolbar.add(removeButton);
        toolbar.setFloatable(false);
        toolbar.setOrientation(SwingConstants.VERTICAL);
        JPanel routineListPanel = new JPanel(new BorderLayout());
        routineListPanel.add(toolbar, BorderLayout.WEST);
        routineListPanel.add(routineListScroll, BorderLayout.CENTER);

        routineTree = new CheckableTree();
        routineRootNode = new CheckableTreeNode("Routine");
        routineTreeModel = new CheckableTreeModel(routineTree, routineRootNode);
        routineTree.setModel(routineTreeModel);
        routineTree.setToggleClickCount(2);
        routineTree.setRootVisible(false);
        routineTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane treeScroll = new JScrollPane(routineTree);
        routineTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (e.getNewLeadSelectionPath() != null) {
                    updateContent(e.getPath().getLastPathComponent());
                }
            }
        });
        setConnectionItem.addActionListener(this);
        copyItem.addActionListener(this);
        MouseListener routineTreeMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath treePath = routineTree.getPathForLocation(e.getX(), e.getY());
                if (treePath == null) {
                    return;
                }
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (e.isMetaDown()) {
                    if (treeNode == routineRootNode) {
                        JPopupMenu popupMenu = new JPopupMenu();
//                        popupMenu.add(clearAllDieDataAction);
                        popupMenu.show(routineTree, e.getX(), e.getY());
                    } else if (obj != null && obj instanceof IndPattern) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.add(editPatternAction);
                        popupMenu.add(setConnectionItem);
                        popupMenu.addSeparator();
                        popupMenu.add(removePatternAction);
                        popupMenu.add(copyItem);
                        popupMenu.show(routineTree, e.getX(), e.getY());
                    }
                } else if (e.getClickCount() == 2 && obj != null && obj instanceof IndPattern) {
                    IndPattern pattern = (IndPattern) obj;
                    editPattern(treeNode, pattern);
                }
            }
        };
        routineTree.addMouseListener(routineTreeMouseListener);
        patternPanel = new IndPatternEditPanel(null, false, true);

        JPanel bottomLeftPanel = new JPanel(new BorderLayout(5, 5));
        bottomLeftPanel.add(routineListPanel, BorderLayout.NORTH);
        bottomLeftPanel.add(treeScroll, BorderLayout.CENTER);

//        JPanel bottomPanel = new JPanel(new GridLayout(0, 2, 2, 2));
//        bottomPanel.add(bottomLeftPanel);
//        bottomPanel.add(patternPanel);

        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        bottomSplit.setBorder(null);
        bottomSplit.setLeftComponent(bottomLeftPanel);
        bottomSplit.setRightComponent(patternPanel);
        bottomSplit.setDividerLocation(252);
        bottomSplit.setResizeWeight(0.24);

        bottomTotalPanel = new JPanel(new BorderLayout());
        bottomTotalPanel.add(bottomSplit, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        JTableUtil.setVisibleRowCount(deviceTable, 8);
//        add(tablePanel, BorderLayout.NORTH);
//        add(bottomPanel, BorderLayout.CENTER);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setLeftComponent(deviceTablePanel);
        splitPane.setRightComponent(bottomTotalPanel);
        splitPane.setDividerLocation(0.24);
        splitPane.setResizeWeight(0.24);
        add(splitPane, BorderLayout.CENTER);
//        setLayout(new GridLayout(0, 1, 2, 2));
//        add(tablePanel);
//        add(bottomPanel);

        if (!routineListModel.isEmpty()) {
            routineList.setSelectedIndex(0);
        }
    }

    public void addDevice() {
        int selectedIndex = deviceTable.getSelectedRow();
        int deviceIndex = deviceTable.convertRowIndexToModel(selectedIndex);

        MeaDevice meaDevice;
        if (deviceIndex >= 0) {
            try {
                meaDevice = (MeaDevice) deviceGroup.getDevice(deviceIndex).clone();
            } catch (CloneNotSupportedException ex) {
                // should never happen
                Exceptions.printStackTrace(ex);
                return;
            }
        } else {
            EntityDevice device = MeaDeviceTypeManager.getDefaultDevice(deviceGroup);
            meaDevice = new MeaDevice(device);
        }

        meaDevice.setSubDie(subDie);
        meaDevice.setDeviceGroup(deviceGroup);
        deviceGroup.addDevice(meaDevice);
        deviceTableModel.fireTableDataChanged();
        JTableUtil.setSelectedIndex(deviceTable, deviceTableModel, deviceGroup.getDeviceNumber() - 1);

//        DeviceEditor editor = device.getDeviceEditor();
//
//        Component editorComponent = editor.getEditorComponent();
//        editorComponent.setPreferredSize(new Dimension(440, 320));
//
//        DialogDescriptor desc = new DialogDescriptor(editorComponent,
//                "New Device", true, DialogDescriptor.OK_CANCEL_OPTION,
//                DialogDescriptor.OK_OPTION, null);
//
//        Object result = DialogDisplayer.getDefault().notify(desc);
//        if (result == NotifyDescriptor.OK_OPTION) {
//            editor.stopEditing();
//
//            MeaDevice meaDevice = new MeaDevice(device);
//            meaDevice.setSubDie(subDie);
//            meaDevice.setDeviceGroup(deviceGroup);
//            deviceGroup.addDevice(meaDevice);
//        }
    }

    public void addRoutine() {
        RoutineManager routineManager = RoutineManager.getInstance();
        List<Routine> routines = routineManager.getRoutines(deviceGroup.getDeviceType());
        if (routines == null || routines.isEmpty()) {
            return;
        }

//        DefaultComboBoxModel<Routine> comboModel = new DefaultComboBoxModel<Routine>(routines.toArray(new Routine[0]));
//        JComboBox<Routine> combo = new JComboBox<Routine>(comboModel);
//
//        JPanel form = new JPanel(new BorderLayout());
//        form.add(new JLabel("Routine: "), BorderLayout.WEST);
//        form.add(combo, BorderLayout.CENTER);
//        GUIUtil.setEmptyBorder(form, 5);
//
//        DialogDescriptor desc = new DialogDescriptor(form,
//                "Add Routine", true, DialogDescriptor.OK_CANCEL_OPTION,
//                DialogDescriptor.OK_OPTION, null);
//        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
//        if (result == NotifyDescriptor.OK_OPTION) {
//            Routine routine = (Routine) combo.getSelectedItem();

        SelectRoutinePanel form = new SelectRoutinePanel(routines);
        DialogDescriptor desc = new DialogDescriptor(form,
                "Add Routine", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            Routine routine = form.getSelectedRoutine();
            Routine added = new Routine(routine);
            deviceGroup.addRoutine(added);

            routineListModel.addElement(added);
            routineList.updateUI();
            routineList.setSelectedIndex(routineListModel.getSize() - 1);
        }
    }

    protected void addToRoutine(Routine routine) {
        RoutinePatternManager routinePatternManager = RoutinePatternManager.getInstance();
        RoutinePattern routinePattern = routinePatternManager.getRoutinePattern(routine.getDeviceType());
        AddPatternToRoutinePanel addPanel = new AddPatternToRoutinePanel(routinePattern, routine);
        DialogDescriptor desc = new DialogDescriptor(addPanel,
                "Add Page/Spec", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            addPanel.stopEditing();
            List<IndPagePattern> ipps = addPanel.getSelectedPagePatterns();
            for (IndPagePattern ipp : ipps) {
                routine.addIndPagePattern(ipp);
            }

            List<IndSpecPattern> isps = addPanel.getSelectedSpecPatterns();
            for (IndSpecPattern isp : isps) {
                routine.addIndSpecPattern(isp);
            }
            if (!ipps.isEmpty() || !isps.isEmpty()) {
                routineSelected(routine);
            }
        }
    }

    protected void addToRoutine0(Routine routine) {
        final CheckableTree tree = new CheckableTree();
        CheckableTreeModel treeModel;
        CheckableTreeNode rootNode;

        rootNode = new CheckableTreeNode("Root");
        treeModel = new CheckableTreeModel(tree, rootNode);
        tree.setModel(treeModel);
        tree.setToggleClickCount(2);
        tree.setRootVisible(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        treeModel.setTreeNodeCheckor(new CheckableTreeNodeChecker() {
            @Override
            public void treeNodeChecked(CheckableTreeNode node, boolean checked) {
                if (node.isLeaf()) {
                    Object obj = node.getAssociatedObject();
                    if (obj instanceof SpecPattern) {
                        SpecPattern specPattern = (SpecPattern) obj;
                        if (checked) {
                            List<SpecPattern> depends = specPattern.getAllDepends();
                            if (depends != null) {
                                for (SpecPattern depend : depends) {
                                    CheckableTreeNode checkableTreeNode = CheckableTreeUtil.getTreeNode(tree, depend);
                                    checkableTreeNode.setChecked(true);
                                }
                            }
                        }
                    }
                }
            }
        });

        RoutinePatternManager routinePatternManager = RoutinePatternManager.getInstance();
        RoutinePattern routinePattern = routinePatternManager.getRoutinePattern(routine.getDeviceType());
        CheckableTreeNode pageNode = new CheckableTreeNode("Page");
        CheckableTreeNode specNode = new CheckableTreeNode("Spec");
        List<EntityPagePattern> pagePatterns = routinePattern.pagePatterns;

        if (!pagePatterns.isEmpty()) {
            rootNode.add(pageNode);
        }
        for (EntityPagePattern pattern : pagePatterns) {
            CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
            patternNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/page.png")));
            patternNode.setCheckable(true);
            pageNode.add(patternNode);
        }
        List<SpecPattern> specPatterns = routinePattern.specPatterns;

        if (!specPatterns.isEmpty()) {
            rootNode.add(specNode);
        }
        for (SpecPattern pattern : specPatterns) {
            CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
            patternNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/spec.png")));
            patternNode.setCheckable(true);
            specNode.add(patternNode);
        }

        JScrollPane treeScroll = new JScrollPane(tree);

        JTreeUtil.expandTree(tree);
        DialogDescriptor desc = new DialogDescriptor(treeScroll,
                "Add To Routine", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            final List<EntityPagePattern> selectedPagePatterns = new ArrayList<EntityPagePattern>();
            CheckableTreeUtil.fetchChecked(pageNode, true, true, false, selectedPagePatterns);
            for (EntityPagePattern pagePattern : selectedPagePatterns) {
                routine.pagePatterns.add(pagePattern);
            }
            List<SpecPattern> selectedSpecPatterns = new ArrayList<SpecPattern>();
            CheckableTreeUtil.fetchChecked(specNode, true, true, false, selectedSpecPatterns);
            for (SpecPattern specPattern : selectedSpecPatterns) {
                routine.specPatterns.add(specPattern);
            }
            routine.updateVars(routinePattern);
            routineSelected(routine);
        }
    }

    protected void updateDeviceGroupComboModel(DefaultComboBoxModel<MeaDeviceGroup> deviceGroupComboModel, MeaSubDie subdie, DeviceType deviceType) {
        deviceGroupComboModel.removeAllElements();
        List<MeaDeviceGroup> groups = subdie.getGroups();
        for (MeaDeviceGroup dg : groups) {
            if (dg.getDeviceType().equals(deviceType)) {
                deviceGroupComboModel.addElement(dg);
            }
        }
    }

    public void copyRoutine(Routine routine) {
        final DeviceType deviceType = routine.getDeviceType();

        List<MeaSubDie> filteredDies = new ArrayList<MeaSubDie>();
        List<MeaSubDie> subDies = die.getSubDies();
        for (MeaSubDie subdie : subDies) {
            List<MeaDeviceGroup> groups = subdie.getGroups();
            for (MeaDeviceGroup dg : groups) {
                if (dg.getDeviceType().equals(deviceType)) {
                    filteredDies.add(subdie);
                    break;
                }
            }
        }

        DefaultComboBoxModel<MeaSubDie> subDieComboModel = new DefaultComboBoxModel<MeaSubDie>(filteredDies.toArray(new MeaSubDie[0]));
        final JComboBox<MeaSubDie> subDieCombo = new JComboBox<MeaSubDie>(subDieComboModel);

        final DefaultComboBoxModel<MeaDeviceGroup> deviceGroupComboModel = new DefaultComboBoxModel<MeaDeviceGroup>();
        final JComboBox<MeaDeviceGroup> deviceGroupCombo = new JComboBox<MeaDeviceGroup>(deviceGroupComboModel);

        subDieCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    MeaSubDie subdie = (MeaSubDie) subDieCombo.getSelectedItem();
                    updateDeviceGroupComboModel(deviceGroupComboModel, subdie, deviceType);
                    deviceGroupCombo.updateUI();
                }
            }
        });
        subDieCombo.setSelectedItem(subDie);
//        deviceGroupCombo.setSelectedItem(subDie);
        updateDeviceGroupComboModel(deviceGroupComboModel, subDie, deviceType);
        deviceGroupCombo.updateUI();

        JTextField nameField = new JTextField(routine.getName(), 30);

        JPanel labelPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        labelPanel.add(new JLabel("Sub Die:"));
        labelPanel.add(new JLabel("Device Group: "));
        labelPanel.add(new JLabel("Routine Name: "));

        JPanel centerPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        centerPanel.add(subDieCombo);
        centerPanel.add(deviceGroupCombo);
        centerPanel.add(nameField);

        JPanel form = new JPanel(new BorderLayout());
        GUIUtil.setEmptyBorder(form, 5);
        form.add(labelPanel, BorderLayout.WEST);
        form.add(centerPanel, BorderLayout.CENTER);

        DialogDescriptor desc = new DialogDescriptor(form,
                "Copy Routine", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            MeaDeviceGroup dg = (MeaDeviceGroup) deviceGroupCombo.getSelectedItem();
            Routine clone = new Routine(routine);
//            clone.setDeviceType(deviceGroup.getDeviceType());
            clone.setName(nameField.getText().trim());
            clone.setId(-1);
            dg.addRoutine(clone);
            if (deviceGroup == dg) {
                routineListModel.addElement(clone);
                routineList.updateUI();
                routineList.setSelectedIndex(routineListModel.getSize() - 1);
            }
        }
    }

    protected void editPattern(final CheckableTreeNode treeNode, final IndPattern pattern) {
        try {
            String title = (pattern instanceof IndPagePattern) ? "Edit Page" : "Edit Spec";
            final AbstractIndPattern aPattern = (AbstractIndPattern) pattern;
            final IndPattern clone = (IndPattern) aPattern.clone();
            final IndPatternEditPanel editPanel = new IndPatternEditPanel(aPattern);
            editPanel.setPattern(clone);

            DialogDescriptor desc = new DialogDescriptor(editPanel, title);
            final Dialog editDlg = DialogDisplayer.getDefault().createDialog(desc);
            final JButton okButton = new JButton("OK");
            final JButton cancelButton = new JButton("Cancel");
            ActionListener actionListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ev) {
                    if (ev.getSource() == okButton) {

                        if (editPanel.isUpdateEnabled()) {
                            String msg = "The content has been changed, please update first";
                            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                            DialogDisplayer.getDefault().notify(nd);
                        } else {
                            editPanel.stopEditing();
                            pattern.setPattern(clone.getPattern());
                            BaseVarProvider.copyVars(clone, aPattern);
                            MeaDAO dao = meaSpace.getDao();
                            selectedRoutine.updatePattern(dao, meaData, pattern);
                            treeNode.setUserObject(pattern.getName());
                            routineTree.updateUI();
                            patternPanel.setPattern(pattern);
                            editDlg.dispose();
                        }
                    }
                }
            };
            desc.setOptions(new Object[]{okButton, cancelButton});
            desc.setClosingOptions(new Object[]{cancelButton});
            desc.setButtonListener(actionListener);
            editDlg.setVisible(true);
        } catch (CloneNotSupportedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void routineSelected(Routine routine) {
        // TODO:

        selectedRoutine = (Routine) routine;
        routineRootNode.removeAllChildren();
        pageNode.removeAllChildren();
        specNode.removeAllChildren();

        if (stressPatternPanel != null) {
            stopEditingStress();
            bottomTotalPanel.remove(stressPatternPanel);
            stressPatternPanel = null;
            bottomTotalPanel.updateUI();
        }

        if (selectedRoutine != null) {
            List<IndPagePattern> pps = routine.getIndPagePatterns();
            for (IndPagePattern pattern : pps) {
                CheckableTreeNode patternNode = new CheckableTreeNode(pattern.toString(), pattern);
                if (routine.hasDeviceBond(pattern)) {
                    patternNode.setIcon(TreeVarSelector.pageValidIcon);
                } else {
                    patternNode.setIcon(TreeVarSelector.pageIcon);
                }

//                 pageNode.add(patternNode);
//                String group = pattern.getPattern().getGroupName();
                String group = AnalysisUtil.getPageGroup(pattern.getPattern());
                if (StringUtil.isValid(group)) {
                    CheckableTreeNode groupNode = CheckableTreeUtil.getBranchNodeByName(pageNode, group);
                    if (groupNode != null) {
                        groupNode.add(patternNode);
                    } else {
                        groupNode = new CheckableTreeNode(group, group);
                        groupNode.setChecked(true);
                        pageNode.add(groupNode);
                        groupNode.add(patternNode);
                    }
                } else {
                    pageNode.add(patternNode);
                }
            }

            List<IndSpecPattern> sps = routine.getIndSpecPatterns();
            for (IndSpecPattern pattern : sps) {
                CheckableTreeNode patternNode = new CheckableTreeNode(pattern.toString(), pattern);
                if (routine.hasDeviceBond(pattern)) {
                    patternNode.setIcon(TreeVarSelector.specValidIcon);
                } else {
                    patternNode.setIcon(TreeVarSelector.specIcon);
                }
                specNode.add(patternNode);
            }

            if (pageNode.getChildCount() > 0) {
                routineRootNode.add(pageNode);
            }
            if (specNode.getChildCount() > 0) {
                routineRootNode.add(specNode);
            }

            StressPattern stressPattern = routine.getStressPattern();
            if (stressPattern != null) {
                stressPatternPanel = new StressPatternPanel(routine, stressPattern);
                bottomTotalPanel.add(stressPatternPanel, BorderLayout.SOUTH);
                bottomTotalPanel.updateUI();
            }
        }
        routineTreeModel.reload(routineRootNode);
        JTreeUtil.expandTree(routineTree);
        routineTree.updateUI();

        if (routineRootNode.getChildCount() > 0) {
            JTreeUtil.selectFirstPath(routineTree);
        } else {
            updateContent(null);
        }
    }

    public void updateContent(Object lastPathComponent) {
        // TODO: device, nodes, instance setup
        // TODO: pattern in text, variables, connection

        stopEditingPatternVars();

        TreePath treePath = routineTree.getSelectionPath();
        if (treePath != null) {
            CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
            Object obj = treeNode.getAssociatedObject();
            if (obj != null && obj instanceof IndPattern) {
                IndPattern pattern = (IndPattern) obj;
                patternPanel.setPattern(pattern);
                return;
            }
        }
        patternPanel.setEmpty();
    }

    protected void stopEditingStress() {
        if (stressPatternPanel.hasEdited()) {
            stressPatternPanel.stopEditing();
            // remove results
            selectedRoutine.clearData(meaSpace.getDao());
            selectedRoutine.resetStress();
            //
        }
    }

    protected void stopEditingPatternVars() {
        IndPattern pattern = patternPanel.getPattern();
        if (pattern != null) {
            patternPanel.stopEditingVar();
            // TODO:

            BaseVarProvider varProvider = patternPanel.getVarProvider();
            if (BaseVarProvider.haveSameVars(pattern, varProvider)) {
            } else {
                pattern.updateVars(varProvider);
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    MeaDAO dao = meaSpace.getDao();
                    selectedRoutine.updatePattern(dao, meaData, pattern);
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == editItem || source == setConnectionItem || source == copyItem) {
            TreePath treePath = routineTree.getSelectionPath();
            if (treePath == null) {
                return;
            }
            CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
            Object obj = treeNode.getAssociatedObject();
            if (obj != null && obj instanceof IndPattern) {
                IndPattern pattern = (IndPattern) obj;
                if (source == editItem) {
                    editPattern(treeNode, pattern);
                } else if (source == setConnectionItem) {
                    RoutinePatternManager patternBondManager = RoutinePatternManager.getInstance();
                    RoutinePattern routinePattern = patternBondManager.getRoutinePattern(selectedRoutine.getDeviceType());
                    DeviceBondTreeBasePanel form = new DeviceBondTreeBasePanel(routinePattern, false);
                    CheckableTree connectionTree = form.getTree();
                    DeviceBond preBond = selectedRoutine.getDeviceBond(pattern);
                    if (preBond != null) {
                        CheckableTreeNode connectionNode = form.getRootNode();
                        CheckableTreeNode bondNode = CheckableTreeUtil.getTreeNode(connectionNode, preBond);
                        if (bondNode != null) {
                            bondNode.setIcon(DeviceBondTreePanel.badgeIcon);
                            connectionTree.updateUI();
                            TreePath tp = new TreePath(bondNode.getPath());
                            connectionTree.setSelectionPath(tp);
                        }
                    }
                    form.setPreferredSize(MeaOptions.COMPONENT_WIZARD_HIGH_SIZE);
                    DialogDescriptor desc = new DialogDescriptor(form,
                            "Set Connection", true, DialogDescriptor.OK_CANCEL_OPTION,
                            DialogDescriptor.OK_OPTION, null);
                    Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
                    if (result == NotifyDescriptor.OK_OPTION) {
                        TreePath tp = connectionTree.getSelectionPath();
                        CheckableTreeNode bondNode = (CheckableTreeNode) tp.getLastPathComponent();
                        obj = bondNode.getAssociatedObject();
                        if (obj != preBond) {
                            DeviceBond assignedBond = (DeviceBond) obj;
                            pattern.setDeviceBond(assignedBond);
                            pattern.setBondName(assignedBond.getName());
                            if (preBond == null) {
                                if (pattern instanceof IndPagePattern) {
                                    treeNode.setIcon(TreeVarSelector.pageValidIcon);
                                } else {
                                    treeNode.setIcon(TreeVarSelector.specValidIcon);
                                }
                                routineTree.updateUI();
                            }
                        }
                    }
                } else if (source == copyItem) {
                    try {
                        final AbstractIndPattern aPattern = (AbstractIndPattern) pattern;
                        final boolean isPage = (aPattern instanceof IndPagePattern);
                        String title = isPage ? "Copy Page" : "Copy Spec";
                        final IndPattern clone = (IndPattern) aPattern.clone();
                        final IndPatternEditPanel editPanel = new IndPatternEditPanel(aPattern);
                        editPanel.setPattern(clone);

                        DialogDescriptor desc = new DialogDescriptor(editPanel, title);
                        final Dialog editDlg = DialogDisplayer.getDefault().createDialog(desc);
                        final JButton okButton = new JButton("OK");
                        final JButton cancelButton = new JButton("Cancel");
                        ActionListener actionListener = new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ev) {
                                if (ev.getSource() == okButton) {

                                    if (editPanel.isUpdateEnabled()) {
                                        String msg = "The content has been changed, please update first";
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                                        DialogDisplayer.getDefault().notify(nd);
                                    } else {
                                        editPanel.stopEditing();
                                        if (isPage) {
                                            selectedRoutine.addIndPagePattern((IndPagePattern) clone);
                                        } else {
                                            selectedRoutine.addIndSpecPattern((IndSpecPattern) clone);
                                        }
                                        routineSelected(selectedRoutine);
                                        CheckableTreeNode treeNode = CheckableTreeUtil.getTreeNode(routineTree, clone);
                                        TreePath treePath = new TreePath(treeNode.getPath());
                                        routineTree.scrollPathToVisible(treePath);
                                        routineTree.setSelectionPath(treePath);
                                        editDlg.dispose();
                                    }
                                }
                            }
                        };
                        desc.setOptions(new Object[]{okButton, cancelButton});
                        desc.setClosingOptions(new Object[]{cancelButton});
                        desc.setButtonListener(actionListener);
                        editDlg.setVisible(true);
                    } catch (CloneNotSupportedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } else if (source == removeDeviceButton) {
            int index = deviceTable.getSelectedRow();
            index = deviceTable.convertRowIndexToModel(index);
            if (index >= 0) {
//                 MeaDevice meaDevice =  deviceGroup.getDevice(index);
                deviceGroup.removeDevice(index);
                deviceTableModel.fireTableDataChanged();
                JTableUtil.setSelectedIndex(deviceTable, deviceTableModel, index - 1);
            }
        } else if (source == addDeviceButton) {
            addDevice();
        } else if (source == addInstanceButton) {
            List<MeaDevice> devicesInGroup = deviceGroup.getDevices();
            if (devicesInGroup.isEmpty()) {
                return;
            }
            JTextField nameField = new JTextField(20);
            JTextField valueField = new JTextField(20);
            JPanel westPanel = new JPanel(new GridLayout(0, 1, 2, 2));
            JPanel fieldPanel = new JPanel(new GridLayout(0, 1, 2, 2));
            westPanel.add(new JLabel("Name: "));
            fieldPanel.add(nameField);
            westPanel.add(new JLabel("Value: "));
            fieldPanel.add(valueField);

            JPanel form = new JPanel(new BorderLayout());
            GUIUtil.setEmptyBorder(form, 5);
            form.add(westPanel, BorderLayout.WEST);
            form.add(fieldPanel, BorderLayout.CENTER);

            DialogDescriptor desc = new DialogDescriptor(form,
                    "Add Instance", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION, null);
            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result == NotifyDescriptor.OK_OPTION) {
                String name = nameField.getText().trim();
                String valueStr = valueField.getText().trim();

                double value = Double.parseDouble(valueStr);

                List<MeaDevice> devices = deviceGroup.getDevices();
                for (MeaDevice meaDevice : devices) {
                    EntityDevice device = meaDevice.getDevice();
                    device.setInstance(name, value);
                    onDeviceUpdated(meaDevice, name, value);
                }
                deviceTableModel.instanceChnaged();
            }
        } else if (source == removeInstanceButton) {
            List<MeaDevice> devicesInGroup = deviceGroup.getDevices();
            if (devicesInGroup.isEmpty()) {
                return;
            }
            String[] instanceNames = devicesInGroup.get(0).getDevice().getOrderedInstanceNames();
            if (instanceNames.length <= 1) {
                return;
            }
            DefaultComboBoxModel<String> instComboModel = new DefaultComboBoxModel<String>(instanceNames);
            JComboBox<String> instCombo = new JComboBox<String>(instComboModel);

            JPanel form = new JPanel(new GridLayout(0, 2, 2, 2));
            form.add(new JLabel("Instance: "));
            form.add(instCombo);

            DialogDescriptor desc = new DialogDescriptor(form,
                    "Remove Instance", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION, null);
            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result == NotifyDescriptor.OK_OPTION) {
                String instance = (String) instCombo.getSelectedItem();
                if (instance == null) {
                    return;
                }
                List<MeaDevice> devices = deviceGroup.getDevices();
                for (MeaDevice meaDevice : devices) {
                    EntityDevice device = meaDevice.getDevice();
                    device.removeInstance(instance);
                    List<MeaedDevice> results = meaData.getDevices(meaDevice);
                    for (MeaedDevice meaedDevice : results) {
                        meaedDevice.removeInstance(instance);
                    }
                }
                deviceTableModel.instanceChnaged();
            }
        }
    }

    @Override
    public void onDeviceUpdated(MeaDevice device, String instance, double value) {
        List<MeaedDevice> results = meaData.getDevices(device);
        for (MeaedDevice meaedDevice : results) {
            meaedDevice.setInstance(instance, value);
        }
    }

    @Override
    public void stopEditing() {
        if (stressPatternPanel != null) {
            stopEditingStress();
        }
        stopEditingPatternVars();
    }
}
