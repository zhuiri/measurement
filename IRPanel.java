/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceTagImpl;
import com.platformda.datacore.DeviceType;
import com.platformda.datacore.DeviceTypeManager;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.datacore.pattern.EntityPagePatternGroup;
import com.platformda.datacore.pattern.PagePatternLoader;
import com.platformda.iv.MeaSpace;
import com.platformda.iv.api.Creater;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.MassEditor;
import com.platformda.iv.api.MatrixBondGroup;
import com.platformda.iv.api.MeaEditor;
import com.platformda.iv.api.ProfileEditor;
//import com.platformda.iv.deviceimpl.CustomDeviceType;
//import com.platformda.mea.deviceimpl.CustomEditPanel;
import com.platformda.iv.measure.DeviceBond;
//import com.platformda.iv.measure.StressPattern;
import com.platformda.spec.SpecPattern;
import com.platformda.spec.SpecPatternGroup;
import com.platformda.spec.SpecPatternLoader;
import com.platformda.system.JdkZipUtil;
import com.platformda.utility.common.BasicFileFilter;
import com.platformda.utility.common.FileUtil;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.utility.common.PathUtil;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.tree.CheckableTreeUtil;
import com.platformda.utility.ui.GUIUtil;
import com.platformda.utility.ui.JTreeUtil;
import com.platformda.utility.ui.TreePanel;
import com.platformda.utility.zip.NetBeansZipUtil;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * Instrument And Measurement Panel
 *
 * @author Junyi
 */
public class IRPanel extends TreePanel implements MeaEditor {

