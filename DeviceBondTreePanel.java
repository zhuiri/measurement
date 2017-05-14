/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceType;
import com.platformda.iv.datacore.DeviceTypeManager;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.MeaSpace;
import com.platformda.iv.api.MeaEditor;
import com.platformda.iv.api.MeterProfile;
import com.platformda.iv.api.NodeBond;
import com.platformda.iv.api.Unit;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.spec.SpecPattern;
import com.platformda.utility.common.BasicFileFilter;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.utility.common.XMLUtil;
import com.platformda.utility.tree.CheckableTree;
import com.platformda.utility.tree.CheckableTreeModel;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.tree.CheckableTreeUtil;
import com.platformda.utility.ui.GUIUtil;
import com.platformda.utility.ui.JTreeUtil;
import com.platformda.utility.ui.TreePanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Junyi
 */
public class DeviceBondTreePanel extends TreePanel implements MeaEditor {

    public static final String KEY_CONNECTION = "connection";
    RoutinePattern routinePattern;
    RoutinePatternPanel routinePatternPanel;
    EntityPagePattern pagePattern;
    SpecPattern specPattern;
    //
    DeviceBondPanel deviceBondPanel;
    DeviceTag deviceTag = DeviceTypeManager.getDeviceTagByAbbreviation("nmosfet");
    Action addAction = new AbstractAction("Add...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            addDeviceBond();
        }
    };
    Action removeAction = new AbstractAction("Remove...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {

            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof DeviceBond) {
                    String msg = "Do you want to remove selected connection?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove connection", NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                    DeviceBond deviceBond = (DeviceBond) obj;
                    routinePattern.removeDeviceBond(deviceBond);
                    treeNode.removeFromParent();
                    treeModel.reload(rootNode);
                    tree.updateUI();

                    routinePatternPanel.updateIcons();
                }
            }
        }
    };
    Action loadAction = new AbstractAction("Load...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            String defaultPath = null;
            File defaultSettingsFile = InstalledFileLocator.getDefault().locate("etc", MeaSpace.CODE_BASE, false);
            if (defaultSettingsFile != null) {
                defaultPath = defaultSettingsFile.getAbsolutePath();
            }
            File file = LoadSaveUtil.openFile("Load Connection", JFileChooser.FILES_ONLY, KEY_CONNECTION, defaultPath, false);
            if (file != null) {
                InstrumentManager instrumentManager = InstrumentManager.getInstance();
                List<DeviceBond> deviceBonds = routinePattern.deviceBonds;
                Map<String, DeviceBond> bondMap = routinePattern.bondMap;
                DeviceType deviceType = routinePattern.getDeviceType();
                try {
                    Element root = XMLUtil.getRoot(file);

                    Map<String, DeviceBond> preBondMap = new HashMap<String, DeviceBond>(bondMap);
                    deviceBonds.clear();
                    bondMap.clear();

                    List<Element> bondElems = root.getChildren("bond");
                    for (Element bondElem : bondElems) {
                        DeviceBond deviceBond = new DeviceBond(bondElem.getAttributeValue("name"));
                        deviceBond.setDeviceType(deviceType);
                        deviceBonds.add(deviceBond);

                        List<Element> nodeElems = bondElem.getChildren("node");
                        NodeBond[] bonds = new NodeBond[nodeElems.size()];
                        deviceBond.setBonds(bonds);

                        for (int i = 0; i < nodeElems.size(); i++) {
                            Element nodeElem = nodeElems.get(i);
                            String node = nodeElem.getAttributeValue("nodename");

                            String instName = nodeElem.getAttributeValue("instname");
                            Unit unit = null;
                            if (instName != null) {
                                String termName = nodeElem.getAttributeValue("term");
                                EntityInstrument inst = instrumentManager.getInstrument(instName);
                                if (inst == null) {
                                    continue;
                                }
                                MeterProfile meterProfile = (MeterProfile) inst.getProfile();
                                int termNumber = meterProfile.getUnitNumber();
                                for (int termIndex = 0; termIndex < termNumber; termIndex++) {
                                    Unit u = meterProfile.getUnit(termIndex);
                                    if (u.getName().equalsIgnoreCase(termName)) {
                                        unit = u;
                                        break;
                                    }
                                }
                            }

                            NodeBond nodeBond = new NodeBond(instName, unit, node);
                            bonds[i] = nodeBond;
                        }

                        for (Map.Entry<String, DeviceBond> entry : preBondMap.entrySet()) {
                            String key = entry.getKey();
                            DeviceBond preBond = entry.getValue();
                            DeviceBond bond = routinePattern.getDeviceBondByName(preBond.getName());
                            if (bond != null) {
                                bondMap.put(key, bond);
                            }
                        }

                        rootNode.removeAllChildren();
                        buildTree();
                        treeModel.reload();
                        JTreeUtil.expandTree(tree);
                        tree.updateUI();
                    }

                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        }
    };
    Action setConnectionAction = new AbstractAction("Set Connection...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            DeviceBond deviceBond = null;
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof DeviceBond) {
                    deviceBond = (DeviceBond) obj;
                }
            }

            DeviceType deviceType = routinePattern.getDeviceType();
