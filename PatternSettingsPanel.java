/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceTagImpl;
import com.platformda.datacore.DeviceType;
import com.platformda.iv.datacore.DeviceTypeManager;
import com.platformda.datacore.pattern.EntityPagePatternGroup;
import com.platformda.iv.datacore.pattern.PagePatternLoader;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.MeaSpace;
import com.platformda.iv.api.MassEditor;
import com.platformda.iv.api.MeaEditor;
import com.platformda.iv.tools.LogHandlerManager;
import com.platformda.spec.SpecPatternGroup;
import com.platformda.iv.spec.SpecPatternLoader;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.ui.JTreeUtil;
import com.platformda.utility.ui.TreePanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;
import org.openide.awt.StatusDisplayer;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Junyi
 */
public class PatternSettingsPanel extends TreePanel implements MeaEditor {
    private static final Logger logger = Logger.getLogger(PatternSettingsPanel.class.getName());

    public static final String KEY_PATTERN_PAGE = "PagePattern";
    public static final String KEY_PATTERN_SPEC = "SpecPattern";
    RoutinePatternManager routinePatternManager = RoutinePatternManager.getInstance();
    MeaDeviceTypeManager deviceTypeManager = MeaDeviceTypeManager.getInstance();
    MassEditor massEditor = new MassEditor();
    DeviceTag deviceTag = DeviceTypeManager.getDeviceTagByAbbreviation("nmosfet");
    //
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
                                //clear the page patterns
                                routinePattern.getPagePatterns().clear();
                                routinePattern.loadPagePattern(patternGroup);
                                updateContent(treeNode);
//                                refreshPatternRootNode();
//                                treeModel.reload(patternRootNode);
//                                tree.updateUI();
                            } catch (Exception ex) {
                                LogHandlerManager.getInstance().logException(logger, ex);
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
                            //clear the spec patterns
                            routinePattern.getSpecPatterns().clear();
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
    CheckableTreeNode patternRootNode;
    public static boolean HIDDEN_STRATEGY = false;

    public PatternSettingsPanel() {
        initComponents(180);
//        JPanel toolbarPanel = getToolbarPanel();
//        add(toolbarPanel, BorderLayout.NORTH);
        if (HIDDEN_STRATEGY) {
            setPreferredSize(MeaOptions.COMPONENT_HIDDEN_SIZE);
        } else {
            setPreferredSize(MeaOptions.COMPONENT_BROAD_SIZE);
        }        
        tree.setToggleClickCount(-1);
        initActions();
        JTreeUtil.selectFirstPath(tree);
        if(logger.getHandlers().length == 0){
            logger.addHandler(LogHandlerManager.getInstance().getDefault());
        } 
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
//                        JPopupMenu popupMenu = new JPopupMenu();
//                        popupMenu.add(loadPagePattern);
//                        popupMenu.add(loadSpecPattern);
//                        popupMenu.show(tree, e.getX(), e.getY());
                    } else if (obj != null && obj instanceof DeviceType) {
                        DeviceType deviceType = (DeviceType) obj;
                        if (treeNode.getParent() == patternRootNode) {
                            JPopupMenu popupMenu = new JPopupMenu();
                            popupMenu.add(loadPagePatternOnDeviceType);
                            popupMenu.add(loadSpecPatternOnDeviceType);
                            popupMenu.show(tree, e.getX(), e.getY());
                        }
                    }
                }
            }
        };
        tree.addMouseListener(mouseListener);
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
            LogHandlerManager.getInstance().logException(logger, ex);
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
            LogHandlerManager.getInstance().logException(logger, ex);
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

    public void refreshPatternRootNode() {
        List<DeviceType> deviceTypes = deviceTypeManager.getAllDeviceTypes();
        for (DeviceType deviceType : deviceTypes) {
            CheckableTreeNode typeNode = getPatternDeviceTypeNode(deviceType);
            if (typeNode != null) {
                refreshPatternNode(typeNode, deviceType);
            }
        }
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

    public void refreshPatternNode(CheckableTreeNode typeNode, DeviceType deviceType) {
        typeNode.removeAllChildren();
        // TODO:
    }

    @Override
    public void buildTree() {
        patternRootNode = new CheckableTreeNode("Pattern and Connection", null);
        rootNode.add(patternRootNode);

        List<DeviceType> deviceTypes = deviceTypeManager.getAllDeviceTypes();

        for (DeviceType deviceType : deviceTypes) {
            CheckableTreeNode typeNode = new CheckableTreeNode(deviceType.getName(), deviceType);
            patternRootNode.add(typeNode);
            refreshPatternNode(typeNode, deviceType);
        }
    }

    @Override
    public void updateContent(Object lastPathComponent) {
        contentPanel.removeAll();
        massEditor.stopEditing();
        massEditor.clear();

        CheckableTreeNode treeNode;
        if (lastPathComponent != null && lastPathComponent instanceof CheckableTreeNode) {
            treeNode = (CheckableTreeNode) lastPathComponent;
            if (treeNode.isLeaf()) {
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof DeviceType) {
                    DeviceType deviceType = (DeviceType) obj;
                    if (treeNode.getParent() == patternRootNode) {
                        RoutinePatternPanel form = new RoutinePatternPanel(deviceType);
                        massEditor.addEditor(form);
                        contentPanel.add(form, BorderLayout.CENTER);
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

    @Override
    public void stopEditing() {
        massEditor.stopEditing();
        massEditor.clear();
    }
}
