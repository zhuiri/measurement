/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.admin.VarProviderEditingTableModel.VarIconProvider;
import com.platformda.utility.Iconable;
import com.platformda.utility.common.BasicFileFilter;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.common.VarProvider;
import com.platformda.utility.common.XMLUtil;
import com.platformda.utility.table.IconableCellRenderer;
import com.platformda.utility.ui.JTableUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * // TODO: move to somewhere
 *
 * @author junyi
 */
public class VarProviderEditorImpl extends JPanel implements ActionListener, VarIconProvider {

    public static final String KEY = "Var";
    VarProvider varProvider;
    String importSourceName;
    VarProvider importSourceVarProvider;//  // if provided, import from button be displayed instead of write protected button
    VarProviderEditingTableModel tableModel;
    JTable table;
    JButton addButton;
    JButton removeButton;
    JButton importButton;
    JButton exportButton;
    JButton importFromButton;
//    JToggleButton writeProtectedButton;
    protected boolean inSimpleMode = false; // if true, no buttons at all
    String referenceName;
    VarProvider referenceVP; // if provided, reference column will be displayed in table
    VarSelector varSelector;
//    static ImageIcon tweakingIcon = new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/config-badge.gif"));
    static ImageIcon tweakingIcon = new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/base-template-badge.png"));
    List<String> tweakingVarNames = new ArrayList<String>();
    
    String patternName = null;

     public VarProviderEditorImpl(String patternName,VarProvider varProvider, VarSelector varSelector) {
        this(varProvider,varSelector);
        this.patternName = patternName;       
    }
    
    public VarProviderEditorImpl(VarProvider varProvider, VarSelector varSelector) {
        this(varProvider, null, null, false, null, null, varSelector);
    }
    
  

    public VarProviderEditorImpl(VarProvider varProvider, String importSourceName, VarProvider importSourceVarProvider, boolean inSimpleMode, String referenceName, VarProvider referenceVP, VarSelector varSelector) {
        super();
        this.varProvider = varProvider;
        this.importSourceName = importSourceName;
        this.importSourceVarProvider = importSourceVarProvider;
        this.inSimpleMode = inSimpleMode;
        this.referenceName = referenceName;
        this.referenceVP = referenceVP;
        this.varSelector = varSelector;

        initComponents();
    }

    private void initComponents() {
        if(patternName!=null){
            tableModel = new VarProviderEditingTableModel(patternName,varProvider, referenceName, referenceVP, this); 
        }else{
            tableModel = new VarProviderEditingTableModel(varProvider, referenceName, referenceVP, this);
        }       
        table = new JTable(tableModel);
        table.setDefaultRenderer(Iconable.class, new IconableCellRenderer(SwingConstants.LEFT));
        JScrollPane scrollPane = new JScrollPane(table);

        addButton = new JButton("New Var...");
        removeButton = new JButton("Remove Var");
        importButton = new JButton("Import...");
        if (importSourceName != null && importSourceVarProvider != null) {
            importFromButton = new JButton("Import from " + importSourceName);
            importFromButton.addActionListener(this);
        }
//        exportButton = new JButton("Export", exportIcon);
        exportButton = new JButton("Export...");
//        writeProtectedButton = new JToggleButton("Write Protected");

        JPanel buttonWrapPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        if (inSimpleMode) {
//            buttonPanel.add(importButton);
//            buttonPanel.add(exportButton);
        } else {
//            buttonPanel.add(addButton);
//            buttonPanel.add(removeButton);
            buttonPanel.add(importButton);
            buttonPanel.add(exportButton);
            if (importFromButton != null) {
                buttonPanel.add(importFromButton);
            } else {
//                buttonPanel.add(writeProtectedButton);
            }
        }
        buttonWrapPanel.add(buttonPanel, BorderLayout.NORTH);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(scrollPane, BorderLayout.CENTER);
        //add(buttonWrapPanel, BorderLayout.EAST);

        if (varSelector != null) {
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isMetaDown()) {
                    return;
                }
                highlightChanged();
            }
        });
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                highlightChanged();
            }
        });

        importButton.setActionCommand("Import");
        importButton.addActionListener(this);
        exportButton.addActionListener(this);

        removeButton.setEnabled(false);

        addButton.addActionListener(this);
        removeButton.addActionListener(this);
    }

    protected void highlightChanged() {
        removeButton.setEnabled(true);
        int index = table.getSelectedRow();
        if (index < 0 || index >= varProvider.getVarNumber()) {
            removeButton.setEnabled(false);
            if (varSelector != null) {
                varSelector.onVarSelected(null);
            }
        } else {
            if (varSelector != null) {
                index = table.convertRowIndexToModel(index);
                String varName = tableModel.getVarName(index);
                varSelector.onVarSelected(varName);
            }
        }
    }

//    @Override
    public Component getEditorComponent() {
        return this;
    }

    public JTable getTable() {
        return table;
    }
    
    public VarProviderEditingTableModel getTableModel(){
        return this.tableModel;
    }

