/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceType;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.api.MeaEditor;
import com.platformda.iv.datacore.pattern.PagePatternLoader;
import com.platformda.iv.tools.auto.SyntaxEditor;
import com.platformda.spec.BaseSpecPattern;
import com.platformda.spec.SpecPattern;
import com.platformda.iv.spec.SpecPatternLoader;
import com.platformda.iv.tools.PatternUtil;
import com.platformda.utility.common.BasicFileFilter;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.utility.common.StringBuilderAppender;
import com.platformda.utility.tree.CheckableTree;
import com.platformda.utility.tree.CheckableTreeModel;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.tree.CheckableTreeNodeChecker;
import com.platformda.utility.ui.GUIUtil;
import com.platformda.utility.ui.JTreeUtil;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Junyi
 */
public class RoutinePatternPanel extends JPanel implements CheckableTreeNodeChecker, MeaEditor {

    public static final String KEY = "PatternBond";
    RoutinePatternManager routinePatternManager = RoutinePatternManager.getInstance();
    DeviceType deviceType;
    RoutinePattern routinePattern;
    VarProviderEditorImpl varEditor;
    TreeVarSelector treeVarSelector;
    DeviceBondTreePanel deviceBondTreePanel;
    //
    protected CheckableTree tree;
    protected CheckableTreeModel treeModel;
    protected CheckableTreeNode rootNode;