//            SetConnectionPanel form = new SetConnectionPanel(deviceType, routinePattern, routinePattern.pagePatterns, routinePattern.specPatterns);
            SetConnectionInTablePanel form = new SetConnectionInTablePanel(deviceType, routinePattern, routinePattern.pagePatterns, routinePattern.specPatterns, deviceBond);
            DialogDescriptor desc = new DialogDescriptor(form,
                    "Set Connection", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION, null);
            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result == NotifyDescriptor.OK_OPTION) {
                form.ok();
                updateIcon();
                routinePatternPanel.updateIcons();
            }
        }
    };
    Action setAsDefaultAction = new AbstractAction("Set As Default") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO:
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof DeviceBond) {
                    setAsDefault((DeviceBond) obj);
                }
            }
        }
    };
    Action setAsDefaultToAction = new AbstractAction("Set As Default To...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO:
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof DeviceBond) {
                    setAsDefaultTo((DeviceBond) obj);
                }
            }
        }
    };

    public DeviceBondTreePanel(RoutinePattern routinePattern, RoutinePatternPanel routinePatternPanel) {
        this.routinePattern = routinePattern;
        this.routinePatternPanel = routinePatternPanel;
        initComponents(120);
        JPanel toolbarPanel = getToolbarPanel();
        add(toolbarPanel, BorderLayout.NORTH);

        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
                if (treePath == null) {
                    return;
                }
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (e.isMetaDown()) {
                    if (obj != null && obj instanceof DeviceBond) {
                        if (pagePattern != null || specPattern != null) {
//                            DeviceBond deviceBond = (DeviceBond) obj;
                            JPopupMenu popupMenu = new JPopupMenu();
                            popupMenu.add(setAsDefaultAction);
//                            popupMenu.add(setAsDefaultToAction);
                            //popupMenu.addSeparator();
                           // popupMenu.add(setConnectionAction);
                            popupMenu.show(tree, e.getX(), e.getY());
                        }
                    }
                }
            }
        };
        tree.addMouseListener(mouseListener);
        setPreferredSize(MeaOptions.COMPONENT_META_WIDE_SIZE);
    }

    private JPanel getToolbarPanel() {
        JButton addButton = new JButton();
        addButton.setAction(addAction);
        addButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/iv/resources/add.gif")));
        addButton.setText("");
        addAction.putValue(Action.SHORT_DESCRIPTION, "Add");

        JButton removeButton = new JButton();
        removeButton.setAction(removeAction);
        removeButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/iv/resources/remove.gif")));
        removeButton.setText("");
        removeAction.putValue(Action.SHORT_DESCRIPTION, "Remove");

        JButton loadButton = new JButton();
        loadButton.setAction(loadAction);
        loadButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/iv/resources/import.png")));
        loadButton.setText("");
        loadAction.putValue(Action.SHORT_DESCRIPTION, "Load...");

        JButton dumpButton = new JButton();
        dumpButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/dump.png")));
        dumpButton.setText("");
        dumpButton.setToolTipText("Export...");
        dumpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                File file = LoadSaveUtil.saveFile("Export Spec Pattern", JFileChooser.FILES_ONLY, ExtractionProject.KEY_DIR_INI_SPEC_SAVE);
                final File file = LoadSaveUtil.saveFile("Export Connection", JFileChooser.FILES_ONLY, KEY_CONNECTION, null, new BasicFileFilter("xml"), "connection.xml", false);
                if (file != null) {
                    try {
                        Element root = new Element("bonds");
                        List<DeviceBond> deviceBonds = routinePattern.deviceBonds;
                        for (DeviceBond deviceBond : deviceBonds) {
                            Element bondElem = new Element("bond");
                            root.addContent(bondElem);
                            bondElem.setAttribute("name", deviceBond.getName());

                            NodeBond[] nodeBonds = deviceBond.getBonds();
                            for (NodeBond nodeBond : nodeBonds) {
                                Element nodeElem = new Element("node");
                                bondElem.addContent(nodeElem);
                                nodeElem.setAttribute("nodename", nodeBond.getNodeName());
                                Unit unit = nodeBond.getUnit();
                                if (unit != null) {
                                    nodeElem.setAttribute("instname", nodeBond.getInstName());
                                    nodeElem.setAttribute("term", unit.getName());
                                }
                            }
                        }

                        Format fmt = Format.getPrettyFormat();
                        fmt.setIndent("  ");
                        fmt.setEncoding("gb2312");
                        XMLOutputter outputtter = new XMLOutputter(fmt);
                        try {
                            FileWriter writer = new FileWriter(file);
                            outputtter.output(root, writer);
                            writer.flush();
                            writer.close();
                        } catch (IOException ioe) {
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DeviceBondTreePanel.class.getName()).severe("Failed to export connection");
                    }
                }
            }
        });

        JPanel toolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.LEADING, addButton, removeButton, null, loadButton, dumpButton);
