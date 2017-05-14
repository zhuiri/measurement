/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DevicePolarity;
import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceType;
import com.platformda.datacore.DeviceTypeManager;
import com.platformda.datacore.DeviceUtil;
import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.MeaData;
import com.platformda.iv.MeaSpace;
import com.platformda.iv.api.Bus;
import com.platformda.iv.api.BusException;
import com.platformda.iv.api.Line;
import com.platformda.iv.api.MeaEditor;
import com.platformda.iv.api.Probe;
import com.platformda.iv.api.Profile;
import com.platformda.iv.api.SubDieMeta;
import com.platformda.iv.measure.MeaDevice;
import com.platformda.iv.measure.MeaDeviceGroup;
import com.platformda.iv.measure.MeaDie;
import com.platformda.iv.measure.MeaSubDie;
import com.platformda.spec.SpecPattern;
import com.platformda.utility.common.BasicFileFilter;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.utility.common.MathUtil;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.provider.GenericSelectionPanel;
import com.platformda.utility.table.EnhancedCopyTable;
import com.platformda.utility.table.IconableCellRenderer;
import com.platformda.utility.tree.CheckableTree;
import com.platformda.utility.tree.CheckableTreeModel;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.tree.CheckableTreeUtil;
import com.platformda.utility.ui.GUIUtil;
import com.platformda.utility.ui.JTreeUtil;
import com.platformda.utility.ui.TreePanel;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.netbeans.swing.etable.ETable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * export & import
 *
 * @author Junyi
 */
public class DeviceManagePanel extends TreePanel implements ActionListener {

    public static final String KEY = "Device";
    MeaSpace meaSpace;
    MeaDie meaDie;
    //
    JTextField waferMappingPathField;
    JButton refreshButton;
    //
    //////
    protected CheckableTree dieTree;
    protected CheckableTreeModel dieTreeModel;
    protected CheckableTreeNode dieRootNode;
    //
    JButton viewDieByGeometryButton;
    //
    MeaEditor meaEditor;
    //
    Action addAction = new AbstractAction("Add...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };
    Action changePolarityAction = new AbstractAction("Change Device Polarity...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof MeaDeviceGroup) {
                    MeaDeviceGroup deviceGroup = (MeaDeviceGroup) obj;
                    MeaSubDie subDie = (MeaSubDie) ((CheckableTreeNode) treeNode.getParent()).getAssociatedObject();

                    List<DeviceTag> deviceTags = MeaDeviceTypeManager.getInstance().getAllDeviceTags();
                    List<DeviceTag> filteredDeviceTags = new ArrayList<DeviceTag>();
                    for (DeviceTag deviceTag : deviceTags) {
                        if (deviceTag.getDeviceType().equals(deviceGroup.getDeviceType())) {
                            filteredDeviceTags.add(deviceTag);
                        }
                    }
                    DefaultComboBoxModel<DeviceTag> deviceTagComboModel = new DefaultComboBoxModel<DeviceTag>(filteredDeviceTags.toArray(new DeviceTag[0]));
                    JComboBox<DeviceTag> deviceCombo = new JComboBox<DeviceTag>(deviceTagComboModel);
                    deviceCombo.setSelectedIndex(DeviceTypeManager.indexOf(filteredDeviceTags, deviceGroup));

                    JPanel labelPanel = new JPanel(new GridLayout(0, 1, 2, 2));
                    labelPanel.add(new JLabel("Device Type: "));

                    JPanel centerPanel = new JPanel(new GridLayout(0, 1, 2, 2));
                    centerPanel.add(deviceCombo);

                    JPanel form = new JPanel(new BorderLayout());
                    GUIUtil.setEmptyBorder(form, 5);
                    form.add(labelPanel, BorderLayout.WEST);
                    form.add(centerPanel, BorderLayout.CENTER);

                    DialogDescriptor desc = new DialogDescriptor(form,
                            "Change Device Polarity", true, DialogDescriptor.OK_CANCEL_OPTION,
                            DialogDescriptor.OK_OPTION, null);
                    Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
                    if (result == NotifyDescriptor.OK_OPTION) {
                        DeviceTag deviceTag = (DeviceTag) deviceCombo.getSelectedItem();
                        deviceGroup.setDeviceType(deviceTag.getDeviceType());
                        deviceGroup.setDevicePolarity(deviceTag.getDevicePolarity());
                        List<MeaDevice> devices = deviceGroup.getDevices();
                        for (MeaDevice meaDevice : devices) {
                            meaDevice.getDevice().setDeviceType(deviceTag.getDeviceType());
                            meaDevice.getDevice().setDevicePolarity(deviceTag.getDevicePolarity());
                        }
                        treeNode.setUserObject(deviceGroup.getNameByDeviceType());
                        tree.repaint();
                    }
                }
            }
        }
    };
    Action copyAction = new AbstractAction("Copy...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof MeaDeviceGroup) {
                    MeaDeviceGroup deviceGroup = (MeaDeviceGroup) obj;
                    MeaSubDie subDie = (MeaSubDie) ((CheckableTreeNode) treeNode.getParent()).getAssociatedObject();
                    try {
                        copyDeviceGroup(subDie, deviceGroup);
                    } catch (CloneNotSupportedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    };
    Action advCopyAction = new AbstractAction("Advanced Copy...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof MeaDeviceGroup) {
                    MeaDeviceGroup deviceGroup = (MeaDeviceGroup) obj;
                    MeaSubDie subDie = (MeaSubDie) ((CheckableTreeNode) treeNode.getParent()).getAssociatedObject();
                    try {
                        advcopyDeviceGroup(subDie, deviceGroup);
                    } catch (CloneNotSupportedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    };
    Action mappingAction = new AbstractAction("Mapping...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof MeaDeviceGroup) {
                    MeaDeviceGroup deviceGroup = (MeaDeviceGroup) obj;
                    MeaSubDie subDie = (MeaSubDie) ((CheckableTreeNode) treeNode.getParent()).getAssociatedObject();
                    try {
                        mappingDeviceGroup(subDie, deviceGroup);
                    } catch (CloneNotSupportedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
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
                if (obj != null && obj instanceof MeaSubDie) {
                    MeaSubDie subDie = (MeaSubDie) obj;
                    String msg = "Do you want to remove sub die?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove", NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                    meaDie.removeSubDie(subDie);
                    treeNode.removeFromParent();
                    treeModel.reload();
                    JTreeUtil.expandTree(tree);

                    updateContent(null);
                } else if (obj != null && obj instanceof MeaDeviceGroup) {
                    MeaDeviceGroup deviceGroup = (MeaDeviceGroup) obj;
                    MeaSubDie subDie = (MeaSubDie) ((CheckableTreeNode) treeNode.getParent()).getAssociatedObject();

                    String msg = "Do you want to remove device group?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove", NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                    subDie.removeGroup(deviceGroup);
                    treeNode.removeFromParent();
                    treeModel.reload();
                    JTreeUtil.expandTree(tree);

                    meaEditor = null;
                    updateContent(null);
                }
            }
        }
    };
    Action loadAction = new AbstractAction("Load...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            // spec pattern or device page pattern
            File file = LoadSaveUtil.openFile("Load Device", JFileChooser.FILES_ONLY, KEY);
            if (file != null) {
            }
        }
    };
    Action renameAction = new AbstractAction("Rename...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof MeaSubDie) {
                    MeaSubDie subDie = (MeaSubDie) obj;
                    NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                            "Name: ",
                            "Rename",
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.PLAIN_MESSAGE);
                    nd.setInputText(subDie.getName());
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result.equals(NotifyDescriptor.OK_OPTION)) {
                        String name = nd.getInputText();
                        subDie.setName(name);
                        treeNode.setUserObject(name);
//                        tree.repaint();
                        tree.updateUI();
                    }
                } else if (obj != null && obj instanceof MeaDeviceGroup) {
                    MeaDeviceGroup deviceGroup = (MeaDeviceGroup) obj;
                    NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                            "Name: ",
                            "Rename",
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.PLAIN_MESSAGE);
                    nd.setInputText(deviceGroup.getName());
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result.equals(NotifyDescriptor.OK_OPTION)) {
                        String name = nd.getInputText();
                        deviceGroup.setName(name);
                        treeNode.setUserObject(deviceGroup.getNameByDeviceType());
//                        tree.repaint();
                        tree.updateUI();
                    }
                }
            }
        }
    };
    Action removeDieAction = new AbstractAction("Remove...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = dieTree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof WaferDieInfo) {
                    WaferDieInfo dieInfo = (WaferDieInfo) obj;
                    String msg = "Do you want to remove die?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove", NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        return;
                    }

                    WaferInfo waferInfo = meaSpace.getWaferInfo();
                    waferInfo.removeDieInfo(dieInfo);

                    treeNode.removeFromParent();
                    dieTreeModel.reload();
                    JTreeUtil.expandTree(dieTree);