    public CheckableTreeNode getRootNode() {
        return rootNode;
    }
    protected JPanel contentPanel;
    //
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
        }
    };
    Action loadAction = new AbstractAction("Load...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };
    Action editPatternAction = new AbstractAction("Edit...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof EntityPagePattern) {
                    EntityPagePattern pagePattern = (EntityPagePattern) obj;
                    editPagePattern(pagePattern);
                } else if (obj != null && obj instanceof SpecPattern) {
                    // TODO: edit spec pattern
                }
            }
        }
    };
    Action copyPatternAction = new AbstractAction("Copy...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath treePath = tree.getSelectionPath();
            if (treePath != null) {
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof EntityPagePattern) {
                    EntityPagePattern pagePattern = (EntityPagePattern) obj;
                    copyPagePattern(pagePattern);
                } else if (obj != null && obj instanceof SpecPattern) {
                    // TODO: edit spec pattern
                }
            }
        }
    };

    public RoutinePatternPanel(DeviceType deviceType) {
        this.deviceType = deviceType;
        routinePattern = routinePatternManager.getRoutinePattern(deviceType);
        initComponents(262);

        textArea.getEditorPane().setEditable(false);
        textArea.hideToolBar();
        JScrollPane areaScroll = new JScrollPane(textArea);
        contentPanel.add(areaScroll, BorderLayout.CENTER);

        JTreeUtil.selectFirstPath(tree);
    }

    protected void initComponents(int dividerLocation) {
        setLayout(new java.awt.BorderLayout(5, 5));
//        setPreferredSize(ExtractionOptions.COMPONENT_WIZARD_COMPONENT_SIZE);
//        setPreferredSize(ExtractionOptions.COMPONENT_BROAD_SIZE);

        initTree();
        JScrollPane treeScroll = new JScrollPane(tree);

        JPanel leftPanel = new JPanel(new BorderLayout());
//        leftPanel.add(toolbarPanel, BorderLayout.NORTH);
        leftPanel.add(treeScroll, BorderLayout.CENTER);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        deviceBondTreePanel = new DeviceBondTreePanel(routinePattern, this);

//        JPanel rightPanel = new JPanel(new GridLayout(0, 1, 2, 2));
//        rightPanel.add(contentPanel);
//        rightPanel.add(deviceBondTreePanel);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplit.setBorder(null);
        if (!PatternSettingsPanel.HIDDEN_STRATEGY) {
            rightSplit.setLeftComponent(contentPanel);
        }    
        rightSplit.setRightComponent(deviceBondTreePanel);
        rightSplit.setDividerLocation(0.64);
        rightSplit.setResizeWeight(0.64);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(null);
//        splitPane.setLeftComponent(treeScroll);
        if(!PatternSettingsPanel.HIDDEN_STRATEGY){
            splitPane.setLeftComponent(leftPanel);
        }        
        splitPane.setRightComponent(rightSplit);
        splitPane.setDividerLocation(dividerLocation);
//        splitPane.setDividerLocation(0.3);
        splitPane.setResizeWeight(0.3);
//        setLayout(new BorderLayout());

        treeVarSelector = new TreeVarSelector(routinePattern, pageNode, specNode, tree, routinePattern.pagePatterns, routinePattern.specPatterns);
        varEditor = new VarProviderEditorImpl(routinePattern, treeVarSelector);
        add(varEditor, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setBorder(null);
        split.setTopComponent(splitPane);
        if (!PatternSettingsPanel.HIDDEN_STRATEGY) {
            split.setBottomComponent(varEditor);
        }


        split.setDividerLocation(0.64);
        split.setResizeWeight(0.8);

        setLayout(new BorderLayout(5, 5));
        add(split, BorderLayout.CENTER);

        updateIcons();

//        JPanel toolbarPanel = getToolbarPanel();
//        add(toolbarPanel, BorderLayout.NORTH);
//        setPreferredSize(MeaOptions.COMPONENT_META_WIDE_SIZE);
//        JTreeUtil.selectFirstPath(tree);
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
                final File file = LoadSaveUtil.saveFile("Export Pattern And Connection", JFileChooser.FILES_ONLY, KEY, null, new BasicFileFilter("ini"), "pattern.ini", false);
                if (file != null) {
                    try {
                    } catch (Exception ex) {
                        Logger.getLogger(RoutinePatternPanel.class.getName()).severe("Failed to export pattern and connection");
                    }
                }
            }
        });

        JPanel toolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.LEADING, addButton, removeButton, null, loadButton, dumpButton);
        return toolbarPanel;
    }
    CheckableTreeNode pageNode;
    CheckableTreeNode specNode;
    //
    SyntaxEditor textArea = new SyntaxEditor("text/specpattern");

    public void initTree() {
        initTree("Root");
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (e.getNewLeadSelectionPath() != null) {
                    updateContent(e.getPath().getLastPathComponent());
                }
            }
        });
        MouseListener routineTreeMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
                if (treePath == null) {
                    return;
                }
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (e.isMetaDown()) {
//                    if (treeNode == routineRootNode) {
//                        JPopupMenu popupMenu = new JPopupMenu();
////                        popupMenu.add(clearAllDieDataAction);
//                        popupMenu.show(routineTree, e.getX(), e.getY());
//                    } else
                    if (obj != null && obj instanceof EntityPagePattern) {
                        JPopupMenu popupMenu = new JPopupMenu();
                        popupMenu.add(editPatternAction);
//                        popupMenu.add(removeOnTreeAction);
                        popupMenu.show(tree, e.getX(), e.getY());
                    }
//                        else if (obj != null && obj instanceof SpecPattern) {
//                        JPopupMenu popupMenu = new JPopupMenu();
////                        popupMenu.add(editOnTreeAction);
//                        popupMenu.add(removeOnTreeAction);
//                        popupMenu.show(routineTree, e.getX(), e.getY());
//                    }
                } else if (e.getClickCount() == 2 && obj != null && obj instanceof EntityPagePattern) {
                    EntityPagePattern pagePattern = (EntityPagePattern) obj;
                    editPagePattern(pagePattern);
                }
            }
        };
        tree.addMouseListener(routineTreeMouseListener);

        treeModel.setTreeNodeCheckor(this);
        buildTree();
        JTreeUtil.expandTree(tree);
        tree.updateUI();
    }

    protected void editPagePattern(EntityPagePattern pagePattern) {
        EntityPagePattern rawPattern = new EntityPagePattern();
        rawPattern.setDeviceType(pagePattern.getDeviceType());
        rawPattern.setDevicePolarity(pagePattern.getDevicePolarity());
        rawPattern.copyValueOf(pagePattern);
        PagePatternEditPanel form = new PagePatternEditPanel(pagePattern);

        DialogDescriptor desc = new DialogDescriptor(form,
                "Edit Page Pattern", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            form.stopEditting();
            if (PatternUtil.checkPatternLegality(pagePattern)) {
                display(pagePattern);
                routinePattern.updateVars(null);
                varEditor.updateView();

            }else{
               pagePattern.copyValueOf(rawPattern);
            }
          
          
        }
    }

    protected void copyPagePattern(EntityPagePattern pagePattern) {
        // TODO:
    }

    protected void initTree(String rootName) {
        tree = new CheckableTree();
        rootNode = new CheckableTreeNode(rootName);
        treeModel = new CheckableTreeModel(tree, rootNode);
        tree.setModel(treeModel);
        tree.setToggleClickCount(2);
        tree.setRootVisible(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    public void updateIcons() {
        treeVarSelector.updateIcons();
    }

    public void buildTree() {
//        rootNode.setUserObject("Devie Types");
//        tree.setRootVisible(true);

        pageNode = new CheckableTreeNode("Page");
        specNode = new CheckableTreeNode("Spec");


        List<EntityPagePattern> pagePatterns = routinePattern.pagePatterns;
        if (!pagePatterns.isEmpty()) {
            rootNode.add(pageNode);
        }
        for (EntityPagePattern pattern : pagePatterns) {
            CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
            patternNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/page.png")));
            pageNode.add(patternNode);
        }

        List<SpecPattern> specPatterns = routinePattern.specPatterns;
        if (!specPatterns.isEmpty()) {
            rootNode.add(specNode);
        }
        for (SpecPattern pattern : specPatterns) {
            CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
            patternNode.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/spec.png")));
            specNode.add(patternNode);
        }
    }
    List<String> tweakingVarNames = new ArrayList<String>();

    public void updateContent(Object lastPathComponent) {
        // TODO: device, nodes, instance setup
        // TODO: pattern in text, variables, connection

        TreePath treePath = tree.getSelectionPath();
        if (treePath != null) {
            CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
            Object obj = treeNode.getAssociatedObject();
            if (obj != null && obj instanceof SpecPattern) {
                SpecPattern specPattern = (SpecPattern) obj;
                deviceBondTreePanel.highlighted(specPattern);
                display(specPattern);

                tweakingVarNames.clear();
                specPattern.fetchSimulationVarNames(tweakingVarNames);
                varEditor.onVarTweaking(tweakingVarNames);
            } else if (obj != null && obj instanceof EntityPagePattern) {
                EntityPagePattern pagePattern = (EntityPagePattern) obj;
                deviceBondTreePanel.highlighted(pagePattern);
                display(pagePattern);

                tweakingVarNames.clear();
                pagePattern.fetchVars(tweakingVarNames);
                varEditor.onVarTweaking(tweakingVarNames);
            } else {
                deviceBondTreePanel.clearHighlighted();

                tweakingVarNames.clear();
                varEditor.onVarTweaking(tweakingVarNames);
            }
        }
    }

    public void display(SpecPattern specPattern) {
        BaseSpecPattern bsp = (BaseSpecPattern) specPattern;
        StringBuilder builder = new StringBuilder();
        StringBuilderAppender appender = new StringBuilderAppender(builder);
        try {
            SpecPatternLoader.dump(bsp, appender);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        textArea.setContent("");
        textArea.setLang("text/specpattern");

        builder.append("\n\n");
        List<SpecPattern> allDepdens = bsp.getAllDepends();
        if (allDepdens != null) {
            for (SpecPattern sp : allDepdens) {
                BaseSpecPattern dbsp = (BaseSpecPattern) sp;
                try {
                    SpecPatternLoader.dump(dbsp, appender);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                builder.append("\n\n");
            }
        }
        textArea.setContent(builder.toString());

    }

    public void display(EntityPagePattern pagePattern) {
        textArea.setContent("");
        textArea.setLang("text/page");
        StringBuilder builder = new StringBuilder();
        StringBuilderAppender appender = new StringBuilderAppender(builder);
        try {
            PagePatternLoader.save(pagePattern, appender);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        textArea.setContent(builder.toString());
    }

    @Override
    public void treeNodeChecked(CheckableTreeNode node, boolean checked) {
        // do nothing
    }

    @Override
    public void stopEditing() {
        deviceBondTreePanel.stopEditing();
    }
}
