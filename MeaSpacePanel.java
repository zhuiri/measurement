/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.datacore.DeviceTypeManager;
import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.PXYPage;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.Installer;
//import com.platformda.datacore.pattern.PagePatternLoader;
import com.platformda.iv.datacore.pattern.PagePatternLoader;
import com.platformda.iv.MeaData;
import com.platformda.iv.MeaSpace;
import com.platformda.iv.MeaSpaceBrowser;
import com.platformda.iv.actions.MeasureAction;
import com.platformda.iv.actions.SelfCalibrationAction;
import com.platformda.iv.actions.Test21Action;
import com.platformda.iv.dialog.PDAChecker;
import com.platformda.iv.dialog.PDADialogOption;
import com.platformda.iv.help.DialogHelper;
import com.platformda.iv.instrument.meter.pda4139.PDA4139;
import com.platformda.iv.measure.MeaCallback;
import com.platformda.iv.tools.LogHandlerManager;
import com.platformda.iv.tools.PatternUtil;
import com.platformda.iv.view.MeaPageView;
import com.platformda.iv.view.TableUtil;
import com.platformda.spec.BaseSpecPattern;
import com.platformda.spec.Spec;
import com.platformda.spec.SpecData;
import com.platformda.spec.SpecPattern;
import com.platformda.iv.spec.SpecPatternLoader;
import com.platformda.iv.tools.PopMenuManager;
import com.platformda.iv.tools.SettingManager;
import com.platformda.iv.tools.ToolBarManager;
import com.platformda.iv.tools.table.EnhancedCopyTable;
import com.platformda.iv.viewgroup.ViewGroupTopComponent;
import com.platformda.utility.common.StringBuilderAppender;
import com.platformda.utility.common.StringUtil;
//import com.platformda.utility.table.EnhancedCopyTable;
import com.platformda.utility.tree.CheckableTree;
import com.platformda.utility.tree.CheckableTreeModel;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.tree.CheckableTreeNodeChecker;
import com.platformda.utility.tree.CheckableTreeUtil;
import com.platformda.utility.ui.GUIUtil;
import com.platformda.utility.ui.JTableUtil;
import com.platformda.utility.ui.SwingUtil;
import com.platformda.view.api.View;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.swing.etable.ETable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.windows.WindowManager;
import sun.java2d.loops.ProcessPath.ProcessHandler;

/**
 *
 * @author Junyi
 */
public class MeaSpacePanel extends JPanel implements ActionListener, MeaCallback,CheckableTreeNodeChecker {
    //private static final ConsoleImpl console = new ConsoleImpl("MeaSpacePanel");
    private static final Logger logger = Logger.getLogger(MeaSpacePanel.class.getName());
    private String loadUI ;
    MeaSpace meaSpace;
    MeaSpaceBrowser browser;
    //
    MeaData meaData;
    //
    Routine routine = null;
    //component the packs to clients
    List<DUTPack> packs = new ArrayList<DUTPack>();  
    //curent pack
    DUTPack pack = null;
    
    EntityPagePattern pagePattern;
    //
    MeaDeviceTableModel deviceTableModel;
    ETable deviceTable;
    //
    TreeSelectionListener pageTreeSelectionListener;
    protected CheckableTree pageTree;
    protected CheckableTreeModel pageTreeModel;
    protected CheckableTreeNode pageRootNode;
    //
    SpecValueTableModel specTableModel;
    EnhancedCopyTable specTable;
    //
    Spec21ValueTableModel spec21TableModel;
    EnhancedCopyTable spec21Table;
    
    JButton addDeviceButton = new JButton("");
    JButton removeDeviceButton = new JButton("");
    JButton addInstanceButton = new JButton("");
    JButton clearDataButton = new JButton("");
    JButton removeInstanceButton = new JButton("Remove Instance");
    //
    JButton addPagePatternButton = new JButton("");
    JButton removePagePatternButton = new JButton("");
    JButton addSpecPatternButton = new JButton("");
    JButton removeSpecPatternButton = new JButton("");
    //
    JButton selectAllPagePatternButton = new JButton("");
    JButton deSelectAllPagePatternButton = new JButton("");
    JButton selectAllSpecPatternButton = new JButton("");
    JButton deSelectAllSpecPatternButton = new JButton("");
    JButton scatterButton = new JButton("");
    JButton copySelectPageButton = new JButton("");
       
    
    //
    JPanel deviceToolbarPanel;
    JPanel pageToolbarPanel;
    JPanel specToolbarPanel;
    JPanel spec21ToolbarPanel;
    