//    @Override
    public void stopEditing() {
        TableCellEditor cellEditor = table.getCellEditor();
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }
    }

    public void save(File file) {
        Element elem = varProvider.exportTo("Vars");
        Format fmt = Format.getPrettyFormat();
        fmt.setIndent("  ");
        fmt.setEncoding("gb2312");
        XMLOutputter outputtter = new XMLOutputter(fmt);
        try {
            FileWriter writer = new FileWriter(file);
            outputtter.output(elem, writer);
            writer.flush();
            writer.close();
        } catch (IOException ex) {
        }
    }

//    @Override
    public void cancelEditing() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object eventSource = e.getSource();
        if (eventSource.equals(addButton)) {
            VarPanel form = new VarPanel();
            DialogDescriptor desc = new DialogDescriptor(form,
                    "New Var", true, DialogDescriptor.OK_CANCEL_OPTION,
                    DialogDescriptor.OK_OPTION, null);
            desc.setValid(true);
            Object result = DialogDisplayer.getDefault().notify(desc); // displays the dialog
            if (result == NotifyDescriptor.OK_OPTION) {
                String name = form.getVarName();
                String valueStr = form.getVarValueStr();
                if (name.isEmpty() || valueStr.isEmpty()) {
                    return;
                } else {
                    try {
                        if (valueStr.indexOf(",") != -1) {
                            String[] parts = valueStr.split(",");
                            List<Double> values = new ArrayList<Double>();
                            for (int i = 0; i < parts.length; i++) {
                                values.add(Double.parseDouble(parts[i].trim()));
                            }
                            varProvider.setVar(name, values);
                        } else {
                            Double d = Double.parseDouble(valueStr);
                            varProvider.setVar(name, d);
                        }
                    } catch (NumberFormatException nfe) {
                        varProvider.setVar(name, valueStr);
                    }
                    tableModel.onVarChanged();
                }
            }
        } else if (eventSource.equals(removeButton)) {
            int index = table.getSelectedRow();
            index = table.convertRowIndexToModel(index);
            varProvider.removeVar(tableModel.getVarName(index));
//            removeButton.setEnabled(false);
            tableModel.onVarChanged();
            index--;
            if (index < 0 || index >= varProvider.getVarNumber()) {
                index = 0;
            }
            JTableUtil.setSelectedIndex(table, tableModel, index);
            highlightChanged();
        } else if (eventSource.equals(importButton)) {
            File file = LoadSaveUtil.openFile("Import Var", JFileChooser.FILES_ONLY, KEY, null, false, new String[]{"xml"}, false, true);
            if (file != null) {
                Element rootElement = null;
                try {
                    rootElement = XMLUtil.getRoot(file.getAbsolutePath());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (rootElement != null) {
                    varProvider.clearVars();
                    varProvider.importFrom(rootElement);
                    removeButton.setEnabled(false);
                    tableModel.onVarChanged();
                }
            }
        } else if (eventSource.equals(importFromButton)) {
            if (importSourceVarProvider != null) {
//                varProvider.clearVars();
                List<String> existingVarNames = varProvider.getVarNames();
                List<String> specVarNames = importSourceVarProvider.getVarNames();
                for (String varName : specVarNames) {
                    if (existingVarNames.contains(varName)) {
                    } else {
                        varProvider.setVar(varName, importSourceVarProvider.getVar(varName));
                    }
                }
                tableModel.onVarChanged();
            }
        } else if (eventSource.equals(exportButton)) {
//            File file = LoadSaveUtil.saveFile("Export Var", JFileChooser.FILES_ONLY, ExtractionProject.KEY_DIR_INI_VAR_SAVE);
            final File file = LoadSaveUtil.saveFile("Export Var", JFileChooser.FILES_ONLY, KEY, null, new BasicFileFilter("xml"), "var.xml", false);
            if (file != null) {
                save(file);
            }
        }
    }

    public void updateView() {
//        tableModel.fireTableDataChanged();
        tableModel.onVarChanged();
    }

    public void onVarTweaking(List<String> varNames) {
        int index = table.getSelectedRow();
        tweakingVarNames.clear();
        tweakingVarNames.addAll(varNames);
        tableModel.fireTableDataChanged();
        if (index >= 0) {
            JTableUtil.setSelectedIndex(table, tableModel, index);
        }
    }

    @Override
    public Icon getIcon(String varName) {
        if (StringUtil.contains(tweakingVarNames, varName)) {
            return tweakingIcon;
        }
        return null;
    }

    static class VarPanel extends JPanel {

        private JTextField nameField;
        private JTextField valueField;

        VarPanel() {
            super(new GridLayout(0, 2, 5, 5));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            nameField = new JTextField(16);

            valueField = new JTextField(16);

            add(new JLabel("Name: "));
            add(nameField);

            add(new JLabel("Value: "));
            add(valueField);
        }

        public String getVarName() {
            return nameField.getText().trim();
        }

        public String getVarValueStr() {
            return valueField.getText().trim();
        }
    }

    public static interface VarSelector {

        public void onVarSelected(String var);
    }

    class JComponentTableCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return (JComponent) value;
        }
    }
}