//                    updateContent(null);
                }
            }
        }
    };
    Dialog addDieDialog;
    Action addDieAction = new AbstractAction("Add Die...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            JPanel labelPanel = new JPanel(new GridLayout(0, 1, 2, 2));
            labelPanel.add(new JLabel("Index(1-based):"));
            labelPanel.add(new JLabel("X: "));
            labelPanel.add(new JLabel("Y: "));

            JPanel centerPanel = new JPanel(new GridLayout(0, 1, 2, 2));
            final JTextField indexField = new JTextField("1", 20);
            final JTextField xField = new JTextField("0", 20);
            final JTextField yField = new JTextField("0", 20);
            centerPanel.add(indexField);
            centerPanel.add(xField);
            centerPanel.add(yField);

            JPanel form = new JPanel(new BorderLayout());
            GUIUtil.setEmptyBorder(form, 5);
            form.add(labelPanel, BorderLayout.WEST);
            form.add(centerPanel, BorderLayout.CENTER);

            final JButton cancelButton = new JButton("Cancel");
            final JButton okButton = new JButton("OK");
            DialogDescriptor desc = new DialogDescriptor(form,
                    "Add Die", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION,
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ev) {
                            if (ev.getSource() == okButton) {
                                int index = Integer.parseInt(indexField.getText());
                                int x = Integer.parseInt(xField.getText());
                                int y = Integer.parseInt(yField.getText());

                                WaferInfo waferInfo = meaSpace.getWaferInfo();
                                int dieNumber = waferInfo.getDieNumber();
                                for (int dieIndex = 0; dieIndex < dieNumber; dieIndex++) {
                                    WaferDieInfo dieInfo = waferInfo.getDieInfo(dieIndex);
                                    if (dieInfo.getDieIndex() == index) {
                                        String msg = "Die with index " + index + " already exists.";
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                                        nd.setTitle("Error");
                                        DialogDisplayer.getDefault().notify(nd);
                                        return;
                                    } else if (dieInfo.getX() == x && dieInfo.getY() == y) {
                                        String msg = "Die with x=" + x + " and y=" + y + " already exists.";
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                                        nd.setTitle("Error");
                                        DialogDisplayer.getDefault().notify(nd);
                                        return;
                                    }
                                }
                                WaferDieInfo dieInfo = new WaferDieInfo(x, y, index);
                                waferInfo.addDieInfo(dieInfo);
                                CheckableTreeNode dieNode = new CheckableTreeNode(dieInfo.toString(), dieInfo);
                                dieNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/die.png")));
                                dieRootNode.add(dieNode);
                                dieTreeModel.reload(dieRootNode);
                                dieTree.updateUI();

                                addDieDialog.setVisible(false);
                                addDieDialog.dispose();
                                addDieDialog = null;
                            }
                        }
                    });
            desc.setValid(true);
            desc.setOptions(new Object[]{okButton, cancelButton});
            desc.setClosingOptions(new Object[]{cancelButton});
            addDieDialog = DialogDisplayer.getDefault().createDialog(desc);
            addDieDialog.setVisible(true);
        }
    };
    Action addSubDieAction = new AbstractAction("Add Sub Die...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                    "Name: ",
                    "Add Sub Die",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE);
            nd.setInputText("manual");
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (result.equals(NotifyDescriptor.OK_OPTION)) {
                String name = nd.getInputText();

                MeaSubDie subDie = new MeaSubDie(name);
                subDie.setSubDieIndex(meaDie.getNextSubDieIndex());
                meaDie.addSubDie(subDie);

                CheckableTreeNode subDieNode = new CheckableTreeNode(subDie.getName(), subDie);
                subDieNode.setCheckable(false);
                subDieNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/subdie.png")));
                rootNode.add(subDieNode);

                treeModel.reload(rootNode);
                JTreeUtil.expandTree(tree);
                tree.updateUI();
                tree.setSelectionPath(new TreePath(subDieNode.getPath()));
            }
        }
    };
    Action loadStvAction = new AbstractAction("Load Sub Dies From File...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
//                loadSTV();
                loadSubDies();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (BusException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    };
    Action editSubDiesAction = new AbstractAction("Edit Sub Dies") {
        @Override
        public void actionPerformed(ActionEvent e) {
            SubDieManagePanel form = new SubDieManagePanel(DeviceManagePanel.this);
            DialogDescriptor desc = new DialogDescriptor(form, "Edit Sub Dies");
            desc.setOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
            DialogDisplayer.getDefault().notify(desc); // displays the dialog
        }
    };
    Action loadSubDieSettingsAction = new AbstractAction("Load Settings...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                File file = LoadSaveUtil.openFile("Load Settings", JFileChooser.FILES_ONLY, "SubDie", null, false);
                if (file != null) {
                    loadSubdieDeviceAndRoutine(file.getAbsolutePath());
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    };
    Action exportSubDieSettingsAction = new AbstractAction("Export Settings...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                final File file = LoadSaveUtil.saveFile("Export Settings", JFileChooser.FILES_ONLY, "SubDie", null, new BasicFileFilter("xls"), "subdieSettings.xls", false);
                if (file != null) {
//                    saveSubdieSettings(file);
                    saveSubdieDeviceAndRoutine(file);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    };
    Action clearSubDieSettingsAction = new AbstractAction("Remove Device Groups...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = "Do you want to remove all device groups?";
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove Device Groups", NotifyDescriptor.YES_NO_OPTION);
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (result != NotifyDescriptor.YES_OPTION) {
                return;
            }

            int subNumber = meaDie.getSubDieNumber();
            for (int i = 0; i < subNumber; i++) {
                MeaSubDie subDie = meaDie.getSubDie(i);
                subDie.getGroups().clear();
            }

            // refresh UI
            rootNode.removeAllChildren();
            buildTree();
            treeModel.reload(rootNode);
            JTreeUtil.expandTree(tree);
            updateContent(null);
        }
    };
    Action addTemperatureAction = new AbstractAction("Add Temperatures...") {
        @Override
        public void actionPerformed(ActionEvent e) {

            NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                    "Temperatures(,): ",
                    "Add Temperatures",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE);
            nd.setInputText("-40"); // -40, 25, 125(150)
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (result.equals(NotifyDescriptor.OK_OPTION)) {
                String s = nd.getInputText();
                String[] parts = s.split(",");
                for (String part : parts) {
                    double t = Double.parseDouble(part);
                    int subNumber = meaDie.getSubDieNumber();
                    for (int i = 0; i < subNumber; i++) {
                        MeaSubDie subDie = meaDie.getSubDie(i);
                        addTemp(t, subDie);
                    }
                }
                TreePath treePath = tree.getSelectionPath();
                if (treePath != null) {
                    CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                    updateContent(treeNode);
                }
            }
        }
    };

    class AddDeviceGroupAction extends AbstractAction {

        MeaSubDie subDie;

        public AddDeviceGroupAction(MeaSubDie subDie) {
            super("Add Device Group");
            this.subDie = subDie;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addDeviceGroup(subDie);
        }
    }

    public DeviceManagePanel(final MeaSpace meaSpace) {
        this.meaSpace = meaSpace;
        meaDie = meaSpace.getMeaDie();
        initComponents(220);
        rootNode.setUserObject("SubDie");
        tree.setRootVisible(true);

        dieTree = new CheckableTree();
        dieRootNode = new CheckableTreeNode("Die");
        dieTreeModel = new CheckableTreeModel(dieTree, dieRootNode);
        dieTree.setModel(dieTreeModel);
        dieTree.setToggleClickCount(2);
        dieTree.setRootVisible(true);
        dieTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
//        dieTree.addTreeSelectionListener(new TreeSelectionListener() {
//            @Override
//            public void valueChanged(TreeSelectionEvent e) {
//                if (e.getNewLeadSelectionPath() != null) {
//                    updateContent(e.getPath().getLastPathComponent());
//                }
//            }
//        });
        MouseListener dieMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath treePath = dieTree.getPathForLocation(e.getX(), e.getY());
                if (treePath == null) {
                    return;
                }

                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (e.isMetaDown() && meaSpace.getMode() == MeaSpace.MODE_MANUAL) {
                    if (treeNode == dieRootNode) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.add(addDieAction);
                        popupMenu.show(dieTree, e.getX(), e.getY());
                    } else if (obj != null && obj instanceof WaferDieInfo) {
                        WaferInfo waferInfo = meaSpace.getWaferInfo();
                        int dieNumber = waferInfo.getDieNumber();
                        if (dieNumber > 1) {
                            JPopupMenu popupMenu = new JPopupMenu();
                            popupMenu.add(removeDieAction);
                            popupMenu.show(dieTree, e.getX(), e.getY());
                        }
                    }
                }
            }
        };
        dieTree.addMouseListener(dieMouseListener);

        buildDieTree();
        JTreeUtil.expandTree(dieTree);
        JScrollPane dieScroll = new JScrollPane(dieTree);

        viewDieByGeometryButton = new JButton("");
        viewDieByGeometryButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/geometry.png")));
        viewDieByGeometryButton.setToolTipText("View die by geometry");
        viewDieByGeometryButton.addActionListener(this);
        JPanel dieToolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, viewDieByGeometryButton);
        JPanel dieWrapPanel = new JPanel(new BorderLayout());
        dieWrapPanel.add(dieScroll, BorderLayout.CENTER);
        dieWrapPanel.add(dieToolbarPanel, BorderLayout.NORTH);

        leftPanel.setLayout(new GridLayout(0, 1, 2, 2));
        leftPanel.add(dieWrapPanel);
        leftPanel.add(treeScroll);