//        JPanel toolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.LEADING, addButton, removeButton);
        return toolbarPanel;
    }

    public void addDeviceBond() {
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                "Name: ",
                "Add Connection",
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE);
        nd.setInputText("connection");
        Object result = DialogDisplayer.getDefault().notify(nd);
        if (result.equals(NotifyDescriptor.OK_OPTION)) {
            String name = nd.getInputText();
            DeviceBond deviceBond = new DeviceBond(name);
            deviceBond.setName(name);
            deviceBond.setDeviceType(routinePattern.getDeviceType());
            routinePattern.deviceBonds.add(deviceBond);

            CheckableTreeNode bondNode = new CheckableTreeNode(name, deviceBond);
            bondNode.setIcon(icon);
            rootNode.add(bondNode);
            treeModel.reload(bondNode);
            tree.updateUI();
            tree.setSelectionPath(new TreePath(bondNode.getPath()));
        }
    }

    public void highlighted(EntityPagePattern pagePattern) {
        this.pagePattern = pagePattern;
        this.specPattern = null;

        updateIcon();

        CheckableTreeNode treeNode = null;
        if (pagePattern != null) {
            DeviceBond deviceBond = routinePattern.getDeviceBond(pagePattern);
            treeNode = CheckableTreeUtil.getTreeNode(rootNode, deviceBond);
        }
        if (treeNode != null) {
            tree.setSelectionPath(new TreePath(treeNode.getPath()));
        } else {
            tree.clearSelection();
        }
    }

    public void highlighted(SpecPattern specPattern) {
        this.pagePattern = null;
        this.specPattern = specPattern;

        updateIcon();

        CheckableTreeNode treeNode = null;
        if (specPattern != null) {
            DeviceBond deviceBond = routinePattern.getDeviceBond(specPattern);
            treeNode = CheckableTreeUtil.getTreeNode(rootNode, deviceBond);
        }
        if (treeNode != null) {
            tree.setSelectionPath(new TreePath(treeNode.getPath()));
        } else {
            tree.clearSelection();
        }
    }

    public void clearHighlighted() {
        this.pagePattern = null;
        this.specPattern = null;
        updateIcon();
        tree.clearSelection();
    }

    public void setAsDefault(DeviceBond deviceBond) {
//        if (pagePattern != null) {
//            routinePattern.setDeviceBond(pagePattern, deviceBond);
//        } else if (specPattern != null) {
//            routinePattern.setDeviceBond(specPattern, deviceBond);
//        }
        routinePattern.setDefaultBond(deviceBond);
        updateIcon();
        routinePatternPanel.updateIcons();
    }

    public void setAsDefaultToInTable(DeviceBond deviceBond) {
    }

    public void setAsDefaultTo(DeviceBond deviceBond) {
        CheckableTree patternTree = new CheckableTree();
        CheckableTreeNode patternRootNode = new CheckableTreeNode("Pattern");
        CheckableTreeModel patternTreeModel = new CheckableTreeModel(patternTree, patternRootNode);
        patternTree.setModel(patternTreeModel);
        patternTree.setToggleClickCount(2);
        patternTree.setRootVisible(false);
        patternTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        CheckableTreeNode pageNode = new CheckableTreeNode("Page");
        CheckableTreeNode specNode = new CheckableTreeNode("Spec");
        pageNode.setCheckable(true);
        specNode.setCheckable(true);

        List<EntityPagePattern> pagePatterns = routinePattern.pagePatterns;

        for (EntityPagePattern pattern : pagePatterns) {
            DeviceBond bond = routinePattern.getDeviceBond(pattern);
            if (bond != deviceBond) {
                CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
                if (bond == null) {
                    patternNode.setIcon(TreeVarSelector.pageIcon);
                } else {
                    patternNode.setIcon(TreeVarSelector.pageValidIcon);
                }
                patternNode.setCheckable(true);
                pageNode.add(patternNode);
            }
        }
        if (pageNode.getChildCount() > 0) {
            patternRootNode.add(pageNode);
        }

        List<SpecPattern> specPatterns = routinePattern.specPatterns;
        for (SpecPattern pattern : specPatterns) {
            DeviceBond bond = routinePattern.getDeviceBond(pattern);
            if (bond != deviceBond) {
                CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
                if (bond == null) {
                    patternNode.setIcon(TreeVarSelector.specIcon);
                } else {
                    patternNode.setIcon(TreeVarSelector.specValidIcon);
                }
                patternNode.setCheckable(true);
                specNode.add(patternNode);
            }
        }
        if (specNode.getChildCount() > 0) {
            patternRootNode.add(specNode);
        }

//        CheckableTreeUtil.checkNodeByLeaf(rootNode);
        JTreeUtil.expandTree(patternTree);
        JScrollPane treeScroll = new JScrollPane(patternTree);
        treeScroll.setPreferredSize(MeaOptions.COMPONENT_SLIM_SIZE);
        DialogDescriptor desc = new DialogDescriptor(treeScroll,
                "Set As Default To", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        desc.setValid(true);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            List<EntityPagePattern> selectedPagePatterns = new ArrayList<EntityPagePattern>();
            CheckableTreeUtil.fetchChecked(pageNode, true, true, false, selectedPagePatterns);
            for (EntityPagePattern entityPagePattern : selectedPagePatterns) {
                routinePattern.setDeviceBond(entityPagePattern, deviceBond);
            }
            List<SpecPattern> selectedSpecPatterns = new ArrayList<SpecPattern>();
            CheckableTreeUtil.fetchChecked(specNode, true, true, false, selectedSpecPatterns);
            for (SpecPattern specPattern : selectedSpecPatterns) {
                routinePattern.setDeviceBond(specPattern, deviceBond);
            }
            updateIcon();
            routinePatternPanel.updateIcons();
        }
    }

    public void updateIcon() {
        DeviceBond deviceBond = null;
        if (pagePattern != null) {
            deviceBond = routinePattern.getDeviceBond(pagePattern);
        } else if (specPattern != null) {
            deviceBond = routinePattern.getDeviceBond(specPattern);
        }
        int number = rootNode.getChildCount();
        for (int i = 0; i < number; i++) {
            CheckableTreeNode bondNode = (CheckableTreeNode) rootNode.getChildAt(i);
            DeviceBond bond = (DeviceBond) bondNode.getAssociatedObject();
            if (bond == deviceBond) {
                bondNode.setIcon(badgeIcon);
            } else {
                bondNode.setIcon(icon);
            }
        }
//        tree.repaint();
        tree.updateUI();
    }
    public static final Image image = ImageUtilities.loadImage("com/platformda/iv/resources/who_calls.png");