    public static final String KEY = "Instrument";
    public static final String KEY_PATTERN_PAGE = "PagePattern";
    public static final String KEY_PATTERN_SPEC = "SpecPattern";
    InstrumentManager instrumentManager = InstrumentManager.getInstance();
    RoutinePatternManager routinePatternManager = RoutinePatternManager.getInstance();
    MeaDeviceTypeManager deviceTypeManager = MeaDeviceTypeManager.getInstance();
    RoutineManager routineManager = RoutineManager.getInstance();
    //
    MassEditor massEditor = new MassEditor();
    // 
    DeviceTag deviceTag = DeviceTypeManager.getDeviceTagByAbbreviation("nmosfet");
    Action addAction = new AbstractAction("Add...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
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
                if (obj != null && obj instanceof Routine) {
                    Routine routine = (Routine) obj;
                    String msg = "Do you want to remove selected routine?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove Routine", NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                    routineManager.removeRoutine(routine);
                    CheckableTreeNode parentNode = (CheckableTreeNode) treeNode.getParent();
//                    treeNode.removeFromParent();
                    treeModel.removeNodeFromParent(treeNode);
                    Object parentObj = parentNode.getAssociatedObject();
                    if (parentObj != null && parentObj instanceof DeviceType) {
                        // ignore
                    } else {
                        // group node
                        if (parentNode.getChildCount() == 0) {
                            treeModel.removeNodeFromParent(parentNode);
                        }
                    }
                    tree.updateUI();
                    massEditor.clear();
                    updateContent(null);
                } else if (obj != null && obj instanceof EntityInstrument) {
                    EntityInstrument ei = (EntityInstrument) obj;
                    String msg = "Do you want to remove selected instrument?";
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove Instrument", NotifyDescriptor.YES_NO_OPTION);
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        return;
                    }
                    instrumentManager.removeInstrument(ei);
                    treeModel.removeNodeFromParent(treeNode);
                    tree.updateUI();
                    massEditor.clear();
                    updateContent(null);
                } else if (obj != null && obj instanceof CustomDeviceType) {
                    CustomDeviceType customDeviceType = (CustomDeviceType) obj;
                    // remove from devicetypeManager
                    // remove from RoutinePatternManager
                    // remove all routines
                    deviceTypeManager.removeCustomDeviceType(customDeviceType);
                    routinePatternManager.removeRoutinePatternByDeviceType(customDeviceType);
                    routineManager.removeRoutinesByDeviceType(customDeviceType);

                    treeModel.removeNodeFromParent(treeNode);
                    CheckableTreeNode deviceTypeRoutineNode = CheckableTreeUtil.getTreeNode(routineRootNode, customDeviceType);
                    if (deviceTypeRoutineNode != null) {
                        treeModel.removeNodeFromParent(deviceTypeRoutineNode);
                    }
                    tree.updateUI();
                }
            }
        }
    };
    Action enableAction = new AbstractAction("Enable") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof EntityInstrument) {
                    // TODO:
                }
            }
        }
    };
    Action disableAction = new AbstractAction("Disable") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof EntityInstrument) {
                    // TODO:
                }
            }
        }
    };
    JButton mergeButton = new JButton();
    JButton loadButton = new JButton();
    JButton dumpButton = new JButton();
    JButton revertButton = new JButton();
    Action mergeAction = new AbstractAction("Load And Merge Settings...") {
        @Override
        public void actionPerformed(ActionEvent e) {
            File file = LoadSaveUtil.openFile("Load And Merge Settings", JFileChooser.FILES_ONLY, "IM");
            if (file != null) {
                backupSettings();
                updateRevertStatus();
                try {
                    File tempDirFile = FileUtil.createTempDirectory("InstAndMea", PathUtil.getTempPath());
                    final String tempDir = tempDirFile.getAbsolutePath();
                    try {
                        NetBeansZipUtil.unpack(file, tempDirFile);

                        MeaDeviceTypeManager edm = MeaDeviceTypeManager.getEmptyInstance();
                        InstrumentManager eim = InstrumentManager.getEmptyInstance();
                        RoutinePatternManager erpm = RoutinePatternManager.getEmptyInstance();
                        RoutineManager erm = RoutineManager.getEmptyInstance();

                        edm.load(tempDir);
                        eim.load(tempDir);
                        erpm.load(tempDir, eim, edm);
                        erm.load(tempDir, erpm, edm);

                        // start to merge
                        MeaDeviceTypeManager dm = MeaDeviceTypeManager.getInstance();
                        InstrumentManager im = InstrumentManager.getInstance();
                        RoutinePatternManager rpm = RoutinePatternManager.getInstance();
                        RoutineManager rm = RoutineManager.getInstance();

                        // TODO: device type
                        List<DeviceType> edeviceTypes = edm.getAllDeviceTypes();
                        List<DeviceType> deviceTypes = dm.getAllDeviceTypes();
                        for (DeviceType edeviceType : edeviceTypes) {
                            if (edeviceType instanceof CustomDeviceType) {
                                CustomDeviceType eCustomDeviceType = (CustomDeviceType) edeviceType;
                                CustomDeviceType customDeviceType = (CustomDeviceType) dm.getDeviceType(eCustomDeviceType.getName());
                                if (customDeviceType == null) {
                                    dm.addCustomDeviceType(eCustomDeviceType);
                                } else {
                                    customDeviceType.copyValueOf(eCustomDeviceType);
                                }
                            }
                        }

                        // instrument,
                        im.setGpibBoardId(eim.getGpibBoardId());
                        List<EntityInstrument> einsts = eim.getInstruments();
                        List<EntityInstrument> insts = im.getInstruments();

                        Map<EntityInstrument, MatrixBondGroup> matrixBondMap = im.getMatrixBondMap();
                        for (EntityInstrument einst : einsts) {
                            String name = einst.getInstrumentName();
                            EntityInstrument inst = im.getInstrument(name);
                            if (inst == null) {
                                insts.add(einst);
                            } else {
                                MatrixBondGroup matrixBondGroup = im.getMatrixBondGroup(inst);
                                if (matrixBondGroup != null) {
                                    matrixBondMap.remove(inst);
                                }
                                int index = insts.indexOf(inst);
                                insts.set(index, einst);

                                // update matrix bond
                                for (Map.Entry<EntityInstrument, MatrixBondGroup> entry : matrixBondMap.entrySet()) {
//                                    EntityInstrument entityInstrument = entry.getKey();
                                    MatrixBondGroup bondGroup = entry.getValue();
                                    bondGroup.replaceInstrument(inst, einst);
                                }
                                matrixBondGroup = eim.getMatrixBondGroup(einst);
                                if (matrixBondGroup != null) {
                                    matrixBondMap.put(einst, matrixBondGroup);
                                }
                            }
                        }

                        // routine pattern
                        List<RoutinePattern> eRoutinePatterns = erpm.getPatterns();
                        for (RoutinePattern eRoutinePattern : eRoutinePatterns) {
                            RoutinePattern routinePattern = rpm.getRoutinePattern(eRoutinePattern.getDeviceType());
                            // page patterns
                            List<EntityPagePattern> ePagePatterns = eRoutinePattern.getPagePatterns();
                            List<EntityPagePattern> pagePatterns = routinePattern.getPagePatterns();
                            for (EntityPagePattern ePagePattern : ePagePatterns) {
                                EntityPagePattern pagePattern = routinePattern.getPagePattern(ePagePattern.getName());
                                if (pagePattern == null) {
                                    // 1. not exist in previous version
                                    // add to previous version
                                    // TODO: DeviceBond
                                    routinePattern.addPagePattern(pagePattern);
                                } else {
                                    // 2. exist in previous version
                                    // ignore or replace?
                                    // replace
                                    int index = pagePatterns.indexOf(pagePattern);
                                    pagePatterns.set(index, ePagePattern);
                                }
                                // 3. not touch page patterns in previous version but not in loaded version
                            }
                            // spec patterns
                            List<SpecPattern> eSpecPatterns = eRoutinePattern.getSpecPatterns();
                            List<SpecPattern> specPatterns = routinePattern.getSpecPatterns();
                            for (SpecPattern eSpecPattern : eSpecPatterns) {
                                SpecPattern specPattern = routinePattern.getSpecPatternByName(eSpecPattern.getName());
                                if (specPattern == null) {
                                    specPatterns.add(eSpecPattern);
                                } else {
                                    int index = specPatterns.indexOf(specPattern);
                                    specPatterns.set(index, eSpecPattern);
                                }
                            }
                            // device bonds
                            List<DeviceBond> eDeviceBonds = eRoutinePattern.getDeviceBonds();
                            List<DeviceBond> deviceBonds = routinePattern.getDeviceBonds();
                            Map<String, DeviceBond> eBondMap = eRoutinePattern.getBondMap();
                            Map<String, DeviceBond> bondMap = routinePattern.getBondMap();
                            for (DeviceBond eDeviceBond : eDeviceBonds) {
                                DeviceBond deviceBond = routinePattern.getDeviceBondByName(eDeviceBond.getName());
                                if (deviceBond == null) {
                                    deviceBonds.add(eDeviceBond);
                                } else {
                                    int index = deviceBonds.indexOf(deviceBond);
                                    deviceBonds.set(index, eDeviceBond);
                                    List<String> matchingKeys = new ArrayList<String>();
                                    for (Map.Entry<String, DeviceBond> entry : bondMap.entrySet()) {
                                        String key = entry.getKey();
                                        DeviceBond bond = entry.getValue();
                                        if (bond == deviceBond) {
                                            matchingKeys.add(key);
                                        }
                                    }
                                    for (String key : matchingKeys) {
                                        bondMap.put(key, eDeviceBond);
                                    }
                                }
                            }
                            for (Map.Entry<String, DeviceBond> entry : eBondMap.entrySet()) {
                                String key = entry.getKey();
                                DeviceBond deviceBond = entry.getValue();
                                bondMap.put(key, deviceBond);
                            }
                        }

                        // routine
                        Map<DeviceType, List<Routine>> eRoutineMap = erm.getRoutineMap();
                        Map<DeviceType, List<Routine>> routineMap = rm.getRoutineMap();
                        for (Map.Entry<DeviceType, List<Routine>> entry : eRoutineMap.entrySet()) {
                            DeviceType deviceType = entry.getKey();
                            List<Routine> eRoutines = entry.getValue();
                            List<Routine> routines = routineMap.get(deviceType);
                            if (routines == null) {
                                routineMap.put(deviceType, eRoutines);
                                continue;
                            }
                            for (Routine eRoutine : eRoutines) {
                                String name = eRoutine.getName();
                                String eGroup = eRoutine.getGroup();

                                int index = -1;
                                for (int i = 0; i < routines.size(); i++) {
                                    Routine routine = routines.get(i);
                                    String group = routine.getGroup();
                                    if (name.equalsIgnoreCase(routine.getName())) {
                                        if ((group == null || group.equalsIgnoreCase("Default"))
                                                && (eGroup == null || eGroup.equalsIgnoreCase("Default"))) {
                                            index = i;
                                            break;
                                        } else if (group != null && group.equalsIgnoreCase(eGroup)) {
                                            index = i;
                                            break;
                                        }
                                    }
                                }
                                if (index < 0) {
                                    routines.add(eRoutine);
                                } else {
                                    routines.set(index, eRoutine);
                                }
                            }
                        }
                        rootNode.removeAllChildren();
                        buildTree();
                        treeModel.reload();
                        JTreeUtil.expandTree(tree);
                        tree.updateUI();
                        massEditor.clear();
                        updateContent(null);
                    } finally {
                        FileUtil.removeDir(tempDir);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    };
    Action loadAction = new AbstractAction("Load...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            // spec pattern or device page pattern
            File file = LoadSaveUtil.openFile("Load Settings", JFileChooser.FILES_ONLY, "IM");
            if (file != null) {
                backupSettings();
                updateRevertStatus();
                try {
                    loadSettings(file);

                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                massEditor.clear();
                updateContent(null);
            }
        }
    };
    Action exportAction = new AbstractAction("Export...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            final File file = LoadSaveUtil.saveFile("Export Settings", JFileChooser.FILES_ONLY, "IM", null, new BasicFileFilter("zip"), "settings.zip", false);
            if (file != null) {
                try {
                    saveSettings(file);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    Logger.getLogger(IRPanel.class.getName()).severe("Failed to export settings");
                }
            }
        }
    };
    Action revertAction = new AbstractAction("Revert") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            File file = new File(PathUtil.getTempPath(), "lastSettings.zip");
            if (file.exists()) {
                String msg = "Do you want to revert to last settings?";
                NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Revert Settings", NotifyDescriptor.YES_NO_OPTION);
                Object result = DialogDisplayer.getDefault().notify(nd);
                if (result != NotifyDescriptor.YES_OPTION) {
                    return;
                }
                try {
                    loadSettings(file);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                massEditor.clear();
                updateContent(null);
                file.renameTo(new File(PathUtil.getTempPath(), "lastSettings_reverted.zip"));
                updateRevertStatus();
            }
        }
    };

    void saveSettings(File file) throws IOException, Exception {
        File tempDirFile = FileUtil.createTempDirectory("InstAndMea", PathUtil.getTempPath());
        final String tempDir = tempDirFile.getAbsolutePath();
        MeaDeviceTypeManager.getInstance().save(tempDir);
        InstrumentManager.getInstance().save(tempDir);
        RoutinePatternManager.getInstance().save(tempDir);
        RoutineManager.getInstance().save(tempDir);
        JdkZipUtil.makeZip(tempDirFile.listFiles(), file.getAbsolutePath());
        FileUtil.removeDir(tempDir);
    }

    void loadSettings(File file) throws IOException, Exception {
        File tempDirFile = FileUtil.createTempDirectory("InstAndMea", PathUtil.getTempPath());
        final String tempDir = tempDirFile.getAbsolutePath();
        try {
            NetBeansZipUtil.unpack(file, tempDirFile);
            MeaDeviceTypeManager dm = MeaDeviceTypeManager.getInstance();
            InstrumentManager im = InstrumentManager.getInstance();
            RoutinePatternManager rpm = RoutinePatternManager.getInstance();
            RoutineManager rm = RoutineManager.getInstance();

            String configPath = PathUtil.getConfigPath();
            dm.clear(configPath);
            im.clear();
            rpm.clear();
            rm.clear(configPath);

            dm.load(tempDir);
            im.load(tempDir);
            rpm.load(tempDir, im, dm);
            rm.load(tempDir, rpm, dm);

            rootNode.removeAllChildren();
            buildTree();
            treeModel.reload();
            JTreeUtil.expandTree(tree);
            tree.updateUI();


        } finally {
            FileUtil.removeDir(tempDir);
        }
    }

    void restorSettings() {
        File file = new File(PathUtil.getTempPath(), "lastSettings.zip");
        if (file.exists()) {
            try {
                loadSettings(file);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    void backupSettings() {
        File file = new File(PathUtil.getTempPath(), "lastSettings.zip");
        try {
            saveSettings(file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void updateRevertStatus() {
        File file = new File(PathUtil.getTempPath(), "lastSettings.zip");
        if (file.exists()) {
            revertButton.setEnabled(true);
        } else {
            revertButton.setEnabled(false);
        }
    }
    Action loadPagePattern = new AbstractAction("Load Page Pattern...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            // spec pattern or device page pattern
            String defaultPath = null;
            File defaultSettingsFile = InstalledFileLocator.getDefault().locate("etc", MeaSpace.CODE_BASE, false);
            if (defaultSettingsFile != null) {
                defaultPath = defaultSettingsFile.getAbsolutePath();
            }
            File file = LoadSaveUtil.openFile("Load Page Pattern", JFileChooser.FILES_ONLY, KEY_PATTERN_PAGE, defaultPath, false);
            if (file != null) {
                loadPagePattern(file.getAbsolutePath());
            }
        }
    };
    Action loadSpecPattern = new AbstractAction("Load Spec Pattern...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            // spec pattern or device page pattern
            String defaultPath = null;
            File defaultSettingsFile = InstalledFileLocator.getDefault().locate("etc", MeaSpace.CODE_BASE, false);
            if (defaultSettingsFile != null) {
                defaultPath = defaultSettingsFile.getAbsolutePath();
            }
            File file = LoadSaveUtil.openFile("Load Spec Pattern", JFileChooser.FILES_ONLY, KEY_PATTERN_SPEC, defaultPath, false);
            if (file != null) {
                loadSpecPattern(file.getAbsolutePath());
            }
        }
    };
    Action loadPagePatternOnDeviceType = new AbstractAction("Load Page Pattern...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof DeviceType) {
                    if (treeNode.getParent() == patternRootNode) {
                        DeviceType deviceType = (DeviceType) obj;
                        String defaultPath = null;
                        File defaultSettingsFile = InstalledFileLocator.getDefault().locate("etc", MeaSpace.CODE_BASE, false);
                        if (defaultSettingsFile != null) {
                            defaultPath = defaultSettingsFile.getAbsolutePath();
                        }
                        File file = LoadSaveUtil.openFile("Load Page Pattern", JFileChooser.FILES_ONLY, KEY_PATTERN_PAGE, defaultPath, false);
                        if (file != null) {
                            EntityPagePatternGroup patternGroup = null;
                            try {
                                PagePatternLoader loader = new PagePatternLoader();
                                DeviceTag dt = new DeviceTagImpl(deviceType, null);

                                patternGroup = loader.load(file.getAbsolutePath(), dt);
                                RoutinePattern routinePattern = routinePatternManager.getRoutinePattern(deviceType);
                                routinePattern.loadPagePattern(patternGroup);
                                updateContent(treeNode);
//                                refreshPatternRootNode();
//                                treeModel.reload(patternRootNode);
//                                tree.updateUI();
                            } catch (Exception ex) {
                                StatusDisplayer.getDefault().setStatusText("Failed to load page patterns.");
                            }
                        }
                    }
                }
            }
        }
    };
    Action loadSpecPatternOnDeviceType = new AbstractAction("Load Spec Pattern...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof DeviceType) {
                    if (treeNode.getParent() == patternRootNode) {
                        DeviceType deviceType = (DeviceType) obj;

                        String defaultPath = null;
                        File defaultSettingsFile = InstalledFileLocator.getDefault().locate("etc", MeaSpace.CODE_BASE, false);
                        if (defaultSettingsFile != null) {
                            defaultPath = defaultSettingsFile.getAbsolutePath();
                        }
                        File file = LoadSaveUtil.openFile("Load Spec Pattern", JFileChooser.FILES_ONLY, KEY_PATTERN_SPEC, defaultPath, false);
                        if (file != null) {
                            SpecPatternGroup patternGroup = null;
                            try {
                                patternGroup = SpecPatternLoader.load(deviceTag.getDeviceType(), file.getAbsolutePath());
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                                StatusDisplayer.getDefault().setStatusText("Failed to load spec patterns.");
                            }
                            if (patternGroup == null) {
                                return;
                            }

                            RoutinePattern routinePattern = routinePatternManager.getRoutinePattern(deviceType);
                            routinePattern.loadSpecPattern(patternGroup);
                            updateContent(treeNode);
//                                refreshPatternRootNode();
//                                treeModel.reload(patternRootNode);
//                                tree.updateUI();
                        }
                    }
                }
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
                if (obj != null && obj instanceof Routine) {
                    Routine routine = (Routine) obj;
                    NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                            "Name: ",
                            "Rename",
                            NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.PLAIN_MESSAGE);
                    nd.setInputText(routine.getName());
                    Object result = DialogDisplayer.getDefault().notify(nd);
                    if (result.equals(NotifyDescriptor.OK_OPTION)) {
                        String name = nd.getInputText();
                        routine.setName(name);
                        treeNode.setUserObject(name);
                        // repaint doesn't work!!!
                        tree.updateUI();
                    }
                } else if (obj != null && obj instanceof EntityInstrument) {
                    // can't rename
//                    EntityInstrument ei = (EntityInstrument) obj;
//                    NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
//                            "Name: ",
//                            "Rename",
//                            NotifyDescriptor.OK_CANCEL_OPTION,
//                            NotifyDescriptor.PLAIN_MESSAGE);
//                    nd.setInputText(ei.getInstrumentName());
//                    Object result = DialogDisplayer.getDefault().notify(nd);
//                    if (result.equals(NotifyDescriptor.OK_OPTION)) {
//                        String name = nd.getInputText();
//                        treeNode.setUserObject(name);
//                        tree.repaint();
//                    }
                }
            }
        }
    };

    @Override
    public void stopEditing() {
        massEditor.stopEditing();
        massEditor.clear();
    }