    public void updateSpec21ValueTableModel(){
         this.spec21TableModel.fireTableDataChanged();
    }
    
//    MenuItem editPageItem;
    Action editPagePatternAction = new AbstractAction("Edit...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (routine != null) {
                TreePath treePath = pageTree.getSelectionPath();
                if (treePath != null) {
                    CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                    Object obj = treeNode.getAssociatedObject();
                    if (obj != null && obj instanceof EntityPagePattern) {
                        final EntityPagePattern selectedPagePattern = (EntityPagePattern) obj;
                        editPagePattern(treeNode, selectedPagePattern);
                    }
                }
            }
        }
    };
    Action copyPagePatternAction = new AbstractAction("Copy...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (routine != null) {
                TreePath treePath = pageTree.getSelectionPath();
                if (treePath != null) {
                    CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();                 
                    Object obj = treeNode.getAssociatedObject();
                    if (obj != null && obj instanceof EntityPagePattern) {
                        final EntityPagePattern selectedPagePattern = (EntityPagePattern) obj;
                        copyPagePattern(selectedPagePattern);
                    }
                }
            }
        }
    };
    Action measurePageAction = new AbstractAction("Measure...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            //check python.exe exsit
            if (!Installer.findSMU()) {
                DialogHelper.getMessageDialog("SMU has been killed!");
                return;
            }
            
            //is check selfcalibration
            if (SelfCalibrationAction.firstTime) {
                SelfCalibrationAction.firstTime = false;
                if (!SelfCalibrationAction.isChecked) {
                    boolean result = DialogHelper.getConfirmDialog("It is strongly recommanded to run SelfCalibration at first measurement!"
                            , ToolBarManager.MEASURE,"Yes,Calibrate","No,Continue");
                    if (result) {
                        return;
                    }
                }
            }            
            
            if (routine != null) {
                TreePath[] treePaths = pageTree.getSelectionPaths();
                String msg = "Do you want to measure this page of selected device?";
                boolean result = DialogHelper.getConfirmDialog(msg, "Measure Page");
                if (!result) {
                    return;
                } 
                
                
                MeasureAction measureAction = new MeasureAction();
                DUTPack dutPack = new DUTPack(routine);
                for (int index = 0; index < treePaths.length; index++) {
                    TreePath treePath = treePaths[index];
                    if (treePath != null) {
                        CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                        Object obj = treeNode.getAssociatedObject();
                        if (obj != null && obj instanceof EntityPagePattern) {
                            final EntityPagePattern selectedPagePattern = (EntityPagePattern) obj;
                            dutPack.addPagePattern(selectedPagePattern);
                        }
                    }
                }               

                measureAction.measure(meaSpace, browser, dutPack, MeaSpacePanel.this);
               
            }
        }
        
    };
    Action editSpecPatternAction = new AbstractAction("Edit...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (routine != null) {
                int index = specTable.getSelectedRow();
                index = specTable.convertRowIndexToModel(index);
                if (index >= 0) {
                    SpecPattern specPattern = routine.getSpecPattern(index);
                    editSpecPattern(specPattern);
                }
            }
        }
    };
    Action measureSpecAction = new AbstractAction("Measure...") {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selectRows = specTable.getSelectedRows();
            String msg = "Do you want to measure this spec of selected device?";
            boolean result = DialogHelper.getConfirmDialog(msg, "Measure Spec");
            if (!result) {
                return;
            }
            
            for (int index = 0; index < selectRows.length; index++) {
                 selectRows[index] = specTable.convertRowIndexToModel(selectRows[index]);           
            }
            measureSpecs(selectRows);
           
            
        }
        
        private void measureSpecs(int[] selectRows){
             MeasureAction measureAction = new MeasureAction();
             DUTPack dutPack = new DUTPack(routine);
            for(int index = 0;index<selectRows.length;index++){
                if (index >= 0) {
                    final SpecPattern selectedSpecPattern = routine.specPatterns.get(selectRows[index]);
                    dutPack.addSpecPattern(selectedSpecPattern);                   
                }
            }
            measureAction.measure(meaSpace, browser, dutPack, MeaSpacePanel.this);           
          
        }        
      
    };

    public MeaSpacePanel(MeaSpace meaSpace, MeaSpaceBrowser browser) {
        loadUI = SettingManager.getInsance().readValue("PagePatternUILoader");
        this.meaSpace = meaSpace;
        this.browser = browser;
        meaData = meaSpace.getMeaData();
        initComponents();
        if(logger.getHandlers().length == 0){
             logger.addHandler(LogHandlerManager.getInstance().getDefault());
        } 
    }
    
      @Override
    public void treeNodeChecked(CheckableTreeNode treeNode, boolean bool) {
        Object obj = treeNode.getAssociatedObject();
        int deviceIndex =  deviceTable.getSelectedRow();
        deviceIndex = deviceTable.convertRowIndexToModel(deviceIndex);
        EntityDevice curDevice = meaData.getDevice(deviceIndex);

        if (obj != null && obj instanceof EntityPagePattern) {
            EntityPagePattern pagePattern = (EntityPagePattern) obj;
            if (bool) {  
                //remove first
                pack.removePagePattern(pagePattern);
                pack.addPagePattern(pagePattern);
            } else {
                pack.removePagePattern(pagePattern);
            }
        }
    }

    private void initToolbarPanels() {
        addDeviceButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/add.gif")));
        removeDeviceButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/remove.gif")));
        addInstanceButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/add_task.png")));
        clearDataButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/add_task.png")));
        addPagePatternButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/add.gif")));
        removePagePatternButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/remove.gif")));
        addSpecPatternButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/add.gif")));
        removeSpecPatternButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/remove.gif")));
        //select all/deselect
        selectAllPagePatternButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/selectAll.png")));
        deSelectAllPagePatternButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/deSelectAll.png")));
        selectAllSpecPatternButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/selectAll.png")));
        deSelectAllSpecPatternButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/deSelectAll.png")));
        //scatter all selected pages
         scatterButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/scatter16.png")));
         //copy selected pages
         copySelectPageButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/copy.png")));
      

        addDeviceButton.setToolTipText("Add Device...");
        removeDeviceButton.setToolTipText("Remove Device...");
        addInstanceButton.setToolTipText("Add Instance...");
        clearDataButton.setToolTipText("Clear Data...");
        addPagePatternButton.setToolTipText("Add Page...");
        removePagePatternButton.setToolTipText("Remove Page...");
        addSpecPatternButton.setToolTipText("Add Spec...");
        removeSpecPatternButton.setToolTipText("Remove Spec...");
        
        selectAllPagePatternButton.setToolTipText("Select All Pages");
        deSelectAllPagePatternButton.setToolTipText("deSelect All Pages");
        selectAllSpecPatternButton.setToolTipText("Select All Specs");
        deSelectAllSpecPatternButton.setToolTipText("Select All Specs");
        scatterButton.setToolTipText("Scatter all selected pages of selected devices");
        copySelectPageButton.setToolTipText("Copy all selected pages");

        addDeviceButton.addActionListener(this);
        removeDeviceButton.addActionListener(this);
        addInstanceButton.addActionListener(this);
        clearDataButton.addActionListener(this);
        addPagePatternButton.addActionListener(this);
        removePagePatternButton.addActionListener(this);
        addSpecPatternButton.addActionListener(this);
        removeSpecPatternButton.addActionListener(this);
        
        selectAllPagePatternButton.addActionListener(this);
        deSelectAllPagePatternButton.addActionListener(this);
        selectAllSpecPatternButton.addActionListener(this);
        deSelectAllSpecPatternButton.addActionListener(this);
        scatterButton.addActionListener(this);
        copySelectPageButton.addActionListener(this);

//        deviceToolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, addDeviceButton, removeDeviceButton);
//        pageToolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, addPagePatternButton, removePagePatternButton);
//        specToolbarPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, addSpecPatternButton, removeSpecPatternButton);

        JPanel dtPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, addDeviceButton, removeDeviceButton, null, addInstanceButton);//clearDataButton
        JPanel ptPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, addPagePatternButton, removePagePatternButton , selectAllPagePatternButton, deSelectAllPagePatternButton,scatterButton,copySelectPageButton);//copySelectPageButton
        JPanel stPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, addSpecPatternButton, removeSpecPatternButton , selectAllSpecPatternButton, deSelectAllSpecPatternButton);

        deviceToolbarPanel = new JPanel(new BorderLayout());
        deviceToolbarPanel.add(new JLabel("Device:"), BorderLayout.WEST);
        deviceToolbarPanel.add(dtPanel, BorderLayout.CENTER);

        pageToolbarPanel = new JPanel(new BorderLayout());
        pageToolbarPanel.add(new JLabel("Page:"), BorderLayout.WEST);
        pageToolbarPanel.add(ptPanel, BorderLayout.CENTER);

        specToolbarPanel = new JPanel(new BorderLayout());
        specToolbarPanel.add(new JLabel("Spec:"), BorderLayout.WEST);
        specToolbarPanel.add(stPanel, BorderLayout.CENTER);
        
        spec21ToolbarPanel = new JPanel(new BorderLayout());
        spec21ToolbarPanel.add(new JLabel("Spec21:"), BorderLayout.WEST);

    }

    private void initComponents() {
        initToolbarPanels();

        deviceTableModel = new MeaDeviceTableModel(meaData);
        deviceTable = new ETable(deviceTableModel);
         //table column config
        for(int index = 0;index<deviceTableModel.getColumnCount();index++){
            TableUtil.configTableColumeWidth(deviceTable, index, 20, 50, 200);
        }
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
        deviceWrapPanel.add(deviceToolbarPanel, BorderLayout.NORTH);
        deviceWrapPanel.add(deviceScroll, BorderLayout.CENTER);

        pageTree = new CheckableTree();
        pageRootNode = new CheckableTreeNode("Page");
        pageTreeModel = new CheckableTreeModel(pageTree, pageRootNode);
        pageTree.setModel(pageTreeModel);
        pageTree.setToggleClickCount(2);
        pageTree.setRootVisible(false);
        pageTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);//CONTIGUOUS_TREE_SELECTION
        pageTreeModel.setTreeNodeCheckor(this);
        MouseListener pageMouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath[] treePaths = pageTree.getSelectionPaths();
                TreePath treePath = pageTree.getPathForLocation(e.getX(), e.getY());
                if (treePaths == null||treePath == null) {
                    return;
                }
                CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                Object obj = treeNode.getAssociatedObject();
                if (e.isMetaDown()) {                    
                    if (obj != null && obj instanceof EntityPagePattern) {
                        if(treePaths.length == 1){
                            JPopupMenu popupMenu = PopMenuManager.getInstance().getPopMenu(PopMenuManager.PAGE_POPMENU);//new JPopupMenu();
                            popupMenu.removeAll();
                            popupMenu.add(editPagePatternAction);
                            popupMenu.add(copyPagePatternAction);
                            popupMenu.addSeparator();
//                        popupMenu.add(clearAllPageDataAction);
                            popupMenu.add(measurePageAction);
                            if(PopMenuManager.getInstance().getMeasureEnable()){
                                 popupMenu.show(pageTree, e.getX(), e.getY());
                            }
                           
                        }else{
                            JPopupMenu popupMenu = PopMenuManager.getInstance().getPopMenu(PopMenuManager.PAGE_POPMENU);//new JPopupMenu();
                            popupMenu.removeAll();
                            popupMenu.add(measurePageAction);
                            if (PopMenuManager.getInstance().getMeasureEnable()) {
                                popupMenu.show(pageTree, e.getX(), e.getY());
                            }
                           
                        }
                   
                    }
                } else if (e.getClickCount() == 2 && obj != null && obj instanceof EntityPagePattern) {
                    EntityPagePattern pagePattern = (EntityPagePattern) obj;
                    editPagePattern(treeNode, pagePattern);
                }
            }
        };
        pageTree.addMouseListener(pageMouseListener);
        pageTreeSelectionListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (e.getNewLeadSelectionPath() != null) {//e.getPaths().length ==2&&
                    //set check box
                    setCheckBox(e.getPath().getLastPathComponent());
                    
                    updateContent(e.getPath().getLastPathComponent());
                }
            }
        };
        pageTree.addTreeSelectionListener(pageTreeSelectionListener);
        JScrollPane pageScroll = new JScrollPane(pageTree);
        JPanel pageWrapPanel = new JPanel(new BorderLayout());
        pageWrapPanel.add(pageToolbarPanel, BorderLayout.NORTH);
        pageWrapPanel.add(pageScroll, BorderLayout.CENTER);

        specTableModel = new SpecValueTableModel();
        specTable = new EnhancedCopyTable(specTableModel);
        specTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        specTable.addMouseListener(new MouseAdapter() {          
            @Override
            public void mousePressed(MouseEvent e) {
                int row = specTable.rowAtPoint(new Point(e.getX(), e.getY()));
                //set check box selection
                if (row >= 0 && row < specTableModel.getRowCount()) {
                    //get original value
                    boolean isChecked = (boolean)specTableModel.getValueAt(row, 0);
                    if(isChecked){
                        //remove 
                        specTableModel.setValueAt(false, row, 0);
                       
                    }else{
                        //remove first
                        specTableModel.setValueAt(false, row, 0);
                        //add later
                        specTableModel.setValueAt(true, row, 0);
                    }
                   
                   
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (specTable.getSelectedRowCount() == 1 ) {
                        JPopupMenu popupMenu = PopMenuManager.getInstance().getPopMenu(PopMenuManager.SPEC_POPMENU);
                        popupMenu.removeAll();
                        popupMenu.add(editSpecPatternAction);
                        popupMenu.addSeparator();
                        popupMenu.add(measureSpecAction);    
                        if(PopMenuManager.getInstance().getMeasureEnable()){
                            popupMenu.show(specTable, e.getX(), e.getY());
                        }
                        
                    }else{
                      
                    }
                }
            }
        });
        JScrollPane specScroll = new JScrollPane(specTable);
        JPanel specWrapPanel = new JPanel(new BorderLayout());
        specWrapPanel.add(specToolbarPanel, BorderLayout.NORTH);
        specWrapPanel.add(specScroll, BorderLayout.CENTER);
        
        
        spec21TableModel = new Spec21ValueTableModel();
        spec21Table = new EnhancedCopyTable(spec21TableModel);
        JScrollPane spec21Scroll = new JScrollPane(spec21Table);
        JPanel spec21WrapPanel = new JPanel(new BorderLayout());
        spec21WrapPanel.add(spec21ToolbarPanel, BorderLayout.NORTH);
        spec21WrapPanel.add(spec21Scroll, BorderLayout.CENTER);

        JPanel routineResultPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        routineResultPanel.add(pageWrapPanel);
        routineResultPanel.add(specWrapPanel);
        String allowSpec21 = SettingManager.getInsance().readValue("ExportT21");
        if(allowSpec21!=null&&allowSpec21.equalsIgnoreCase("true")){
            routineResultPanel.add(spec21WrapPanel);
        }
        