//    public static final Image badge = ImageUtilities.loadImage("com/platformda/mea/resources/connected.png");
//    public static final Image badgeImage = ImageUtilities.mergeImages(image, badge, 8, 8);
    public static final Image badge = ImageUtilities.loadImage("com/platformda/iv/resources/existing-badge.png");
    public static final Image badgeImage = ImageUtilities.mergeImages(image, badge, 18, 0);
    public static final ImageIcon icon = new ImageIcon(image);
    public static final ImageIcon badgeIcon = new ImageIcon(badgeImage);

    @Override
    public void buildTree() {
//        rootNode.setUserObject("Devie Types");
//        tree.setRootVisible(true);
        List<DeviceBond> deviceBonds = routinePattern.deviceBonds;
        for (DeviceBond deviceBond : deviceBonds) {
            CheckableTreeNode bondNode = new CheckableTreeNode(deviceBond.getName(), deviceBond);
            bondNode.setIcon(icon);
            rootNode.add(bondNode);
        }
    }

    @Override
    public void updateContent(Object lastPathComponent) {
        contentPanel.removeAll();
        deviceBondPanel = null;
        CheckableTreeNode treeNode;
        if (lastPathComponent != null && lastPathComponent instanceof CheckableTreeNode) {
            treeNode = (CheckableTreeNode) lastPathComponent;
            if (treeNode.isLeaf()) {
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof DeviceBond) {
                    DeviceBond deviceBond = (DeviceBond) obj;
                    deviceBondPanel = new DeviceBondPanel(deviceBond);
                    contentPanel.add(deviceBondPanel, BorderLayout.CENTER);
                }
            }
        }

        contentPanel.updateUI();
    }

    @Override
    public void treeNodeChecked(CheckableTreeNode node, boolean checked) {
        // do nothing
    }

    @Override
    public void stopEditing() {
        if (deviceBondPanel != null) {
            deviceBondPanel.stopEditing();
        }
    }
}