//        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
//        leftSplitPane.setBorder(null);
//        leftSplitPane.setLeftComponent(dieWrapPanel);
//        leftSplitPane.setRightComponent(treeScroll);
//        leftSplitPane.setResizeWeight(0.4);
//        leftPanel.add(leftSplitPane, BorderLayout.CENTER);

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
                    if (treeNode == rootNode && meaSpace.getMode() == MeaSpace.MODE_MANUAL) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.add(loadSubDieSettingsAction);
                        popupMenu.add(exportSubDieSettingsAction);
                        popupMenu.add(clearSubDieSettingsAction);
                        popupMenu.add(addTemperatureAction);
                        popupMenu.addSeparator();
                        popupMenu.add(addSubDieAction);
                        popupMenu.show(tree, e.getX(), e.getY());
                    } else if (treeNode == rootNode && meaSpace.getMode() == MeaSpace.MODE_PROBE) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.add(loadSubDieSettingsAction);
                        popupMenu.add(exportSubDieSettingsAction);
                        popupMenu.add(clearSubDieSettingsAction);
                        popupMenu.add(addTemperatureAction);
                        EntityInstrument entityProbe = InstrumentManager.getInstance().getProbe();
//                        Probe probe = (Probe) entityProbe.getInstrument();
//                        if ((probe instanceof PA300)) {
//                            popupMenu.addSeparator();
//                            popupMenu.add(loadStvAction);
//                            popupMenu.add(editSubDiesAction);
//                        }
                        if (entityProbe != null) {
                            popupMenu.addSeparator();
                            popupMenu.add(loadStvAction);
                            popupMenu.add(editSubDiesAction);
                        }
                        popupMenu.show(tree, e.getX(), e.getY());
                    } else if (obj != null && obj instanceof MeaSubDie) {
                        MeaSubDie subDie = (MeaSubDie) obj;
                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.add(new AddDeviceGroupAction(subDie));
                        popupMenu.addSeparator();
                        popupMenu.add(renameAction);
                        popupMenu.add(removeAction);
                        popupMenu.show(tree, e.getX(), e.getY());
                    } else if (obj != null && obj instanceof MeaDeviceGroup) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.add(renameAction);
                        popupMenu.add(removeAction);
                        popupMenu.addSeparator();
                        popupMenu.add(changePolarityAction);
                        popupMenu.add(copyAction);
                        popupMenu.add(advCopyAction);
                        popupMenu.add(mappingAction);
                        popupMenu.show(tree, e.getX(), e.getY());
                    }
                }
            }
        };
        tree.addMouseListener(mouseListener);

//        JPanel toolbarPanel = getToolbarPanel();
//        add(toolbarPanel, BorderLayout.NORTH);

        waferMappingPathField = new JTextField("", 64);
        waferMappingPathField.setEditable(false);
        refreshButton = new JButton("Refresh");
        JPanel pathPanel = new JPanel(new BorderLayout());
        pathPanel.add(new JLabel("Wafer Mapping: "), BorderLayout.WEST);
        pathPanel.add(waferMappingPathField, BorderLayout.CENTER);
        pathPanel.add(refreshButton, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        JPanel comboPanel = new JPanel(new BorderLayout(5, 5));
        String[] modes = MeaSpace.MODE_STRINGS;
        final JComboBox comboBox = new JComboBox(modes);
        int mode = meaSpace.getMode();
        comboBox.setSelectedIndex(mode);
        comboPanel.add(new JLabel("Mode:"), BorderLayout.WEST);
        comboPanel.add(comboBox, BorderLayout.CENTER);
        topPanel.add(pathPanel, BorderLayout.CENTER);
        topPanel.add(comboPanel, BorderLayout.WEST);
        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int index = comboBox.getSelectedIndex();
                    updateSpaceOnModeChanged(index);
                    updateUIOnModeChanged(index);
                }
            }
        });
        add(topPanel, BorderLayout.NORTH);
        updateUIOnModeChanged(mode);

        refreshButton.addActionListener(this);