//            File file = LoadSaveUtil.openFile("Load Page Pattern", JFileChooser.FILES_ONLY, KEY_PATTERN);
    class AddInstrumentAction extends AbstractAction {

        Creater creater;
        String name;
        CheckableTreeNode treeNode;

        public AddInstrumentAction(Creater creater, String name, CheckableTreeNode treeNode) {
            super("Add " + name);
            this.creater = creater;
            this.name = name;
            this.treeNode = treeNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addInstrument(creater, name, treeNode);
        }
    }

    class AddDeviceTypeAction extends AbstractAction {

        DeviceType deviceType;

        public AddDeviceTypeAction() {
            super("Add Custom Device Type");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addDeviceType();

        }
    }

    class EditDeviceTypeAction extends AbstractAction {

        public EditDeviceTypeAction() {
            super("Edit...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof CustomDeviceType) {
                    CustomDeviceType customDeviceType = (CustomDeviceType) obj;
                    CustomEditPanel form = new CustomEditPanel(customDeviceType);
                    String preName = customDeviceType.getName();

                    DialogDescriptor desc = new DialogDescriptor(form,
                            "Edit Custom Device Type", true, DialogDescriptor.OK_CANCEL_OPTION,
                            DialogDescriptor.OK_OPTION, null);
                    Object result = DialogDisplayer.getDefault().notify(desc);
                    if (result.equals(NotifyDescriptor.OK_OPTION)) {
                        form.stopEditing();
                        String name = customDeviceType.getName();
                        if (!preName.equalsIgnoreCase(name)) {
                            treeNode.setUserObject(customDeviceType.getName());
                            // repaint doesn't work!!!
                            tree.updateUI();
                        }
                    }
                }
            }
        }
    }

    class AddRoutineAction extends AbstractAction {

        DeviceType deviceType;
        CheckableTreeNode treeNode;

        public AddRoutineAction(DeviceType deviceType, CheckableTreeNode treeNode) {
            super("Add Routine");
            this.deviceType = deviceType;
            this.treeNode = treeNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addRoutine(deviceType, treeNode);
        }
    }

    class RemoveRoutinesAction extends AbstractAction {

        CheckableTreeNode treeNode;
        DeviceType deviceType;
        boolean removeAllRoutines = false;
        boolean removeRoutineGroup = false;

        public RemoveRoutinesAction(CheckableTreeNode treeNode, DeviceType deviceType, boolean removeAllRoutines, boolean removeRoutineGroup) {
            super("Remove Routines...");
            this.treeNode = treeNode;
            this.deviceType = deviceType;
            this.removeAllRoutines = removeAllRoutines;
            this.removeRoutineGroup = removeRoutineGroup;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = null;
            if (removeAllRoutines) {
                msg = "Do you want to remove all routines?";
            } else if (removeRoutineGroup) {
                msg = "Do you want to remove all routines of selected group?";
            } else {
                msg = "Do you want to remove all routines of " + deviceType.getName() + "?";
            }

            String groupName = null;
            Object obj = treeNode.getAssociatedObject();
            if (removeRoutineGroup && obj != null) {
                groupName = obj.toString();
            }

            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, "Remove Routines", NotifyDescriptor.YES_NO_OPTION);
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (result == NotifyDescriptor.YES_OPTION) {
                if (removeAllRoutines) {
                    routineManager.clear();
                    refreshRoutineRootNode();
                    treeModel.reload(treeNode);
                } else if (removeRoutineGroup) {
                    routineManager.removeRoutines(deviceType, groupName);
                    treeModel.removeNodeFromParent(treeNode);
                    tree.updateUI();
                } else {
                    routineManager.removeRoutines(deviceType);
                    refreshRoutineNode(treeNode, deviceType);
                    treeModel.reload(treeNode);
                }
            }
        }
    }

    public IRPanel() {
        initComponents(180);
        JPanel toolbarPanel = getToolbarPanel();
        leftPanel.add(toolbarPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(920, 560));

        tree.setToggleClickCount(-1);
        initActions();
        updateRevertStatus();
    }

    private void initActions() {
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
                    if (treeNode == patternRootNode) {
                        JPopupMenu popupMenu = new JPopupMenu();
//                        popupMenu.add(loadPagePattern);
//                        popupMenu.add(loadSpecPattern);
                        popupMenu.add(new AddDeviceTypeAction());
                        popupMenu.show(tree, e.getX(), e.getY());
                    } else if (treeNode == routineRootNode) {
                        if (routineManager.hasRoutine()) {
                            JPopupMenu popupMenu = new JPopupMenu();
                            popupMenu.add(new RemoveRoutinesAction(treeNode, null, true, false));
                            popupMenu.show(tree, e.getX(), e.getY());
                        }
                    } else if (obj != null && obj instanceof Integer) {
                        Integer type = (Integer) obj;
                        List<Creater> drivers = CreaterManager.getCreaters(type.intValue());
                        JPopupMenu popupMenu = new JPopupMenu();
                        for (Creater driver : drivers) {
                            String[] names = driver.getNames();
                            for (String name : names) {
                                AddInstrumentAction action = new AddInstrumentAction(driver, name, treeNode);
                                popupMenu.add(action);
                            }
                        }
                        popupMenu.show(tree, e.getX(), e.getY());
                    } else if (obj != null && obj instanceof DeviceType) {
                        DeviceType deviceType = (DeviceType) obj;
                        if (treeNode.getParent() == patternRootNode) {
                            JPopupMenu popupMenu = new JPopupMenu();
                            if (deviceType instanceof CustomDeviceType) {
                                popupMenu.add(new EditDeviceTypeAction());
                                popupMenu.add(removeAction);
                                popupMenu.addSeparator();
                            }
                            popupMenu.add(loadPagePatternOnDeviceType);
                            popupMenu.add(loadSpecPatternOnDeviceType);
                            popupMenu.show(tree, e.getX(), e.getY());
                        } else if (treeNode.getParent() == routineRootNode) {
                            JPopupMenu popupMenu = new JPopupMenu();
                            popupMenu.add(new AddRoutineAction(deviceType, treeNode));
                            if (routineManager.hasRoutine(deviceType)) {
                                popupMenu.add(new RemoveRoutinesAction(treeNode, deviceType, false, false));
                            }
                            popupMenu.show(tree, e.getX(), e.getY());
                        }
                    } else if (obj != null && obj instanceof String) {
                        if (treeNode.getParent() != null && treeNode.getParent().getParent() == routineRootNode) {
                            JPopupMenu popupMenu = new JPopupMenu();
                            DeviceType deviceType = (DeviceType) ((CheckableTreeNode) treeNode.getParent()).getAssociatedObject();
                            popupMenu.add(new AddRoutineAction(deviceType, treeNode));
                            popupMenu.add(new RemoveRoutinesAction(treeNode, deviceType, false, true));
                            popupMenu.show(tree, e.getX(), e.getY());
                        }
                    } else if (obj != null && obj instanceof Routine) {
//                        Routine routine = (Routine) obj;
                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.add(renameAction);
                        popupMenu.add(removeAction);
                        popupMenu.show(tree, e.getX(), e.getY());
                    } else if (obj != null && obj instanceof EntityInstrument) {
//                        Routine routine = (Routine) obj;
                        JPopupMenu popupMenu = new JPopupMenu();
//                        popupMenu.add(renameAction);
                        popupMenu.add(removeAction);
                        popupMenu.show(tree, e.getX(), e.getY());
                    }
                }
            }
        };
        tree.addMouseListener(mouseListener);
    }

    private JPanel getToolbarPanel() {
//        JButton addButton = new JButton();
//        addButton.setAction(addAction);
//        addButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
//                "com/platformda/mea/resources/add.gif")));
//        addButton.setText("");
//        addAction.putValue(Action.SHORT_DESCRIPTION, "Add");
//
//        JButton removeButton = new JButton();
//        removeButton.setAction(removeAction);
//        removeButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
//                "com/platformda/mea/resources/remove.gif")));
//        removeButton.setText("");
//        removeAction.putValue(Action.SHORT_DESCRIPTION, "Remove");

        Image loadImage = ImageUtilities.loadImage("com/platformda/mea/resources/import.png");
        Image incBadgeImage = ImageUtilities.loadImage("com/platformda/mea/resources/badgeLocModified.gif");
        Image mergeImage = ImageUtilities.mergeImages(loadImage, incBadgeImage, 8, 0);
        Image dumpImage = ImageUtilities.loadImage("com/platformda/mea/resources/dump.png");
        Image revertImage = ImageUtilities.loadImage("com/platformda/mea/resources/revert.png");

        mergeButton.setAction(mergeAction);
        mergeButton.setIcon(new ImageIcon(mergeImage));
        mergeButton.setText("");
        mergeAction.putValue(Action.SHORT_DESCRIPTION, "Load And Merge Settings...");

        loadButton.setAction(loadAction);
        loadButton.setIcon(new ImageIcon(loadImage));
        loadButton.setText("");
        loadAction.putValue(Action.SHORT_DESCRIPTION, "Load Settings...");

        dumpButton.setAction(exportAction);
        dumpButton.setIcon(new ImageIcon(dumpImage));
        dumpButton.setText("");
        exportAction.putValue(Action.SHORT_DESCRIPTION, "Export Settings...");

        revertButton.setAction(revertAction);
        revertButton.setIcon(new ImageIcon(revertImage));
        revertButton.setText("");
        revertAction.putValue(Action.SHORT_DESCRIPTION, "Revert Settings...");

        JPanel toolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, mergeButton, loadButton, dumpButton, revertButton);
        return toolbarPanel;
    }
    CheckableTreeNode[] typeNodes;
    CheckableTreeNode instrumentNode;
    CheckableTreeNode patternRootNode;
    CheckableTreeNode routineRootNode;

    public void addDeviceType() {
        CustomDeviceType deviceType = new CustomDeviceType();
        CustomEditPanel form = new CustomEditPanel(deviceType);

        DialogDescriptor desc = new DialogDescriptor(form,
                "Add Custom Device Type", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc);
        if (result.equals(NotifyDescriptor.OK_OPTION)) {
            form.stopEditing();
            deviceTypeManager.addCustomDeviceType(deviceType);
            CheckableTreeNode typeNode = new CheckableTreeNode(deviceType.getName(), deviceType);
            patternRootNode.add(typeNode);
            treeModel.reload(patternRootNode);

            CheckableTreeNode patternByTypeNode = new CheckableTreeNode(deviceType.getName(), deviceType);
            routineRootNode.add(patternByTypeNode);
            refreshPatternNode(patternByTypeNode, deviceType);
            treeModel.reload(routineRootNode);

            tree.updateUI();
            tree.setSelectionPath(new TreePath(typeNode.getPath()));
        }
    }

    public void addInstrument(Creater creater, String name, CheckableTreeNode treeNode) {
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
                "Name: ",
                "Add " + name,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE);
        nd.setInputText(name);
        Object result = DialogDisplayer.getDefault().notify(nd);
        if (result.equals(NotifyDescriptor.OK_OPTION)) {
            String instName = nd.getInputText();
            EntityInstrument instrument = new EntityInstrument(creater, instName, name);
            instrumentManager.addInstrument(instrument);
            CheckableTreeNode instNode = new CheckableTreeNode(instName, instrument);
            treeNode.add(instNode);
            treeModel.reload(instNode);
            tree.updateUI();
            tree.setSelectionPath(new TreePath(instNode.getPath()));
        }
    }

    public void addRoutine(DeviceType deviceType, CheckableTreeNode treeNode) {
//        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(
//                "Name: ",
//                "Add Routine",
//                NotifyDescriptor.OK_CANCEL_OPTION,
//                NotifyDescriptor.PLAIN_MESSAGE);
//        nd.setInputText("routine");

        String groupName = "Default";
        Object obj = treeNode.getAssociatedObject();
        if (obj != null && obj instanceof String) {
            groupName = obj.toString();
        }
        JTextField groupField = new JTextField(groupName, 30);
        JTextField nameField = new JTextField("routine", 30);

        JPanel westPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        westPanel.add(new JLabel("Group: "));
        westPanel.add(new JLabel("Name: "));

        JPanel fieldPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        fieldPanel.add(groupField);
        fieldPanel.add(nameField);

        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(westPanel, BorderLayout.WEST);
        namePanel.add(fieldPanel, BorderLayout.CENTER);

        JCheckBox stressBox = new JCheckBox("Stress");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(stressBox, BorderLayout.SOUTH);
        panel.add(namePanel, BorderLayout.CENTER);

        GUIUtil.setEmptyBorder(panel, 5);

        DialogDescriptor desc = new DialogDescriptor(panel,
                "Add Routine", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc);
        if (result.equals(NotifyDescriptor.OK_OPTION)) {
//            String routineName = nd.getInputText();
            groupName = groupField.getText().trim();
            String routineName = nameField.getText().trim();
            Routine routine = new Routine(deviceType);
            routine.setName(routineName);
            routine.setGroup(groupName);
            routine.setDeviceType(deviceType);
            routine.setId(routineManager.getNextRoutineId());
            if (stressBox.isSelected()) {
                StressPattern stressPattern = new StressPattern(new String[0], new String[0], new int[]{10}, StressPattern.THRESHOLD);
                routine.setStressPattern(stressPattern);
            }
            routineManager.addRoutine(routine);

            CheckableTreeNode routineNode = new CheckableTreeNode(routine.toString(), routine);
            routineNode.setIcon(routine.getIcon());
            if (!StringUtil.isValid(groupName)) {
                groupName = "Default";
            }
            CheckableTreeNode deviceTypeNode = null;
            if (obj != null && obj instanceof DeviceType) {
                deviceTypeNode = treeNode;
            } else if (obj != null && obj instanceof String) {
                deviceTypeNode = (CheckableTreeNode) treeNode.getParent();
            }
            CheckableTreeNode groupNode = CheckableTreeUtil.getBranchNodeByName(deviceTypeNode, groupName);
            if (groupNode != null) {
                groupNode.add(routineNode);
            } else {
                groupNode = new CheckableTreeNode(groupName, groupName);
                deviceTypeNode.add(groupNode);
                groupNode.add(routineNode);
            }
            treeModel.reload(treeNode);
            tree.updateUI();
            tree.setSelectionPath(new TreePath(routineNode.getPath()));
        }
    }

    public void loadPagePattern(String path) {
        EntityPagePatternGroup patternGroup = null;
        try {
            PagePatternLoader loader = new PagePatternLoader();
            patternGroup = loader.load(path, deviceTag);

            List<RoutinePattern> routinePatterns = routinePatternManager.patterns;
            for (RoutinePattern routinePattern : routinePatterns) {
                routinePattern.loadPagePattern(patternGroup);
            }

            refreshPatternRootNode();
            treeModel.reload(patternRootNode);
            tree.updateUI();
        } catch (Exception ex) {
//                    logger.log(Level.SEVERE, "Failed to load page pattern file: {0}", path);
//                    logger.log(Level.SEVERE, "Exception", ex);

            StatusDisplayer.getDefault().setStatusText("Failed to load page patterns.");
        }
    }

    public void loadSpecPattern(String path) {
//                provider.reloadSpecPatterns(file.getAbsolutePath());
        SpecPatternGroup patternGroup = null;
        try {
            patternGroup = SpecPatternLoader.load(deviceTag.getDeviceType(), path);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            StatusDisplayer.getDefault().setStatusText("Failed to load spec patterns.");
        }
        if (patternGroup == null) {
            return;
        }

        List<RoutinePattern> routinePatterns = routinePatternManager.patterns;
        for (RoutinePattern routinePattern : routinePatterns) {
            routinePattern.loadSpecPattern(patternGroup);
        }

        refreshPatternRootNode();
        treeModel.reload(patternRootNode);
        tree.updateUI();
    }

    public CheckableTreeNode getPatternDeviceTypeNode(DeviceType deviceType) {
        int number = patternRootNode.getChildCount();
        for (int i = 0; i < number; i++) {
            CheckableTreeNode node = (CheckableTreeNode) patternRootNode.getChildAt(i);
            if (node.getAssociatedObject() == deviceType) {
                return node;
            }
        }
        return null;
    }

    public CheckableTreeNode getRoutineDeviceTypeNode(DeviceType deviceType) {
        int number = routineRootNode.getChildCount();
        for (int i = 0; i < number; i++) {
            CheckableTreeNode node = (CheckableTreeNode) routineRootNode.getChildAt(i);
            if (node.getAssociatedObject() == deviceType) {
                return node;
            }
        }
        return null;
    }

    public void refreshPatternRootNode() {
        List<DeviceType> deviceTypes = deviceTypeManager.getAllDeviceTypes();
        for (DeviceType deviceType : deviceTypes) {
            CheckableTreeNode typeNode = getPatternDeviceTypeNode(deviceType);
            if (typeNode != null) {
                refreshPatternNode(typeNode, deviceType);
            }
        }
    }

    public void refreshPatternNode(CheckableTreeNode typeNode, DeviceType deviceType) {
        typeNode.removeAllChildren();
    }

    public void refreshRoutineRootNode() {
        List<DeviceType> deviceTypes = deviceTypeManager.getAllDeviceTypes();
        for (DeviceType deviceType : deviceTypes) {
            CheckableTreeNode typeNode = getRoutineDeviceTypeNode(deviceType);
            if (typeNode != null) {
                refreshRoutineNode(typeNode, deviceType);
            }
        }
    }

    public void refreshRoutineNode(CheckableTreeNode typeNode, DeviceType deviceType) {
        typeNode.removeAllChildren();
        List<Routine> routines = routineManager.getRoutines(deviceType);
        if (routines != null) {
            for (Routine routine : routines) {
                CheckableTreeNode routineNode = new CheckableTreeNode(routine.toString(), routine);
                routineNode.setIcon(routine.getIcon());
                String groupName = routine.getGroup();
                if (!StringUtil.isValid(groupName)) {
                    groupName = "Default";
                }
                CheckableTreeNode groupNode = CheckableTreeUtil.getBranchNodeByName(typeNode, groupName);
                if (groupNode != null) {
                    groupNode.add(routineNode);
                } else {
                    groupNode = new CheckableTreeNode(groupName, groupName);
                    typeNode.add(groupNode);
                    groupNode.add(routineNode);
                }
            }
        }
    }

    @Override
    public void buildTree() {
        instrumentNode = new CheckableTreeNode("Instrument", null);
        rootNode.add(instrumentNode);
        int[] types = Instrument.ALL_TYPES;

        typeNodes = new CheckableTreeNode[types.length];

        for (int i = 0; i < types.length; i++) {
            Integer type = types[i];
            CheckableTreeNode typeNode = new CheckableTreeNode(Instrument.TYPE_NAMES[i], type);
//            typeNode.setIcon(null);
            typeNodes[i] = typeNode;
            instrumentNode.add(typeNode);
        }

        List<EntityInstrument> instruments = instrumentManager.instruments;
        for (EntityInstrument entityInstrument : instruments) {
            CheckableTreeNode node = new CheckableTreeNode(entityInstrument.getInstrumentName(), entityInstrument);
            int type = entityInstrument.getType();
            for (CheckableTreeNode typeNode : typeNodes) {
                Integer intObj = (Integer) typeNode.getAssociatedObject();
                if (intObj.intValue() == type) {
                    typeNode.add(node);
                    break;
                }
            }
        }

        patternRootNode = new CheckableTreeNode("Pattern and Connection", null);
        rootNode.add(patternRootNode);

        List<DeviceType> deviceTypes = deviceTypeManager.getAllDeviceTypes();

        for (DeviceType deviceType : deviceTypes) {
            CheckableTreeNode typeNode = new CheckableTreeNode(deviceType.getName(), deviceType);
            patternRootNode.add(typeNode);
            refreshPatternNode(typeNode, deviceType);
        }

        routineRootNode = new CheckableTreeNode("Routine", null);
        rootNode.add(routineRootNode);
        for (DeviceType deviceType : deviceTypes) {
            CheckableTreeNode typeNode = new CheckableTreeNode(deviceType.getName(), deviceType);
            routineRootNode.add(typeNode);
            refreshRoutineNode(typeNode, deviceType);
        }
    }

    @Override
    public void updateContent(Object lastPathComponent) {
        // TODO: matrix with connection to meters

        contentPanel.removeAll();
        massEditor.stopEditing();
        massEditor.clear();

        CheckableTreeNode treeNode;
        if (lastPathComponent != null && lastPathComponent instanceof CheckableTreeNode) {
            treeNode = (CheckableTreeNode) lastPathComponent;
            if (treeNode == instrumentNode) {
                GPIBBusPanel panel = new GPIBBusPanel();
                massEditor.addEditor(panel);
                contentPanel.add(panel, BorderLayout.NORTH);
            } else if (treeNode.isLeaf()) {
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof EntityInstrument) {
                    EntityInstrument ei = (EntityInstrument) obj;
                    ProfileEditor editor = ei.getSettingsEditor();
                    editor.setProfile(ei.getProfile());
                    massEditor.addEditor(editor);
                    if (ei.getType() == Instrument.TYPE_MATRIX) {
                        contentPanel.add(editor.getEditorComponent(), BorderLayout.NORTH);
                        MatrixBondGroup matrixBondGroup = instrumentManager.getMatrixBondGroup(ei);
                        MatrixBondPanel bondPanel = new MatrixBondPanel(ei, matrixBondGroup);
                        massEditor.addEditor(bondPanel);
                        contentPanel.add(bondPanel, BorderLayout.CENTER);
                    } else {
                        contentPanel.add(editor.getEditorComponent(), BorderLayout.NORTH);
                    }
                } else if (obj != null && obj instanceof Routine) {
                    Routine routine = (Routine) obj;
//                    RoutinePanel form = new RoutinePanel(routine);
                    RoutinePanel form = new RoutinePanel(routine);
                    massEditor.addEditor(form);
                    contentPanel.add(form, BorderLayout.CENTER);
                } else if (obj != null && obj instanceof DeviceType) {
                    DeviceType deviceType = (DeviceType) obj;
                    if (treeNode.getParent() == patternRootNode) {
                        RoutinePatternPanel form = new RoutinePatternPanel(deviceType);
                        massEditor.addEditor(form);
                        contentPanel.add(form, BorderLayout.CENTER);
                    } else if (treeNode.getParent() == routineRootNode) {
                        // nothing
                    }
                }
            }
        }

        contentPanel.updateUI();
    }

    @Override
    public void treeNodeChecked(CheckableTreeNode node, boolean checked) {
        // do nothing
    }
}