//        JPanel routineToolbarPanel = createRoutineToolBarPanel();
        JPanel routienResultWrapPanel = new JPanel(new BorderLayout());
//        routienResultWrapPanel.add(routineToolbarPanel, BorderLayout.NORTH);
        routienResultWrapPanel.add(routineResultPanel, BorderLayout.CENTER);

        JSplitPane bottomSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        // TODO: 
        bottomSplitPane.setTopComponent(deviceWrapPanel);
        bottomSplitPane.setBottomComponent(routienResultWrapPanel);
        bottomSplitPane.setResizeWeight(0.15);
        bottomSplitPane.setBorder(null);

        setLayout(new BorderLayout());
        add(bottomSplitPane, BorderLayout.CENTER);
        
        //add initial packs
        List<EntityDevice> devices = meaData.getDevices();
        for (EntityDevice device : devices) {
            Routine routine = meaData.getRoutineByDevice(device);
           
            DUTPack pack = new DUTPack(routine);
            packs.add(pack);
        }

        if (meaData.getDeviceNumber() > 0) {
            JTableUtil.setSelectedIndex(deviceTable, deviceTableModel, 0);
            deviceHighlighted();
        }
        
        
    }

    protected void editPagePattern(CheckableTreeNode treeNode, EntityPagePattern pagePattern) {
        IndPagePattern ipp = routine.getIndPagePattern(pagePattern);       
        if (loadUI!=null&&loadUI.equalsIgnoreCase("true")) {
            final PagePatternUpdateUI form = new PagePatternUpdateUI();
            form.setPagePattern(pagePattern, routine,false);
            boolean result = DialogHelper.withChecker((JFrame)WindowManager.getDefault().getMainWindow(), "Edit Page Pattern", form, 
                    new PDAChecker(){
                        @Override 
                        public String doCheck(PDADialogOption option){
                            return form.getErrorInfo();                            
                        }
                    });
//            DialogDescriptor desc = new DialogDescriptor(form,
//                    "Edit Page Pattern", true, DialogDescriptor.OK_CANCEL_OPTION,
//                    DialogDescriptor.OK_OPTION,null                
//                    );
//            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
//            if (result == NotifyDescriptor.OK_OPTION)
              if (result) {                
                EntityPagePattern updatedPagePattern = form.getPagePattern();
                if (!PatternUtil.checkPatternLegality(updatedPagePattern)) {
                    return;
                }
                String updatedName = updatedPagePattern.getName();
                updatedPagePattern.setName(pagePattern.getName());
                StringBuilder builder = new StringBuilder();
                StringBuilderAppender appender = new StringBuilderAppender(builder);
                try {
                    PagePatternLoader.save(pagePattern, appender);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    LogHandlerManager.getInstance().logException(logger, ex);
                }
                String raw = builder.toString();
                builder.setLength(0);
                try {
                    PagePatternLoader.save(updatedPagePattern, appender);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    LogHandlerManager.getInstance().logException(logger, ex);
                }
                boolean eq = raw.equalsIgnoreCase(builder.toString());
                if (!eq) {
                    pagePattern.copyValueOf(updatedPagePattern);
                }
                if (!updatedName.equalsIgnoreCase(pagePattern.getName())) {
                    pagePattern.setName(updatedName);
                    treeNode.setUserObject(updatedName);
                    pageTree.updateUI();
                }
                if (!eq) {
                    routine.updatePagePattern(meaData, pagePattern);
                    updatePageTreeIcons();
                }
            }
            
        } else {
            PagePatternUpdatePanel form = new PagePatternUpdatePanel(ipp);
            //form.setPagePattern(pagePattern);
            form.setPagePattern(pagePattern, routine);
            DialogDescriptor desc = new DialogDescriptor(form,
                    "Edit Page Pattern(F1 for Help)", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION, null);
            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result == NotifyDescriptor.OK_OPTION) {
                EntityPagePattern updatedPagePattern = form.getPagePattern();
                if (!PatternUtil.checkPatternLegality(updatedPagePattern)) {
                    return;
                }
                String updatedName = updatedPagePattern.getName();
                updatedPagePattern.setName(pagePattern.getName());
                StringBuilder builder = new StringBuilder();
                StringBuilderAppender appender = new StringBuilderAppender(builder);
                try {
                    PagePatternLoader.save(pagePattern, appender);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    LogHandlerManager.getInstance().logException(logger, ex);
                }
                String raw = builder.toString();
                builder.setLength(0);
                try {
                    PagePatternLoader.save(updatedPagePattern, appender);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    LogHandlerManager.getInstance().logException(logger, ex);
                }
                boolean eq = raw.equalsIgnoreCase(builder.toString());
                if (!eq) {
                    pagePattern.copyValueOf(updatedPagePattern);
                }
                if (!updatedName.equalsIgnoreCase(pagePattern.getName())) {
                    pagePattern.setName(updatedName);
                    treeNode.setUserObject(updatedName);
                    pageTree.updateUI();
                }
                form.updateVars(updatedPagePattern);
                if (form.updateVar() || !eq) {
                    routine.updatePagePattern(meaData, pagePattern);
                    updatePageTreeIcons();
                }
            }
        }


    }

    protected void copyPagePattern(EntityPagePattern pagePattern) {
        IndPagePattern ipp = routine.getIndPagePattern(pagePattern);
        if (loadUI != null && loadUI.equalsIgnoreCase("true")) {
            final PagePatternUpdateUI form = new PagePatternUpdateUI();
            form.setPagePattern(pagePattern, routine, false);
            boolean result = DialogHelper.withChecker((JFrame) WindowManager.getDefault().getMainWindow(), "Copy Page Pattern", form,
                    new PDAChecker() {
                        @Override
                        public String doCheck(PDADialogOption option) {
                            return form.getErrorInfo();
                        }
                    });
            if (result) {
                IndPagePattern indPagePattern = form.getIndPagePattern();
                routine.addIndPagePattern(indPagePattern);
                pagePattern = indPagePattern.pagePattern;
                CheckableTreeNode patternNode = new CheckableTreeNode(pagePattern.getName(), pagePattern);
                patternNode.setCheckable(true);
                patternNode.setIcon(pageIcon);
                pageRootNode.add(patternNode);
                pageTree.updateUI();
                pageTree.setSelectionPath(new TreePath(patternNode.getPath()));
            }

        } else {
            PagePatternUpdatePanel form = new PagePatternUpdatePanel(ipp);
            form.setPagePattern(pagePattern, this.routine);
            DialogDescriptor desc = new DialogDescriptor(form,
                    "Copy Page Pattern", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION, null);
            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result == NotifyDescriptor.OK_OPTION) {
                IndPagePattern indPagePattern = form.getIndPagePattern();
                routine.addIndPagePattern(indPagePattern);
                pagePattern = indPagePattern.pagePattern;
                CheckableTreeNode patternNode = new CheckableTreeNode(pagePattern.getName(), pagePattern);
                patternNode.setCheckable(true);
                patternNode.setIcon(pageIcon);
                pageRootNode.add(patternNode);
                pageTree.updateUI();
                pageTree.setSelectionPath(new TreePath(patternNode.getPath()));

            }
        }   
       
    }

    protected void editSpecPattern(SpecPattern specPattern) {
        IndSpecPattern isp = routine.getIndSpecPattern(specPattern);
        SpecPatternUpdatePanel form = new SpecPatternUpdatePanel(isp);
        form.setSpecPattern(specPattern);
        DialogDescriptor desc = new DialogDescriptor(form,
                "Edit Spec Pattern", true, DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null);
        Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
        if (result == NotifyDescriptor.OK_OPTION) {
            form.stopEditting();
            BaseSpecPattern updatedPattern =(BaseSpecPattern) form.getSpecPattern();
            String updatedName = updatedPattern.getName();
            updatedPattern.setName(updatedName);
            StringBuilder builder = new StringBuilder();
            StringBuilderAppender appender = new StringBuilderAppender(builder);
            try {
                SpecPatternLoader.dump((BaseSpecPattern)specPattern, appender);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                LogHandlerManager.getInstance().logException(logger, ex);
            }
            String raw = builder.toString();
            builder.setLength(0);
            try {
                SpecPatternLoader.dump(updatedPattern, appender);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                LogHandlerManager.getInstance().logException(logger, ex);
            }
            boolean eq = raw.equalsIgnoreCase(builder.toString());
            if (!eq) {
                 BaseSpecPattern baseSpecPattern = (BaseSpecPattern)specPattern; 
                //set names
                if(!baseSpecPattern.getName().equalsIgnoreCase(updatedPattern.getName())){
                    baseSpecPattern.setName(updatedPattern.getName());
                }
                
               //set simulate vars 
              
               baseSpecPattern.getSimulationVars().clear();
               for(int varIndex = 0; varIndex < updatedPattern.getSimulationVars().size();varIndex++){
                    baseSpecPattern.addSimulationVar(updatedPattern.getSimulationVars().get(varIndex));
               }            
                            
            }
           form.updateVars(updatedPattern);
            
            if (form.updateVar() || !eq) {
                meaData.removeSpecData(routine.getDevice(), specPattern);
                specTableModel.fireTableDataChanged();
            }
            
        }
    }
    
    public void setCheckBox(Object lastPathComponent){
         CheckableTreeNode treeNode;
        if (lastPathComponent != null && lastPathComponent instanceof CheckableTreeNode) {
            treeNode = (CheckableTreeNode) lastPathComponent;
            EntityPagePattern pattern = (EntityPagePattern) treeNode.getAssociatedObject();
            if (treeNode.isChecked()) {
                treeNode.setChecked(false);
                //remove 
                pack.removePagePattern(pattern);

            } else {
                treeNode.setChecked(true);
                //remove first,then add
                pack.removePagePattern(pattern);
                pack.addPagePattern(pattern);
            }
           
        }
    }

    public void updateContent(Object lastPathComponent) {
        CheckableTreeNode treeNode;
        if (lastPathComponent != null && lastPathComponent instanceof CheckableTreeNode) {
            treeNode = (CheckableTreeNode) lastPathComponent;
//            if (treeNode == rootNode) {
//                selectedDie = null;
//                //
//                updatePageTreeIcons();
//                specTableModel.fireTableDataChanged();
//            } else 


            if (treeNode.isLeaf()) {
                Object obj = treeNode.getAssociatedObject();
                if (obj != null && obj instanceof EntityPagePattern) {
                    pagePattern = (EntityPagePattern) obj;
                    pageHighlighted();
                }
            }
        }
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
    public static final Image pageImage = ImageUtilities.loadImage("com/platformda/iv/resources/page.png");
    public static final Image meaedBadge = ImageUtilities.loadImage("com/platformda/iv/resources/server_running.png");
    public static final Image pageMeaedImage = ImageUtilities.mergeImages(pageImage, meaedBadge, 8, 8);
    public static final ImageIcon pageIcon = new ImageIcon(pageImage);
    public static final ImageIcon pageMeaedIcon = new ImageIcon(pageMeaedImage);

    public void buildPageTree() {
        pageRootNode.removeAllChildren();
        if (routine != null) {
            List<EntityPagePattern> pagePatterns = routine.pagePatterns;
            for (EntityPagePattern pattern : pagePatterns) {
                CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
                patternNode.setIcon(pageIcon);
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
                node.setIcon(pageMeaedIcon);
            } else {
                node.setIcon(pageIcon);
            }
        }

        pageTree.repaint();
    }
    
    public void specHighlighted(int row){
          specTable.setRowSelectionInterval(row, row);
          specTable.scrollRectToVisible(specTable.getCellRect(row, 0, true));
    }

    public void pageHighlighted() {
        if (routine != null) {
            EntityDevice meaedDevice = routine.device;

            // TODO: build views and send to 
            List<PXYPage> pages = meaData.getPages(meaedDevice, pagePattern);
            if (pages != null && !pages.isEmpty()) {
                List<MeaPageView> views = new ArrayList<MeaPageView>(pages.size());
                for (PXYPage page : pages) {
                    MeaPageView view = new MeaPageView(page, routine, pagePattern);
                    views.add(view);
                }
                displayViews(views);
            }else{
                //set empty view
                displayViews(null);
            }
        }
    }

    protected void displayViews(List<MeaPageView> pages) {
        if (pages != null) {
            View[] views = new View[pages.size()];
            views = pages.toArray(views);
            if (views.length == 1) {
                browser.displayView(views[0]);
            } else {
                browser.displayView(views);
            }
        }else{
            browser.displayView(new View[1]);
        }
    }

    public void viewActivated(View view) {
        if (view != null && view instanceof MeaPageView) {
            MeaPageView meaPageView = (MeaPageView) view;
            EntityDevice device = meaPageView.getDevice();
            Routine viewRoutine = meaPageView.getRoutine();
            EntityPagePattern pagePattern = meaPageView.getPagePattern();
            if (pagePattern == null) {
                pagePattern = viewRoutine.getPagePattern(meaPageView.getPageType());
            }

            if (pagePattern != null) {
                CheckableTreeNode treeNode = CheckableTreeUtil.getTreeNode(pageRootNode, pagePattern);
                if (treeNode != null) {
                    pageTree.removeTreeSelectionListener(pageTreeSelectionListener);
                    TreePath treePath = new TreePath(treeNode.getPath());
                    pageTree.scrollPathToVisible(treePath);
                    pageTree.setSelectionPath(treePath);
                    pageTree.addTreeSelectionListener(pageTreeSelectionListener);
                }

            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == removeDeviceButton) {
            String msg = "Do you want to delete the seleted devices?";
            boolean result = DialogHelper.getConfirmDialog(msg, "Delete Devices");
            if (!result) {
                return;
            }           
            
            
            int index = deviceTable.getSelectedRow();
            index = deviceTable.convertRowIndexToModel(index);
            if (index >= 0) {
                meaData.removeDevice(index);
                deviceTableModel.fireTableDataChanged();
                JTableUtil.setSelectedIndex(deviceTable, deviceTableModel, index - 1);
                packs.remove(index);
                deviceHighlighted();

            }
        } else if (source == addDeviceButton) {
            addDevice();
        } else if (source == addInstanceButton) {
            InstancePanel form = new InstancePanel();
            DialogDescriptor desc = new DialogDescriptor(form,
                    "New Instance", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION, null);
            desc.setValid(true);
            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result == NotifyDescriptor.OK_OPTION) {
                String name = form.getInstName();
                String valueStr = form.getInstValue();
                if (name.isEmpty() || valueStr.isEmpty()) {
                    return;
                } else {
                    double value = Double.parseDouble(valueStr);
                    List<EntityDevice> devices = meaData.getDevices();
                    for (EntityDevice device : devices) {
                        device.setInstance(name, value);
                        meaData.updateDevice(device);
                    }

                    int index = deviceTable.getSelectedRow();
                    deviceTableModel.deviceChanged();
                     //table column config
                    int colnum = deviceTable.getColumnCount();                     
                    for(int i = 0;i<colnum;i++){
                        TableUtil.configTableColumeWidth(deviceTable, i, 20, 50, 200);
                    }
                    JTableUtil.setSelectedIndex(deviceTable, deviceTableModel, index);
                }
            }
        } else if (source == clearDataButton){
            clearData();            
        } else if (source == addPagePatternButton) {
            addPagePattern();
        } else if (source == removePagePatternButton) {
            if(pack.getSelectedPagePatterns().isEmpty()){
                String msg = "please select pages to delete!";
                DialogHelper.getMessageDialog(msg);
                return;
            }
            String msg = "Do you want to delete the seleted pages?";
            boolean result = DialogHelper.getConfirmDialog(msg, "Delete Pages");
            if (!result) {
                return;
            }
            removePagePatterns();
            //removePagePattern();
        } else if (source == addSpecPatternButton) {
            addSpecPattern();
            //set visible to last row of table
            if (specTable.getRowCount() != 0) {
                int lastRow = specTable.getRowCount() - 1;
//            specTable.setRowSelectionInterval(lastRow, lastRow);
//            specTable.scrollRectToVisible(specTable.getCellRect(lastRow, 0, true));
                specHighlighted(lastRow);
            }
          
        } else if (source == removeSpecPatternButton) {
             if(pack.getSelectedSpecPatterns().isEmpty()){
                String msg = "please select specs to delete!";
                DialogHelper.getMessageDialog(msg);
                return;
            }
            String msg = "Do you want to delete the seleted specs?";
            boolean result = DialogHelper.getConfirmDialog(msg, "Delete Specs");
            if (!result) {
                return;
            }
            removeSpecPatterns();
        } else if (source == selectAllPagePatternButton){
             pack.selectAllPages(routine);
             deviceHighlighted();
        } else if (source == deSelectAllPagePatternButton){
             pack.deSelectAllPages(routine);
             deviceHighlighted();
        } else if (source == selectAllSpecPatternButton){
             pack.selectAllSpecs(routine);
             deviceHighlighted();
        } else if (source == deSelectAllSpecPatternButton){
             pack.deSelectAllSpecs(routine);
             deviceHighlighted();
        } else if (source == scatterButton){
            //todo sctter all pages
           // scatterAllPages(routine.getPagePatterns());
            //scatter all selected pages
            scatterAllPages(pack.getSelectedPagePatterns());
        } else if (source == copySelectPageButton){
            if(pack.getSelectedPagePatterns().isEmpty()){
                
                DialogHelper.getMessageDialog("please select pages to copy");
                return;
            }                   
            String msg = "Do you want to copy the seleted pages?";
            boolean result = DialogHelper.getConfirmDialog(msg, "Copy Pages");
            if (!result) {
                return;
            }           
            
            //copy all the selected pages
            final ProgressHandle handle = ProgressHandleFactory.createHandle("starting to copy pages");

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        handle.start();
                        //copy select pages
                        List<EntityPagePattern> patterns = pack.getSelectedPagePatterns();
                        int size = patterns.size();
                        for (int index = 0; index < size; index++) {                              
                            EntityPagePattern pattern = patterns.get(index);
                            IndPagePattern indPattern = routine.getIndPagePattern(pattern);
                       
                            AbstractPagePanel form;
                            if (loadUI != null && loadUI.equalsIgnoreCase("true")) {
                                form = new PagePatternUpdateUI();
                                form.setPagePattern(pattern, routine);
                            } else {
                                form = new PagePatternUpdatePanel(indPattern);
                                form.setPagePattern(pattern);
                            }                            
                          
                            //get IndPattern from update panel
                            IndPagePattern indPagePattern = form.getIndPagePattern();
                            routine.addIndPagePattern(indPagePattern);
                            //copy back
                            pattern = indPagePattern.pagePattern;
                            
                            //update tree node
                            CheckableTreeNode patternNode = new CheckableTreeNode(pattern.getName(), pattern);
                            patternNode.setCheckable(true);
                            patternNode.setIcon(pageIcon);
                            pageRootNode.add(patternNode);
                            pageTree.updateUI();
                            //pageTree.setSelectionPath(new TreePath(patternNode.getPath()));
                        }
                      
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                        LogHandlerManager.getInstance().logException(logger, e);
                    } finally {
                        handle.finish();
                    }
                }
            };
                    
            Thread thread = new Thread(runnable);
            thread.start();
            

        }
    }
    
    private void scatterAllPages(List<EntityPagePattern> pagePatterns){
         if (routine != null) {
            EntityDevice meaedDevice = routine.device;

            // TODO: build views and send to 
            List<MeaPageView> views = new ArrayList<>();
             for (int index = 0; index <pagePatterns.size(); index++) {
                 EntityPagePattern curPagePattern = pagePatterns.get(index);
                 List<PXYPage> pages = meaData.getPages(meaedDevice, curPagePattern);
                 if (pages != null && !pages.isEmpty()) {

                     for (PXYPage page : pages) {
                         MeaPageView view = new MeaPageView(page, routine, curPagePattern);
                         views.add(view);
                     }

                 }
             }
             displayViews(views);
           
        }
    }

    protected void addPagePattern() {
        if (routine != null) {
            RoutinePatternManager routinePatternManager = RoutinePatternManager.getInstance();
            RoutinePattern routinePattern = routinePatternManager.getRoutinePattern(routine.device.getDeviceType());

            //AddPagePatternPanel addPagePatternPanel = new AddPagePatternPanel(routinePattern);
            final AddPagePatternPanel addPagePatternPanel = new AddPagePatternPanel(routinePattern,routine);
            String title = "Add Page Pattern(F1 for Help)";
            if(loadUI!=null && loadUI.equalsIgnoreCase("true")){
                title = "Add Page Pattern";
            }
            boolean result = DialogHelper.withChecker((JFrame) WindowManager.getDefault().getMainWindow(), title, addPagePatternPanel,
                    new PDAChecker() {
                        @Override
                        public String doCheck(PDADialogOption option) {
                            return addPagePatternPanel.getErrorInfo();
                        }
                    });

//            DialogDescriptor desc = new DialogDescriptor(addPagePatternPanel,
//                    "Add Page Pattern(F1 for Help)", true, DialogDescriptor.OK_CANCEL_OPTION,
//                    DialogDescriptor.OK_OPTION, null);
//            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result) {//result == NotifyDescriptor.OK_OPTION
//                //get addpagepattern and check it
//                EntityPagePattern addPagePattern = addPagePatternPanel.getPagePattern();
//                  if(!PatternUtil.checkPatternLegality(addPagePattern)){
//                    return;
//                }
//                
//                IndPagePattern indPagePattern = addPagePatternPanel.getIndPagePattern();
//                routine.addIndPagePattern(indPagePattern);
//                EntityPagePattern pagePattern = indPagePattern.pagePattern;
//                CheckableTreeNode patternNode = new CheckableTreeNode(pagePattern.getName(), pagePattern);
//                patternNode.setCheckable(true);
//                patternNode.setIcon(pageIcon);
//                pageRootNode.add(patternNode);
//                pageTree.updateUI();
//                pageTree.setSelectionPath(new TreePath(patternNode.getPath()));
                
                List<IndPagePattern> selectedIndPagePatterns = addPagePatternPanel.getSelectedIndPagePatterns();
                for(int index = 0;index<selectedIndPagePatterns.size();index++){
                    IndPagePattern indPagePattern = selectedIndPagePatterns.get(index);
                    routine.addIndPagePattern(indPagePattern);
                    EntityPagePattern pagePattern = indPagePattern.pagePattern;
                    CheckableTreeNode patternNode = new CheckableTreeNode(pagePattern.getName(), pagePattern);
                    patternNode.setCheckable(true);
                    patternNode.setIcon(pageIcon);
                    pageRootNode.add(patternNode);
                    pageTree.updateUI();
                    pageTree.setSelectionPath(new TreePath(patternNode.getPath()));
                }
              
            }
        }
    }
    
    protected void clearData() {
        for(EntityPagePattern pattern : routine.getPagePatterns()){
             routine.updatePagePattern(meaData, pattern);
        }
        
        int deviceIndex =  deviceTable.getSelectedRow();
        deviceIndex = deviceTable.convertRowIndexToModel(deviceIndex);
        EntityDevice curDevice = meaData.getDevice(deviceIndex);
        for(SpecPattern spec : routine.getSpecPatterns()){
//            routine.removeSpecPattern(meaData, spec);            
            meaData.removeSpecData(curDevice, spec);
        }
         
        updatePageTreeIcons();
        specTableModel.fireTableDataChanged();
        
    }
    
    protected void removePagePatterns(){
       List<EntityPagePattern> patterns = pack.getSelectedPagePatterns();
       for(int index =0;index<patterns.size();index++){
           EntityPagePattern pattern = patterns.get(index);
           if(pattern!=null){
               int patternIndexInRoutine = routine.getPagePatternIndex(pattern);
               routine.removePagePattern(meaData, pattern);             
               //delete from tree
                TreePath treePath = pageTree.getPathForRow(patternIndexInRoutine);
                if(treePath!=null){
                      CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
                      if(treeNode!=null){
                           pageTreeModel.removeNodeFromParent(treeNode);
                      }
                }
               
           }
       }
       //deselect all after removing
       pack.deSelectAllPages(routine);
       pageTree.updateUI();
       
    } 

    protected void removePagePattern() {
        TreePath treePath = pageTree.getSelectionPath();
        if (treePath != null) {
            CheckableTreeNode treeNode = (CheckableTreeNode) treePath.getLastPathComponent();
            Object obj = treeNode.getAssociatedObject();
            if (obj != null && obj instanceof EntityPagePattern) {
                EntityPagePattern pagePattern = (EntityPagePattern) obj;
                routine.removePagePattern(meaData, pagePattern);
                pageTreeModel.removeNodeFromParent(treeNode);
            }
        }
    }

    protected void addSpecPattern() {
        if (routine != null) {
            RoutinePatternManager routinePatternManager = RoutinePatternManager.getInstance();
            RoutinePattern routinePattern = routinePatternManager.getRoutinePattern(routine.device.getDeviceType());

            AddSpecPatternPanel addSpecPatternPanel = new AddSpecPatternPanel(routinePattern);

            DialogDescriptor desc = new DialogDescriptor(addSpecPatternPanel,
                    "Add Spec Pattern", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION, null);
            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result == NotifyDescriptor.OK_OPTION) {
//                IndSpecPattern indSpecPattern = addSpecPatternPanel.getIndSpecPattern();
//                boolean isAdded = routine.addIndSpecPattern(indSpecPattern);
//                if(!isAdded){                    
//                    logger.log(Level.INFO, "Add Spec Pattern:{0} is repeated!", new Object[]{indSpecPattern.specPattern.getName()});  
//                    final ConsoleImpl console = new ConsoleImpl("MeaSpacePanel");
//                    console.info("Add Spec Pattern:"+indSpecPattern.specPattern.getName()+" is repeated!");
//                }else{
//                     specTableModel.fireTableDataChanged();
//                }
                 List<IndSpecPattern> indSpecPatterns = addSpecPatternPanel.getSelectedIndSpecPatterns();
                 for (int index = 0; index < indSpecPatterns.size(); index++) {
                    IndSpecPattern indSpecPattern = indSpecPatterns.get(index);
                    boolean isAdded = routine.addIndSpecPattern(indSpecPattern);
                    if (!isAdded) {
                        logger.log(Level.INFO, "Add Spec Pattern:{0} is repeated!", new Object[]{indSpecPattern.specPattern.getName()});
                    } else {
                        specTableModel.fireTableDataChanged();
                    }
                }
               
            }
        }
    }

    protected void removeSpecPatterns(){
       List<SpecPattern> patterns =  pack.getSelectedSpecPatterns();
       for(int index = 0;index<patterns.size();index++){
           SpecPattern pattern = patterns.get(index);
           if(pattern!=null){
               int patternIndexInRoutine = routine.getSpecPatternIndex(pattern);
                routine.removeSpecPattern(meaData, pattern);  
                specTableModel.fireTableDataChanged();
                //JTableUtil.setSelectedIndex(specTable, specTableModel, index - 1);
               
           }
       }
       //deselect
       pack.deSelectAllSpecs(routine);
    }
    
    protected void removeSpecPattern() {
        int index = specTable.getSelectedRow();
        index = specTable.convertRowIndexToModel(index);
        if (index >= 0) {           
            SpecPattern specPattern = routine.getSpecPattern(index);
            routine.removeSpecPattern(meaData, specPattern);
            specTableModel.fireTableDataChanged();
            JTableUtil.setSelectedIndex(specTable, specTableModel, index - 1);
        }
    }

    public void addDevice() {
        int selectedIndex = deviceTable.getSelectedRow();
        int deviceIndex = deviceTable.convertRowIndexToModel(selectedIndex);

        EntityDevice device;
        if (deviceIndex >= 0) {
            device = meaData.getDevice(deviceIndex);
            device = (EntityDevice) device.clone();
        } else {
            device = DeviceTypeManager.getDefaultDevice(meaData);
        }

        meaData.addDevice(device);
        deviceTableModel.fireTableDataChanged();
        routine = meaData.getRoutineByDevice(device);
        JTableUtil.setSelectedIndex(deviceTable, deviceTableModel, meaData.getDeviceNumber() - 1);
        DUTPack pack = new DUTPack(routine);
        packs.add(pack);
        deviceHighlighted();
    }

    @Override
    public void start() {
    }

    @Override
    public void finish() {
        updatePageTreeIcons();
        if (EventQueue.isDispatchThread()) {
            specTableModel.fireTableDataChanged();
            return;
        }
        SwingUtil.invokeLater(new Runnable() {
            @Override
            public void run() {
                specTableModel.fireTableDataChanged();
            }
        });
    }
    
    public DUTPack getPack() {
        return pack;
    }

    public DUTPacks getPacks() {
        return new DUTPacks(packs);
    }
    
    /////////////Spec21///////////////////////////////////
     final class Spec21ValueTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {          
            return Test21Action.SpecMap.size();
        }

        @Override
        public int getColumnCount() {
            return specColumnNames.length-1;
        }

        @Override
        public String getColumnName(int column) {
            return specColumnNames[column+1];
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {          

            return super.getColumnClass(columnIndex);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Map.Entry<String,Double> entry = Test21Action.getSpecByIndex(rowIndex);
            if(entry == null){
                return "";
            }
            
            if(columnIndex == 0){
                return entry.getKey();
            }else if (columnIndex == 1){
                return String.format("%-6g", entry.getValue());
            }
            
            return "";
            
        }
        
    
    }
    
    /////////////Sepc Pattern///////////////////////////////////////////
    public static String[] specColumnNames = new String[]{"Selected","Spec", "Value"};//"Selected"

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
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            }

            return super.getColumnClass(columnIndex);
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

    static class InstancePanel extends JPanel {

        private JTextField nameField;
        private JTextField valueField;

        InstancePanel() {
            super(new BorderLayout(5, 5));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            nameField = new JTextField(40);
            valueField = new JTextField(40);

            JPanel westPanel = new JPanel(new GridLayout(0, 1, 2, 2));
            JPanel fieldPanel = new JPanel(new GridLayout(0, 1, 2, 2));

            westPanel.add(new JLabel("Name: "));
            fieldPanel.add(nameField);

            westPanel.add(new JLabel("Value: "));
            fieldPanel.add(valueField);

            add(westPanel, BorderLayout.WEST);
            add(fieldPanel, BorderLayout.CENTER);
        }

        public void setNameAndValue(String name, String value) {
            nameField.setText(name);
            nameField.setEditable(false);
            valueField.setText(value);
        }

        public String getInstName() {
            return nameField.getText().trim();
        }

        public String getInstValue() {
            return valueField.getText().trim();
        }
    }
}