//        setPreferredSize(MeaOptions.COMPONENT_META_LARGE_SIZE); //  new Dimension(720, 640)
        setPreferredSize(new Dimension(920, 620)); // 1024 x 768
        JTreeUtil.selectFirstPath(tree);
    }

    protected void updateUIOnModeChanged(int mode) {
        if (mode == MeaSpace.MODE_MANUAL) {
            waferMappingPathField.setEnabled(false);
            refreshButton.setEnabled(false);
        } else {
            //
            waferMappingPathField.setEnabled(true);
            refreshButton.setEnabled(true);
        }
    }

    public void updateSpaceOnModeChanged(int mode) {
        meaSpace.setMode(mode);

        if (mode == MeaSpace.MODE_MANUAL) {
            WaferInfo waferInfo = meaSpace.getWaferInfo();
            if (waferInfo.getDieNumber() == 0) {
                WaferDieInfo dieInfo = new WaferDieInfo(0, 0, 1);
                waferInfo.addDieInfo(dieInfo);
                CheckableTreeNode dieNode = new CheckableTreeNode(dieInfo.toString(), dieInfo);
                dieNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/die.png")));
                dieRootNode.add(dieNode);
                dieTreeModel.reload(dieRootNode);
                dieTree.updateUI();
            }

            if (meaDie.getSubDieNumber() == 0) {
                MeaSubDie subDie = new MeaSubDie("manual");
                subDie.setSubDieIndex(meaDie.getNextSubDieIndex());
                meaDie.addSubDie(subDie);
                CheckableTreeNode subDieNode = new CheckableTreeNode(subDie.getName(), subDie);
                subDieNode.setCheckable(false);
                subDieNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/subdie.png")));
                rootNode.add(subDieNode);

                treeModel.reload(rootNode);
                JTreeUtil.expandTree(tree);
                tree.updateUI();
                tree.setSelectionPath(new TreePath(subDieNode.getPath()));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == refreshButton) {
            try {
                refresh();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (source == viewDieByGeometryButton) {
            viewDieInGeometry();
        }
    }

    protected void viewDieInGeometry() {
        final WaferInfo waferInfo = meaSpace.getWaferInfo();
        final DieGeometryTableModel tableModel = new DieGeometryTableModel(waferInfo);

        EnhancedCopyTable table = new EnhancedCopyTable(tableModel);
//        table.getTableHeader().setReorderingAllowed(false);
//        table.displaySearchField();

        JScrollPane deviceScroll = new JScrollPane(table);
        deviceScroll.setBorder(null);

        final JComboBox<String> orderCombo = new JComboBox<String>(new DefaultComboBoxModel(Probe.orders));
        orderCombo.setSelectedIndex(waferInfo.getOrder());
//        JPanel orderPanel = new JPanel(new BorderLayout(5, 5));
//        orderPanel.add(new JLabel("X,Y Order: "), BorderLayout.WEST);
//        orderPanel.add(orderCombo, BorderLayout.CENTER);
        JPanel orderPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        orderPanel.add(new JLabel("X,Y Order: "));
        orderPanel.add(orderCombo);
        orderCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int order = orderCombo.getSelectedIndex();
                    tableModel.onOrderChanged(waferInfo.getOrder(), order);
                    waferInfo.setOrder(order);
                }
            }
        });

        JPanel form = new JPanel(new BorderLayout());
        form.add(deviceScroll, BorderLayout.CENTER);
        form.add(orderPanel, BorderLayout.SOUTH);

        DialogDescriptor desc = new DialogDescriptor(form, "View die in geometry");
        desc.setOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
        DialogDisplayer.getDefault().notify(desc); // displays the dialog
    }

    protected void refreshSeudo() throws IOException {
        WaferInfo waferInfo = meaSpace.getWaferInfo();
        String waferMappingPath = "waferMappingPath";
        meaSpace.setWaferMappingPath(waferMappingPath);
        waferMappingPathField.setText(waferMappingPath);

        waferInfo.clearDieInfos();
        for (int i = 0; i < 10; i++) {
            WaferDieInfo dieInfo = new WaferDieInfo(i, i, i);
            waferInfo.addDieInfo(dieInfo);
        }
        dieRootNode.removeAllChildren();
        buildDieTree();
        dieTreeModel.reload();
        JTreeUtil.expandTree(dieTree);

        meaDie.clearSubDies();
        for (int subDieIndex = 0; subDieIndex < 2; subDieIndex++) {
            MeaSubDie subDie = new MeaSubDie("Tile" + subDieIndex);
            subDie.setSubDieIndex(subDieIndex);
            meaDie.addSubDie(subDie);
        }
        rootNode.removeAllChildren();
        buildTree();
        treeModel.reload(rootNode);
        JTreeUtil.expandTree(tree);
        updateContent(null);
    }

    protected void refresh() throws IOException, BusException {
        WaferInfo waferInfo = new WaferInfo();
        EntityInstrument entityProbe = InstrumentManager.getInstance().getProbe();

        Probe probe = (Probe) entityProbe.getInstrument();
        Profile conf = entityProbe.getProfile();
        probe.setProfile(conf);
        Bus bus = null;
        Line line = null;
        String[] subDieNames = null;
        try {
            waferInfo.setOrder(probe.getXYOrder());
            if (probe.isOnGlobalBus()) {
                bus = InstrumentManager.getInstance().createBus();
                bus.open();
                line = bus.connect(conf.getAddress());
                probe.setLine(line);
            }
            probe.open();
            String path = probe.getWaferMappingPath();
            waferInfo.setPath(path);

            // NOTE, die index is 1-based, sub die index is 0-based
            // TODO:
//            int dieNumber = probe.getDieNumber();
//            for (int i = 1; i <= dieNumber; i++) {
//                int[] loc = probe.getDieCoordsByIndex(i);
//                waferInfo.addDieInfo(new WaferDieInfo(loc[0], loc[1], i));
//            }
            int[][] allDieCoords = probe.getAllDieCoords();
            for (int i = 0; i < allDieCoords.length; i++) {
                int[] coords = allDieCoords[i];
                waferInfo.addDieInfo(new WaferDieInfo(coords[0], coords[1], i + 1));
            }

//            waferInfo.setSubsiteNames(subDieNames);

            int[] ref = probe.getReference();
            if (ref != null) {
                waferInfo.setRefX(ref[0]);
                waferInfo.setRefY(ref[1]);
            }

//            wafer.setDiameter(probe.getDiameter());
//            wafer.setQualitySize(probe.getQualitySize());
//            wafer.setXDieSize(probe.getXDieSize());
//            wafer.setYDieSize(probe.getYDieSize());
//            double[] shift = probe.getGridShift();
//            wafer.setXGridShift(shift[0]);
//            wafer.setYGridShift(shift[1]);
//            wafer.setXStreet(probe.getXStreet());
//            wafer.setYStreet(probe.getYStreet());

            waferMappingPathField.setText(path);

            downloadSubDies(probe);
            probe.close();
            meaSpace.setWaferMappingPath(path);
            // NOTE, waferInfo is a new object !!!
            // SHOULD update the references in all codes.
            meaSpace.setWaferInfo(waferInfo);

            dieRootNode.removeAllChildren();
            buildDieTree();
            dieTreeModel.reload();
            JTreeUtil.expandTree(dieTree);

            rootNode.removeAllChildren();
            buildTree();
            treeModel.reload(rootNode);
            JTreeUtil.expandTree(tree);
            updateContent(null);
        } finally {
            if (line != null) {
                line.disconnect();
            }
            if (bus != null) {
                bus.close();
            }
        }
    }

    // PTC 
    protected void loadSubdieDeviceAndRoutine(String excelPath) throws IOException {
        Workbook workbook = null;
        if (excelPath.toLowerCase().endsWith(".xls")) {
            workbook = new HSSFWorkbook(new FileInputStream(excelPath));
        } else if (excelPath.toLowerCase().endsWith(".xlsx")) {
//            workbook = new XSSFWorkbook(new FileInputStream(excelPath));
        }
        if (workbook == null) {
//            JOptionPane.showMessageDialog(null, "Parsing the specified Excel failed!", "Message", JOptionPane.PLAIN_MESSAGE);
            return;
        }

        int subNumber = meaDie.getSubDieNumber();
        for (int i = 0; i < subNumber; i++) {
            MeaSubDie subDie = meaDie.getSubDie(i);
            subDie.getGroups().clear();
        }

        RoutineManager routineManager = RoutineManager.getInstance();
        MeaDeviceTypeManager deviceTypeManager = MeaDeviceTypeManager.getInstance();

        Sheet sheet = workbook.getSheetAt(0);
        int rowStart = sheet.getFirstRowNum();
        int rowEnd = sheet.getLastRowNum();
        int columnStart = 0, columnEnd = 0;
        for (int rowIndex = rowStart; rowIndex <= rowEnd;) {
            Row row = sheet.getRow(rowIndex);
            rowIndex++;
            if (row == null) {
                continue;
            }
            columnStart = row.getFirstCellNum();
            Cell cell = row.getCell(columnStart);
            if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING) {
                continue;
            }
            String str = cell.getStringCellValue();
            if (!str.equalsIgnoreCase("type")) {
                continue;
            }
            cell = row.getCell(columnStart + 1);
            if (cell == null || cell.getCellType() != Cell.CELL_TYPE_STRING) {
                continue;
            }
            str = cell.getStringCellValue();
            DeviceType deviceType = deviceTypeManager.getDeviceType(str); // TODO:
            if (deviceType == null) {
                continue;
            }
            List<Routine> routines = routineManager.getRoutines(deviceType);
            String[] nodeNames = deviceType.getNodeNames();

            // load header
            row = sheet.getRow(rowIndex);
            rowIndex++;
            if (row == null) {
                continue;
            }
            columnStart = row.getFirstCellNum();
            cell = row.getCell(columnStart);
            if (cell == null) {
                continue;
            }
            if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
                continue;
            }
            str = cell.getStringCellValue();
            if (!str.equalsIgnoreCase("subdie")) {
                continue;
            }
            columnEnd = row.getLastCellNum();
            List<String> instAndNodes = new ArrayList<String>();
            for (int columnIndex = columnStart + 3; columnIndex < columnEnd; columnIndex++) {
                cell = row.getCell(columnIndex);
                if (cell == null) {
                    break;
                }
                if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
                    break;
                }
                str = cell.getStringCellValue();
                if (str.equalsIgnoreCase("routine")) {
                    break;
                }
                instAndNodes.add(str);
            }

            // load device
            int instLength = instAndNodes.size();
            for (int i = 0; i < instAndNodes.size(); i++) {
                String s = instAndNodes.get(i);
                if (StringUtil.contains(nodeNames, s)) {
                    instLength = i;
                    break;
                }
            }

            MeaSubDie lastSubDie = null;
            MeaDeviceGroup lasDeviceGroup = null;
            Routine lastRoutine = null;
            for (; rowIndex <= rowEnd;) {
                row = sheet.getRow(rowIndex);
                rowIndex++;
                if (row == null) {
                    break;
                }
                // next device type?
                int firstColumn = row.getFirstCellNum();
                cell = row.getCell(firstColumn);
                if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    str = cell.getStringCellValue();
                    if (str.equalsIgnoreCase("type")) {
                        rowIndex--;
                        break;
                    }
                }
                cell = row.getCell(columnStart);
                if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    str = cell.getStringCellValue();
                    lastSubDie = meaDie.getSubDieByName(str);
                }
                cell = row.getCell(columnStart + 1);
                if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    str = cell.getStringCellValue();
                    String groupName = str;
                    String polarity = row.getCell(columnStart + 2).getStringCellValue();
                    DevicePolarity devicePolarity = deviceTypeManager.getDevicePolarity(polarity);
                    lasDeviceGroup = new MeaDeviceGroup();
                    lasDeviceGroup.setName(groupName);
                    lasDeviceGroup.setDeviceType(deviceType);
                    lasDeviceGroup.setDevicePolarity(devicePolarity);
                    if (lastSubDie != null) {
                        lastSubDie.addGroup(lasDeviceGroup);
                    }
                }
                if (lastSubDie == null || lasDeviceGroup == null) {
                    continue;
                }

                cell = row.getCell(columnStart + 3);
                if (cell != null && (cell.getCellType() == Cell.CELL_TYPE_STRING || cell.getCellType() == Cell.CELL_TYPE_NUMERIC)) {
                    // load device
                    EntityDevice device = new EntityDevice();
                    device.setDeviceType(deviceType);
                    device.setDevicePolarity(lasDeviceGroup.getDevicePolarity());
                    MeaDevice meaDevice = new MeaDevice(device);
                    meaDevice.setSubDie(lastSubDie);
                    meaDevice.setDeviceGroup(lasDeviceGroup);
                    lasDeviceGroup.addDevice(meaDevice);
                    for (int i = 0; i < instLength; i++) {
                        cell = row.getCell(columnStart + 3 + i);
                        double value = Double.NaN;
                        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                            String valueStr = cell.getStringCellValue();
                            value = Double.parseDouble(valueStr);
                        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            value = cell.getNumericCellValue();
                        }
                        device.setInstance(instAndNodes.get(i), value);
                    }
                    for (int i = instLength; i < instAndNodes.size(); i++) {
                        cell = row.getCell(columnStart + 3 + i);
                        int value = -1;
                        if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                            String valueStr = cell.getStringCellValue();
                            value = Integer.parseInt(valueStr);
                        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            value = (int) cell.getNumericCellValue();
                        }
                        meaDevice.setPin(instAndNodes.get(i), value);
                    }
                }

                int routineIndex = columnStart + 3 + instAndNodes.size();
                cell = row.getCell(routineIndex);
                if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    str = cell.getStringCellValue();

                    if (StringUtil.isValid(str)) {
                        Routine routine = null; // Get routine from pattern
                        if (routines != null) {
                            for (Routine r : routines) {
                                if (r.getName().equalsIgnoreCase(str)) {
                                    routine = r;
                                    break;
                                }
                            }
                        }
                        if (routine == null) {
                            lastRoutine = null;
                            continue;
                        }

                        Routine added = new Routine(routine);
                        lasDeviceGroup.addRoutine(added);
                        lastRoutine = routine;
                    }
                }

                int pageSpecIndex = routineIndex + 1;
                cell = row.getCell(pageSpecIndex);
                if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING && lastRoutine != null) {
                    str = cell.getStringCellValue();
                    if (StringUtil.isValid(str)) {
                        IndPattern ip = null;
                        EntityPagePattern pp = lastRoutine.getPagePattern(str);
                        if (pp != null) {
                            ip = lastRoutine.getIndPagePattern(pp);
                        } else {
                            SpecPattern sp = lastRoutine.getSpecPatternByName(str);
                            if (sp != null) {
                                ip = lastRoutine.getIndSpecPattern(sp);
                            }
                        }
                        if (ip != null) {
                            columnEnd = row.getLastCellNum();
                            for (int columnIndex = pageSpecIndex + 1; columnIndex < columnEnd - 1; columnIndex++) {
                                cell = row.getCell(columnIndex);
                                if (cell == null) {
                                    break;
                                }
                                if (cell.getCellType() != Cell.CELL_TYPE_STRING) {
                                    break;
                                }
                                String var = cell.getStringCellValue();
                                cell = row.getCell(columnIndex + 1);
                                if (cell == null) {
                                    break;
                                }
                                double value = Double.NaN;
                                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                                    String valueStr = cell.getStringCellValue();
                                    value = Double.parseDouble(valueStr);
                                } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                                    value = cell.getNumericCellValue();
                                }
                                if (!Double.isNaN(value)) {
                                    ip.setVar(var, value);
                                }
                            }
                        }
                    }
                }
            }
        }

        rootNode.removeAllChildren();
        buildTree();
        treeModel.reload(rootNode);
        JTreeUtil.expandTree(tree);
        updateContent(null);

    }

    // version powered by PTC
    protected void saveSubdieDeviceAndRoutine(File resultFile) throws FileNotFoundException, IOException {
        // by device type
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("subdie");
        sheet.setColumnWidth(0, 32 * 256);
        sheet.setColumnWidth(1, 32 * 256);

        CellStyle redColorStyle = workbook.createCellStyle();
//                        style.setFillForegroundColor(HSSFColor.RED.index);
//                        // style.setFillBackgroundColor(HSSFColor.RED.index);
//                        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        HSSFFont font = workbook.createFont();
        font.setColor(HSSFColor.RED.index);
        redColorStyle.setFont(font);

        int subDieNumber = meaDie.getSubDieNumber();
        List<MeaDeviceGroup> groups = new ArrayList();

        HSSFRow row;
        HSSFCell cell;
        int rowIndex = 0;
        int cellIndex = 0;

        MeaData meaData = meaSpace.getMeaData();
        List<RoutineTuple> rts = new ArrayList<RoutineTuple>();
        List<EntityDevice> devices = new ArrayList<EntityDevice>();
        List<DeviceType> deviceTypes = meaData.getDistinctDeviceTypes();
        for (DeviceType dt : deviceTypes) {
            row = sheet.createRow((short) rowIndex);
            cell = row.createCell(0);
            cell.setCellValue("type");
            cell.setCellStyle(redColorStyle);
            cell = row.createCell(1);
            cell.setCellValue(dt.getName());
            cell.setCellStyle(redColorStyle);
            rowIndex++;

            meaData.fetchRoutineTuples(dt, rts);
            devices.clear();
            for (RoutineTuple rt : rts) {
                devices.add(rt.meaDevice.getDevice());
            }

            List<String> instNames = DeviceUtil.getAllInstanceNames(dt, devices, null);
            String[] nodeNames = dt.getNodeNames();

            row = sheet.createRow((short) rowIndex);
            cell = row.createCell(0);
            cell.setCellValue("subdie");
            cell = row.createCell(1);
            cell.setCellValue("group");
            cell = row.createCell(2);
            cell.setCellValue("polarity");
            cellIndex = 3;
            for (String instName : instNames) {
                cell = row.createCell(cellIndex++);
                cell.setCellValue(instName);
            }
            for (String nodeName : nodeNames) {
                cell = row.createCell(cellIndex++);
                cell.setCellValue(nodeName);
            }

            cell = row.createCell(cellIndex++);
//            cell.setCellValue("group");
//            cell = row.createCell(cellIndex++);
            cell.setCellValue("routine");
            cell = row.createCell(cellIndex++);
            cell.setCellValue("page/spec");
            rowIndex++;

            for (int subIndex = 0; subIndex < subDieNumber; subIndex++) {
                MeaSubDie subDie = meaDie.getSubDie(subIndex);
                subDie.fetchGroups(dt, groups);
                if (groups.isEmpty()) {
                    continue;
                }
                cellIndex = 0;
                row = sheet.createRow((short) rowIndex);
                cell = row.createCell(cellIndex++);
                cell.setCellValue(subDie.getName());

                for (int groupIndex = 0; groupIndex < groups.size(); groupIndex++) {
                    MeaDeviceGroup group = groups.get(groupIndex);
                    if (groupIndex != 0) {
                        row = sheet.createRow((short) rowIndex);
                    }
                    cellIndex = 1;
                    cell = row.createCell(cellIndex++);
                    cell.setCellValue(group.getName());
                    cell = row.createCell(cellIndex++);
                    cell.setCellValue(group.getDevicePolarity().getName());

                    List<MeaDevice> devicesInGroup = group.getDevices();
                    int deviceSize = devicesInGroup.size();

                    List<Routine> routinesInGroup = group.getRoutines();
                    int routineSize = routinesInGroup.size();

                    int groupStartRow = rowIndex;
                    // devices
                    for (int deviceIndex = 0; deviceIndex < devicesInGroup.size(); deviceIndex++) {
                        if (deviceIndex != 0) {
                            rowIndex++;
                            row = sheet.createRow((short) rowIndex);
                        }
                        cellIndex = 3;
                        MeaDevice meaDevice = devicesInGroup.get(deviceIndex);
                        EntityDevice device = meaDevice.getDevice();
                        for (String instName : instNames) {
                            cell = row.createCell(cellIndex++);
                            cell.setCellValue(device.getInstance(instName));
                        }
                        for (String nodeName : nodeNames) {
                            cell = row.createCell(cellIndex++);
                            cell.setCellValue(meaDevice.getPin(nodeName));
                        }
                    }

                    int maxRowIndex = groupStartRow + deviceSize;
                    // routines
                    rowIndex = groupStartRow;
                    for (int routineIndex = 0; routineIndex < routineSize; routineIndex++) {
                        Routine routine = routinesInGroup.get(routineIndex);
                        if (rowIndex < maxRowIndex) {
                            row = sheet.getRow(rowIndex);
                        } else {
                            row = sheet.createRow((short) rowIndex);
                        }

                        cellIndex = instNames.size() + nodeNames.length + 3;
                        cell = row.createCell(cellIndex++);
                        cell.setCellValue(routine.getName());

                        List<IndPagePattern> indPagePatterns = routine.getIndPagePatterns();
                        for (IndPagePattern ipp : indPagePatterns) {
                            if (rowIndex < maxRowIndex) {
                                row = sheet.getRow(rowIndex);
                            } else {
                                row = sheet.createRow((short) rowIndex);
                            }
                            List<String> varNames = ipp.getVarNames();
                            int patternCellIndex = cellIndex;
                            cell = row.createCell(patternCellIndex++);
                            cell.setCellValue(ipp.getName());
                            for (String varName : varNames) {
                                cell = row.createCell(patternCellIndex++);
                                cell.setCellValue(varName);
                                cell = row.createCell(patternCellIndex++);
                                cell.setCellValue(ipp.getDouble(varName));
                            }
                            rowIndex++;
                        }
                        List<IndSpecPattern> indSpecPatterns = routine.getIndSpecPatterns();
                        for (IndSpecPattern isp : indSpecPatterns) {
                            if (rowIndex < maxRowIndex) {
                                row = sheet.getRow(rowIndex);
                            } else {
                                row = sheet.createRow((short) rowIndex);
                            }
                            List<String> varNames = isp.getVarNames();
                            int patternCellIndex = cellIndex;
                            cell = row.createCell(patternCellIndex++);
                            cell.setCellValue(isp.getName());
                            for (String varName : varNames) {
                                cell = row.createCell(patternCellIndex++);
                                cell.setCellValue(varName);
                                cell = row.createCell(patternCellIndex++);
                                cell.setCellValue(isp.getDouble(varName));
                            }
                            rowIndex++;
                        }
                    }

                    if (maxRowIndex > rowIndex) {
                        rowIndex = maxRowIndex;
                    }
                }
                groups.clear();
            }

            rts.clear();
            rowIndex++;
            rowIndex++;
        }

        FileOutputStream fOut = new FileOutputStream(resultFile);
        workbook.write(fOut);
        fOut.flush();
        fOut.close();

    }

    protected void addTemp(double t, MeaSubDie subDie) {
        List<MeaDeviceGroup> groups = subDie.getGroups();
        for (MeaDeviceGroup deviceGroup : groups) {
            addTemp(t, subDie, deviceGroup);
        }
    }

    protected void addTemp(double t, MeaSubDie subDie, MeaDeviceGroup deviceGroup) {
        List<MeaDevice> devices = deviceGroup.getDevices();
        if (devices.isEmpty()) {
            return;
        }
        List<EntityDevice> diffTempDevices = new ArrayList<EntityDevice>();
        List<EntityDevice> sameTempDevices = new ArrayList<EntityDevice>();
        for (MeaDevice meaDevice : devices) {
            EntityDevice device = meaDevice.getDevice();
            double temp = device.getInstance("T");
            if (MathUtil.compare(t, temp) == 0) {
                sameTempDevices.add(device);
            } else {
                diffTempDevices.add(device);
            }
        }
        String[] ignores = {"T"};

        if (!sameTempDevices.isEmpty()) {
            List<EntityDevice> kins = DeviceUtil.getKinDevices(diffTempDevices, sameTempDevices, ignores);
            diffTempDevices.removeAll(kins);
        }
        if (diffTempDevices.isEmpty()) {
            return;
        }
        sameTempDevices.clear();
        for (EntityDevice device : diffTempDevices) {

            EntityDevice clone = (EntityDevice) device.clone();
            clone.setInstance("T", t);
            if (!sameTempDevices.contains(clone)) {
                sameTempDevices.add(clone);

                int[] pins = deviceGroup.getMeaDeviceByDevice(device).getPins();
                MeaDevice meaDevice = new MeaDevice(clone);
                meaDevice.setPins(Arrays.copyOf(pins, pins.length));
                meaDevice.setSubDie(subDie);
                meaDevice.setDeviceGroup(deviceGroup);

                deviceGroup.addDevice(meaDevice);
            }
        }
    }

    protected void loadSubDies() throws IOException, BusException {
        EntityInstrument entityProbe = InstrumentManager.getInstance().getProbe();
        Probe probe = (Probe) entityProbe.getInstrument();
        String ext = probe.getSubDieSettingsExtension();
        // load file
//        File file = LoadSaveUtil.openFile("Load ." + ext, JFileChooser.FILES_ONLY, ext.toUpperCase(), null, false);
        File file = LoadSaveUtil.openFile("Load Sub Dies From File", JFileChooser.FILES_ONLY, ext.toUpperCase(), null, false);
        if (file == null) {
            return;
        }

        List<SubDieMeta> metas = probe.loadSubDies(file);

        Profile profile = entityProbe.getProfile();
        probe.setProfile(profile);
        Bus bus = null;
        Line line = null;
        try {
            if (probe.isOnGlobalBus()) {
                bus = InstrumentManager.getInstance().createBus();
                bus.open();
                line = bus.connect(profile.getAddress());
                probe.setLine(line);
            }
            probe.open();
            probe.setSubDies(metas);

            // verify
            downloadSubDies(probe);
            probe.close();
            rootNode.removeAllChildren();
            buildTree();
            treeModel.reload(rootNode);
            JTreeUtil.expandTree(tree);
            updateContent(null);
        } finally {
            if (line != null) {
                line.disconnect();
            }
            if (bus != null) {
                bus.close();
            }
        }
    }

    public void downloadSubDies(Probe probe) throws IOException {
        int subDieNumber = probe.getSubDieNumber();
//            subDieNames = new String[subDieNumber];
//            for (int i = 0; i < subDieNumber; i++) {
//                subDieNames[i] = probe.getSubDieName(i);
//            }
        List<String> markedNames = new ArrayList<String>();
        List<Integer> markedSubDieIndice = new ArrayList<Integer>();
        for (int subdieIndex = 0; subdieIndex < subDieNumber; subdieIndex++) {
            if (probe.isSubDieMarked(subdieIndex)) {
                markedNames.add(probe.getSubDieName(subdieIndex));
                markedSubDieIndice.add(subdieIndex);
            }
        }
        subDieNumber = markedNames.size();
        String[] subDieNames = markedNames.toArray(new String[subDieNumber]);
        boolean keepIfSameNumber = false;
        if (keepIfSameNumber && meaDie.getSubDieNumber() == subDieNumber) {
            for (int i = 0; i < subDieNumber; i++) {
                MeaSubDie subDie = meaDie.getSubDie(i);
                subDie.setSubDieIndex(markedSubDieIndice.get(i));
                subDie.setName(subDieNames[i]);
            }
        } else {
            boolean keepSameSubDie = true;
            if (keepSameSubDie) {
                List<MeaSubDie> subDies = new ArrayList();
                for (int i = 0; i < subDieNumber; i++) {
                    MeaSubDie subDie = meaDie.getSubDieByName(subDieNames[i]);
                    if (subDie == null) {
                        subDie = new MeaSubDie(subDieNames[i]);
                    }
                    subDie.setSubDieIndex(markedSubDieIndice.get(i));
                    subDies.add(subDie);
                }
                meaDie.clearSubDies();
                meaDie.getSubDies().addAll(subDies);
            } else {
                meaDie.clearSubDies();
                for (int i = 0; i < subDieNumber; i++) {
                    MeaSubDie subDie = new MeaSubDie(subDieNames[i]);
                    subDie.setSubDieIndex(markedSubDieIndice.get(i));
                    meaDie.addSubDie(subDie);
                }
            }
        }
    }

    public void syncSubDie(List<SubDieMeta> metas) {
        List<MeaSubDie> subDies = new ArrayList();
        for (int i = 0; i < metas.size(); i++) {
            SubDieMeta meta = metas.get(i);
            MeaSubDie subDie = meaDie.getSubDieByName(meta.name);
            if (subDie == null) {
                subDie = new MeaSubDie(meta.name);
            }
            subDie.setSubDieIndex(meta.index);
            subDies.add(subDie);
        }
        meaDie.clearSubDies();
        meaDie.getSubDies().addAll(subDies);
        rootNode.removeAllChildren();
        buildTree();
        treeModel.reload(rootNode);
        JTreeUtil.expandTree(tree);
        updateContent(null);
    }

    protected void copyDeviceGroup(MeaSubDie subDie, MeaDeviceGroup deviceGroup) throws CloneNotSupportedException {
        List<DeviceTag> deviceTags = MeaDeviceTypeManager.getInstance().getAllDeviceTags();
        List<DeviceTag> filteredDeviceTags = new ArrayList<DeviceTag>();
        for (DeviceTag deviceTag : deviceTags) {
            if (deviceTag.getDeviceType().equals(deviceGroup.getDeviceType())) {
                filteredDeviceTags.add(deviceTag);
            }
        }

        DefaultComboBoxModel<DeviceTag> deviceTagComboModel = new DefaultComboBoxModel<DeviceTag>(filteredDeviceTags.toArray(new DeviceTag[0]));
        JComboBox<DeviceTag> deviceCombo = new JComboBox<DeviceTag>(deviceTagComboModel);
        deviceCombo.setSelectedIndex(DeviceTypeManager.indexOf(filteredDeviceTags, deviceGroup));

        JTextField nameField = new JTextField("devciegroup", 30);
        nameField.setText(deviceGroup.getName());

        JComboBox<MeaSubDie> subDieCombo = null;
        int subNumber = meaDie.getSubDieNumber();
        if (subNumber > 1) {
            DefaultComboBoxModel<MeaSubDie> subDieComboModel = new DefaultComboBoxModel<MeaSubDie>(meaDie.getSubDiesInArray());
            subDieCombo = new JComboBox<MeaSubDie>(subDieComboModel);
            subDieCombo.setSelectedItem(subDie);
        }

        JPanel labelPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        labelPanel.add(new JLabel("Sub Die:"));
        labelPanel.add(new JLabel("Device Type: "));
        labelPanel.add(new JLabel("Group Name: "));

        JPanel centerPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        if (subDieCombo != null) {
            centerPanel.add(subDieCombo);
        } else {
//            centerPanel.add(new JTextField(subDie.getName()));
            JTextField subDieField = new JTextField(subDie.getName());
            subDieField.setEditable(false);
            centerPanel.add(subDieField);
        }
        centerPanel.add(deviceCombo);
        centerPanel.add(nameField);

        JPanel form = new JPanel(new BorderLayout());
        GUIUtil.setEmptyBorder(form, 5);
        form.add(labelPanel, BorderLayout.WEST);
        form.add(centerPanel, BorderLayout.CENTER);

        DialogDescriptor desc = new DialogDescriptor(form,
                "Copy Device Group", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            if (subDieCombo != null) {
                subDie = (MeaSubDie) subDieCombo.getSelectedItem();
            }
            MeaDeviceGroup copy = new MeaDeviceGroup();
            copy.setName(nameField.getText().trim());
            DeviceTag deviceTag = (DeviceTag) deviceCombo.getSelectedItem();
            copy.setDeviceType(deviceTag.getDeviceType());
            copy.setDevicePolarity(deviceTag.getDevicePolarity());

            List<MeaDevice> devices = deviceGroup.getDevices();
            for (MeaDevice meaDevice : devices) {
                MeaDevice clone = (MeaDevice) meaDevice.clone();
                clone.getDevice().setDeviceType(deviceTag.getDeviceType());
                clone.getDevice().setDevicePolarity(deviceTag.getDevicePolarity());
                clone.setSubDie(subDie);
                clone.setDeviceGroup(copy);
                copy.addDevice(clone);
            }
            List<Routine> routines = deviceGroup.getRoutines();
            for (Routine routine : routines) {
//                Routine clone = (Routine) routine.clone();
                Routine clone = new Routine(routine);
                clone.setDeviceType(deviceTag.getDeviceType());
                clone.setId(-1);
                copy.addRoutine(clone);
            }
            subDie.addGroup(copy);

            CheckableTreeNode subDieNode = CheckableTreeUtil.getTreeNode(tree, subDie);
            if (subDieNode != null) {
                CheckableTreeNode groupNode = new CheckableTreeNode(copy.getNameByDeviceType(), copy);
                groupNode.setCheckable(false);
                groupNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/devicegroup.png")));
                subDieNode.add(groupNode);
                treeModel.reload(subDieNode);
                tree.updateUI();
                tree.setSelectionPath(new TreePath(groupNode.getPath()));
            }
        }
    }

    protected void advcopyDeviceGroup(MeaSubDie subDie, MeaDeviceGroup deviceGroup) throws CloneNotSupportedException {
        List<MeaSubDie> subDies = new ArrayList<MeaSubDie>(meaDie.getSubDies());
        subDies.remove(subDie);
        GenericSelectionPanel<MeaSubDie> selectionPanel = new GenericSelectionPanel<MeaSubDie>(subDies, "Sub Die", "Sub Dies", false);

        IconableCellRenderer cellRenderer = new IconableCellRenderer();
        ETable table = selectionPanel.getTable();
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(1).setCellRenderer(cellRenderer);

        JCheckBox deviceBox = new JCheckBox("Copy Device");
        JCheckBox routineBox = new JCheckBox("Copy Routine");
        deviceBox.setSelected(true);
        routineBox.setSelected(true);

        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        optionsPanel.add(deviceBox);
        optionsPanel.add(routineBox);

        JPanel form = new JPanel(new BorderLayout());
        form.add(selectionPanel, BorderLayout.CENTER);
        form.add(optionsPanel, BorderLayout.SOUTH);

        DialogDescriptor desc = new DialogDescriptor(form,
                "Advanced Copy Device Group", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            boolean copyDevice = deviceBox.isSelected();
            boolean copyRoutine = routineBox.isSelected();
            if (!copyDevice && !copyRoutine) {
                return;
            }
            List<MeaSubDie> selected = selectionPanel.getSelected();
            if (selected.isEmpty()) {
                return;
            }
            for (MeaSubDie msd : selected) {
                MeaDeviceGroup copy = new MeaDeviceGroup();
                copy.setName(deviceGroup.getName());
                copy.setDeviceType(deviceGroup.getDeviceType());
                copy.setDevicePolarity(deviceGroup.getDevicePolarity());

                if (copyDevice) {
                    List<MeaDevice> devices = deviceGroup.getDevices();
                    for (MeaDevice meaDevice : devices) {
                        MeaDevice clone = (MeaDevice) meaDevice.clone();
                        clone.setSubDie(msd);
                        clone.setDeviceGroup(copy);
                        copy.addDevice(clone);
                    }
                }
                if (copyRoutine) {
                    List<Routine> routines = deviceGroup.getRoutines();
                    for (Routine routine : routines) {
//                Routine clone = (Routine) routine.clone();
                        Routine clone = new Routine(routine);
                        clone.setId(-1);
                        copy.addRoutine(clone);
                    }
                }
                msd.addGroup(copy);

                CheckableTreeNode subDieNode = CheckableTreeUtil.getTreeNode(tree, msd);
                if (subDieNode != null) {
                    CheckableTreeNode groupNode = new CheckableTreeNode(copy.getNameByDeviceType(), copy);
                    groupNode.setCheckable(false);
                    groupNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/devicegroup.png")));
                    subDieNode.add(groupNode);
                    treeModel.reload(subDieNode);
                }
            }
            JTreeUtil.expandTree(tree);
            tree.updateUI();
            // TODO: selection
        }
    }

    protected void mappingDeviceGroup(MeaSubDie subDie, MeaDeviceGroup deviceGroup) throws CloneNotSupportedException {
        List<MeaSubDie> subDies = new ArrayList<MeaSubDie>(meaDie.getSubDies());
        subDies.remove(subDie);
        GenericSelectionPanel<MeaSubDie> selectionPanel = new GenericSelectionPanel<MeaSubDie>(subDies, "Sub Die", "Sub Dies", false);

        IconableCellRenderer cellRenderer = new IconableCellRenderer();
        ETable table = selectionPanel.getTable();
        TableColumnModel tcm = table.getColumnModel();
        tcm.getColumn(1).setCellRenderer(cellRenderer);

        JCheckBox deviceBox = new JCheckBox("Mapping Device");
        JCheckBox routineBox = new JCheckBox("Mapping Routine");
        deviceBox.setSelected(false);
        routineBox.setSelected(true);

        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        optionsPanel.add(deviceBox);
        optionsPanel.add(routineBox);

        JPanel form = new JPanel(new BorderLayout());
        form.add(selectionPanel, BorderLayout.CENTER);
        form.add(optionsPanel, BorderLayout.SOUTH);

        DialogDescriptor desc = new DialogDescriptor(form,
                "Mapping Device Group By Same Name and Device Polarity", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            boolean copyDevice = deviceBox.isSelected();
            boolean copyRoutine = routineBox.isSelected();
            if (!copyDevice && !copyRoutine) {
                return;
            }
            List<MeaSubDie> selected = selectionPanel.getSelected();
            if (selected.isEmpty()) {
                return;
            }
            String groupName = deviceGroup.getName();
            DeviceType deviceType = deviceGroup.getDeviceType();
            DevicePolarity devicePolarity = deviceGroup.getDevicePolarity();
            List<MeaDevice> devices = deviceGroup.getDevices();
            List<Routine> routines = deviceGroup.getRoutines();

            for (MeaSubDie msd : selected) {
                List<MeaDeviceGroup> groups = msd.getGroups();
                for (MeaDeviceGroup group : groups) {
                    if (group.getName().equalsIgnoreCase(groupName)
                            && group.getDeviceType().equals(deviceType)
                            && group.getDevicePolarity().equals(devicePolarity)) {
                        if (copyDevice) {
                            group.getDevices().clear();
                            for (MeaDevice meaDevice : devices) {
                                MeaDevice clone = (MeaDevice) meaDevice.clone();
                                clone.setSubDie(msd);
                                clone.setDeviceGroup(group);
                                group.addDevice(clone);
                            }
                        }
                        if (copyRoutine) {
                            group.getRoutines().clear();
                            for (Routine routine : routines) {
                                Routine clone = new Routine(routine);
                                clone.setId(-1);
                                group.addRoutine(clone);
                            }
                        }
                    }
                }
            }
        }
    }

    protected void addDeviceGroup(MeaSubDie subDie) {
        List<DeviceTag> deviceTags = MeaDeviceTypeManager.getInstance().getAllDeviceTags();
        DefaultComboBoxModel<DeviceTag> deviceTagComboModel = new DefaultComboBoxModel<DeviceTag>(deviceTags.toArray(new DeviceTag[0]));
        JComboBox<DeviceTag> deviceCombo = new JComboBox<DeviceTag>(deviceTagComboModel);
        JTextField nameField = new JTextField("devciegroup", 30);

        JComboBox<MeaSubDie> subDieCombo = null;
        int subNumber = meaDie.getSubDieNumber();
        if (subNumber > 1) {
            DefaultComboBoxModel<MeaSubDie> subDieComboModel = new DefaultComboBoxModel<MeaSubDie>(meaDie.getSubDiesInArray());
            subDieCombo = new JComboBox<MeaSubDie>(subDieComboModel);
            subDieCombo.setSelectedItem(subDie);
        }

        JPanel labelPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        labelPanel.add(new JLabel("Sub Die:"));
        labelPanel.add(new JLabel("Device Type: "));
        labelPanel.add(new JLabel("Group Name: "));

        JPanel centerPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        if (subDieCombo != null) {
            centerPanel.add(subDieCombo);
        } else {
//            centerPanel.add(new JTextField(subDie.getName()));
            JTextField subDieField = new JTextField(subDie.getName());
            subDieField.setEditable(false);
            centerPanel.add(subDieField);
        }
        centerPanel.add(deviceCombo);
        centerPanel.add(nameField);

        JPanel form = new JPanel(new BorderLayout());
        GUIUtil.setEmptyBorder(form, 5);
        form.add(labelPanel, BorderLayout.WEST);
        form.add(centerPanel, BorderLayout.CENTER);

        DialogDescriptor desc = new DialogDescriptor(form,
                "Add Device Group", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            if (subDieCombo != null) {
                subDie = (MeaSubDie) subDieCombo.getSelectedItem();
            }
            MeaDeviceGroup deviceGroup = new MeaDeviceGroup();
            deviceGroup.setName(nameField.getText().trim());
            DeviceTag deviceTag = (DeviceTag) deviceCombo.getSelectedItem();
            deviceGroup.setDeviceType(deviceTag.getDeviceType());
            deviceGroup.setDevicePolarity(deviceTag.getDevicePolarity());
            subDie.addGroup(deviceGroup);

            // add default device
            EntityDevice device = DeviceTypeManager.getDefaultDevice(deviceGroup);
            MeaDevice meaDevice = new MeaDevice(device);
            meaDevice.setSubDie(subDie);
            meaDevice.setDeviceGroup(deviceGroup);
            deviceGroup.addDevice(meaDevice);

            CheckableTreeNode subDieNode = CheckableTreeUtil.getTreeNode(tree, subDie);
            if (subDieNode != null) {
                CheckableTreeNode groupNode = new CheckableTreeNode(deviceGroup.getNameByDeviceType(), deviceGroup);
                groupNode.setCheckable(false);
                groupNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/devicegroup.png")));
                subDieNode.add(groupNode);
                treeModel.reload(subDieNode);
                tree.updateUI();
                tree.setSelectionPath(new TreePath(groupNode.getPath()));
            }
        }
    }

    private JPanel getToolbarPanel() {
        JButton addButton = new JButton();
        addButton.setAction(addAction);
        addButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/add.gif")));
        addButton.setText("");
        addAction.putValue(Action.SHORT_DESCRIPTION, "Add");

        JButton removeButton = new JButton();
        removeButton.setAction(removeAction);
        removeButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/remove.gif")));
        removeButton.setText("");
        removeAction.putValue(Action.SHORT_DESCRIPTION, "Remove");

        JButton loadButton = new JButton();
        loadButton.setAction(loadAction);
        loadButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/import.png")));
        loadButton.setText("");
        loadAction.putValue(Action.SHORT_DESCRIPTION, "Load...");

        JButton dumpButton = new JButton();
        dumpButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/dump.png")));
        dumpButton.setText("");
        dumpButton.setToolTipText("Export...");
        dumpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                File file = LoadSaveUtil.saveFile("Export Spec Pattern", JFileChooser.FILES_ONLY, ExtractionProject.KEY_DIR_INI_SPEC_SAVE);
                final File file = LoadSaveUtil.saveFile("Export Device", JFileChooser.FILES_ONLY, KEY, null, new BasicFileFilter("ini"), "pattern.ini", false);
                if (file != null) {
                    try {
                    } catch (Exception ex) {
                        Logger.getLogger(DeviceManagePanel.class.getName()).severe("Failed to export device");
                    }
                }
            }
        });

        JPanel toolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.LEADING, addButton, removeButton, null, loadButton, dumpButton);
        return toolbarPanel;
    }

    @Override
    public void buildTree() {
        int subNumber = meaDie.getSubDieNumber();
        for (int i = 0; i < subNumber; i++) {
            MeaSubDie subDie = meaDie.getSubDie(i);
            CheckableTreeNode subDieNode = new CheckableTreeNode(subDie.toString(), subDie);
            subDieNode.setCheckable(false);
            subDieNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/subdie.png")));
            rootNode.add(subDieNode);

            int deviceGroupNumber = subDie.getGroupNumber();
            for (int j = 0; j < deviceGroupNumber; j++) {
                MeaDeviceGroup deviceGroup = subDie.getGroup(j);
                CheckableTreeNode groupNode = new CheckableTreeNode(deviceGroup.getNameByDeviceType(), deviceGroup);
                groupNode.setCheckable(false);
                groupNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/devicegroup.png")));
                subDieNode.add(groupNode);
            }
        }
    }

    private void buildDieTree() {
        WaferInfo waferInfo = meaSpace.getWaferInfo();
        int dieNumber = waferInfo.getDieNumber();
        for (int dieIndex = 0; dieIndex < dieNumber; dieIndex++) {
            WaferDieInfo dieInfo = waferInfo.getDieInfo(dieIndex);
            CheckableTreeNode dieNode = new CheckableTreeNode(dieInfo.toString(), dieInfo);
            dieNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/die.png")));
            dieRootNode.add(dieNode);
        }
    }

    @Override
    public void updateContent(Object lastPathComponent) {
        if (meaEditor != null) {
            meaEditor.stopEditing();
        }
        contentPanel.removeAll();
        meaEditor = null;
        if (lastPathComponent != null && lastPathComponent instanceof CheckableTreeNode) {
            CheckableTreeNode treeNode = (CheckableTreeNode) lastPathComponent;
            if (treeNode.isLeaf()) {
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof MeaDeviceGroup) {
                    MeaDeviceGroup deviceGroup = (MeaDeviceGroup) obj;
                    MeaSubDie subDie = (MeaSubDie) ((CheckableTreeNode) treeNode.getParent()).getAssociatedObject();
//                    DeviceGroupPanel deviceGroupPanel = new DeviceGroupPanel(meaSpace, meaDie, subDie, deviceGroup);
//                    DeviceGroupPanel1 deviceGroupPanel = new DeviceGroupPanel1(meaSpace, meaDie, subDie, deviceGroup);
                    DeviceGroupPanel deviceGroupPanel = new DeviceGroupPanel(meaSpace, meaDie, subDie, deviceGroup);
                    contentPanel.add(deviceGroupPanel, BorderLayout.CENTER);
                    meaEditor = deviceGroupPanel;
                }
            }
        }
        contentPanel.updateUI();
    }

    @Override
    public void treeNodeChecked(CheckableTreeNode node, boolean checked) {
        // do nothing
    }

    public void stopEditing() {
        if (meaEditor != null) {
            meaEditor.stopEditing();
        }
    }
}
