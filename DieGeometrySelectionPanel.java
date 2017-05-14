/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.api.Probe;
import com.platformda.utility.common.MathUtil;
import com.platformda.utility.ui.GUIUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.commons.lang.ArrayUtils;
import org.openide.util.ImageUtilities;

/**
 * selects dies by geometry
 *
 * @author Junyi
 */
public class DieGeometrySelectionPanel extends JPanel {

    DieHighlighter dieHighlighter;
    WaferInfo waferInfo;
    List<WaferDieInfo> dieInfos = new ArrayList<WaferDieInfo>();
//    List<WaferDieInfo> selectedDieInfos = new ArrayList<WaferDieInfo>();
    List<WaferDieInfo> selectedDieInfos;
    StatusProvider statusProvider;
    //
    int[] xValues;
    int[] yValues;
    DieGeometrySelectionTableModel tableModel;
    JTable table;
    JLabel selectionLabel = new JLabel("");
    JComboBox<String> orderCombo;

    public DieGeometrySelectionPanel(DieHighlighter dieHighlighter, WaferInfo waferInfo, List<WaferDieInfo> dieInfos, List<WaferDieInfo> selectedDieInfos, StatusProvider statusProvider) {
        this.dieHighlighter = dieHighlighter;
        this.waferInfo = waferInfo;
        this.dieInfos.addAll(dieInfos);
//        this.selectedDieInfos.addAll(selectedDieInfos);
        this.selectedDieInfos = selectedDieInfos;
        this.statusProvider = statusProvider;
        List<WaferDieInfo> allDieInfos = waferInfo.getDieInfos();
        List<Integer> distincts = getDistinctXValues(allDieInfos);
        xValues = new int[distincts.size()];
        for (int i = 0; i < xValues.length; i++) {
            xValues[i] = distincts.get(i);
        }
        distincts = getDistinctYValues(allDieInfos);
        yValues = new int[distincts.size()];
        for (int i = 0; i < yValues.length; i++) {
            yValues[i] = distincts.get(i);
        }
        reorder(xValues, yValues, Probe.ORDER_X_ASEND_Y_ASEND, waferInfo.getOrder());
        initComponents();
    }

    private void initComponents() {
        tableModel = new DieGeometrySelectionTableModel();
        table = new JTable(tableModel);
        table.setDefaultRenderer(Boolean.class, new DieCellRenderer());
        table.setDefaultEditor(Boolean.class, new DieCellEditor(new JCheckBox()));
//        table.setColumnSelectionAllowed(true);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

//        table.setTableHeader(null);
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    int[] rows = table.getSelectedRows();
                    int[] columns = table.getSelectedColumns();
                    if (rows.length == 1 && columns.length == 1) {
                        int row = table.getSelectedRow();
                        int column = table.getSelectedColumn();
                        if (column == 0) {
                            List<WaferDieInfo> dieInfos = new ArrayList<WaferDieInfo>();
                            boolean hasSelected = false;

                            int y = yValues[row];
                            for (int columnIndex = 0; columnIndex < xValues.length; columnIndex++) {
                                int x = xValues[columnIndex];
                                WaferDieInfo dieInfo = getDieInfo(x, y);
                                if (dieInfo != null) {
                                    dieInfos.add(dieInfo);
                                    if (selectedDieInfos.contains(dieInfo)) {
                                        hasSelected = true;
                                    }
                                }
                            }
                            selectedDieInfos.removeAll(dieInfos);
                            if (hasSelected) {
                            } else {
                                selectedDieInfos.addAll(dieInfos);
                            }

                            tableModel.fireTableDataChanged();
                            updateSelectionLabel();
                        }

                    }
                } else if (e.isMetaDown()) {
                } else {
                    dieHighlighted();
                }
            }
        };

        table.addMouseListener(mouseAdapter);
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                dieHighlighted();
            }
        });
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    int column = table.columnAtPoint(e.getPoint());
                    if (column == 0) {
                        if (selectedDieInfos.isEmpty()) {
                            selectedDieInfos.addAll(dieInfos);
                        } else {
                            selectedDieInfos.clear();
                        }
                    } else {
                        List<WaferDieInfo> dieInfos = new ArrayList<WaferDieInfo>();
                        boolean hasSelected = false;
                        int x = xValues[column - 1];
                        for (int rowIndex = 0; rowIndex < yValues.length; rowIndex++) {
                            int y = yValues[rowIndex];
                            WaferDieInfo dieInfo = getDieInfo(x, y);
                            if (dieInfo != null) {
                                dieInfos.add(dieInfo);
                                if (selectedDieInfos.contains(dieInfo)) {
                                    hasSelected = true;
                                }
                            }
                        }
                        selectedDieInfos.removeAll(dieInfos);
                        if (hasSelected) {
                        } else {
                            selectedDieInfos.addAll(dieInfos);
                        }
                    }
                    tableModel.fireTableDataChanged();
                    updateSelectionLabel();
                }
            }
        });

        JScrollPane deviceScroll = new JScrollPane(table);
        deviceScroll.setBorder(null);
        GUIUtil.updateBackground(deviceScroll);

        final JButton selectAllDevicesButton = new JButton("");
        final JButton deselectAllDevicesButton = new JButton("");

        final JButton selectHighlightsDevicesButton = new JButton("");
        final JButton deselectHighlightsButton = new JButton("");

        selectAllDevicesButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/checked.png")));
        deselectAllDevicesButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/unchecked.png")));
        selectAllDevicesButton.setToolTipText("Select all dies");
        deselectAllDevicesButton.setToolTipText("Unselect all dies");

        selectAllDevicesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDieInfos.clear();
                selectedDieInfos.addAll(dieInfos);

                tableModel.fireTableDataChanged();
                updateSelectionLabel();
            }
        });
        deselectAllDevicesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedDieInfos.clear();
                tableModel.fireTableDataChanged();
                updateSelectionLabel();
            }
        });
        selectHighlightsDevicesButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/selectHighlight.png")));
        deselectHighlightsButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/deselectHighlight.png")));
        selectHighlightsDevicesButton.setToolTipText("Select all highlighted dies");
        deselectHighlightsButton.setToolTipText("Unselect all highlighted dies");

        selectHighlightsDevicesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = table.getSelectedRows();
                int[] columns = table.getSelectedColumns();
                for (int rowIndex : rows) {
                    for (int columnIndex : columns) {
                        setSelected(xValues[columnIndex - 1], yValues[rowIndex], true);
                    }
                }
                tableModel.fireTableDataChanged();

                updateSelectionLabel();
            }
        });
        deselectHighlightsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = table.getSelectedRows();
                int[] columns = table.getSelectedColumns();
                for (int rowIndex : rows) {
                    for (int columnIndex : columns) {
                        setSelected(xValues[columnIndex - 1], yValues[rowIndex], true);
                    }
                }
                tableModel.fireTableDataChanged();
                updateSelectionLabel();
            }
        });

        final JButton invertButton = new JButton("");
        invertButton.setIcon(new ImageIcon(ImageUtilities.loadImage(
                "com/platformda/mea/resources/invert.png")));
        invertButton.setToolTipText("Invert select");
        invertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<WaferDieInfo> total = new ArrayList<WaferDieInfo>(dieInfos);
                total.removeAll(selectedDieInfos);
                selectedDieInfos.clear();
                selectedDieInfos.addAll(total);
                tableModel.fireTableDataChanged();
                updateSelectionLabel();
            }
        });

        JToolBar toolbar = new JToolBar();
        toolbar.add(selectAllDevicesButton);
        toolbar.add(deselectAllDevicesButton);
        toolbar.add(invertButton);
        toolbar.addSeparator();
        toolbar.add(selectHighlightsDevicesButton);
        toolbar.add(deselectHighlightsButton);

        toolbar.setFloatable(false);
        GUIUtil.updateBackground(toolbar);

        JPanel deviceToolbarPanel = new JPanel(new BorderLayout());
        deviceToolbarPanel.add(toolbar, BorderLayout.WEST);
        deviceToolbarPanel.add(selectionLabel, BorderLayout.EAST);
        selectionLabel.setToolTipText("selected die number/ total die number");

        orderCombo = new JComboBox<String>(new DefaultComboBoxModel(Probe.orders));
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
                    reorder(xValues, yValues, waferInfo.getOrder(), order);
                    waferInfo.setOrder(order);
//                    tableModel.fireTableDataChanged();
                    tableModel.fireTableStructureChanged();
                }
            }
        });

        setLayout(new BorderLayout());
        add(deviceScroll, BorderLayout.CENTER);
        add(deviceToolbarPanel, BorderLayout.NORTH);
        add(orderPanel, BorderLayout.SOUTH);
        updateSelectionLabel();
    }

    public void update(List<WaferDieInfo> ds, List<WaferDieInfo> sds, StatusProvider statusProvider) {
        dieInfos.clear();
        this.dieInfos.addAll(ds);
//        selectedDieInfos.clear();
//        this.selectedDieInfos.addAll(sds);
        this.statusProvider = statusProvider;
        tableModel.fireTableDataChanged();
    }

    public void selectedDiesUpdated(List<WaferDieInfo> sds) {
//        selectedDieInfos.clear();
//        this.selectedDieInfos.addAll(sds);
        tableModel.fireTableDataChanged();
    }

    public void selectDieInfo(WaferDieInfo dieInfo) {
        // TODO: select cell
        int x = dieInfo.getX();
        int y = dieInfo.getY();

//        int row = MathUtil.indexOf(y, yValues);
//        int column = MathUtil.indexOf(x, xValues) + 1;
        int row = ArrayUtils.indexOf(yValues, y);
        int column = ArrayUtils.indexOf(xValues, x) + 1;

        row = table.convertRowIndexToView(row);
        column = table.convertColumnIndexToView(column);
        table.setRowSelectionInterval(row, row);
        table.setColumnSelectionInterval(column, column);
    }

    public void dieHighlighted() {
        if (dieHighlighter != null) {
            int row = table.getSelectedRow();
            int column = table.getSelectedColumn();
            if (row >= 0 && column >= 0) {
                row = table.convertRowIndexToModel(row);
                column = table.convertColumnIndexToModel(column);
                if (column >= 1) {
                    int y = yValues[row];
                    int x = xValues[column - 1];
                    WaferDieInfo dieInfo = getDieInfo(x, y);
                    dieHighlighter.dieHighlighted(dieInfo);
                }
            }
        }
    }

    public WaferDieInfo getDieInfo(int x, int y) {
        for (WaferDieInfo dieInfo : dieInfos) {
            if (dieInfo.getX() == x && dieInfo.getY() == y) {
                return dieInfo;
            }
        }
        return null;
    }

    public static void reorder(int[] xValues, int[] yValues, int preOrder, int order) {
        int[] orders = new int[2];
        orders[0] = preOrder;
        orders[1] = order;
        for (int o : orders) {
            switch (o) {
                case Probe.ORDER_X_ASEND_Y_ASEND:
                    break;
                case Probe.ORDER_X_ASEND_Y_DESEND:
                    revert(yValues);
                    break;
                case Probe.ORDER_X_DESEND_Y_ASEND:
                    revert(xValues);
                    break;
                case Probe.ORDER_X_DESEND_Y_DESEND:
                    revert(xValues);
                    revert(yValues);
            }
        }
    }

    public static void revert(int[] array) {
        int ylength = array.length;
        for (int i = 0; i < ylength / 2; i++) {
            int temp = array[i];
            array[i] = array[ylength - i - 1];
            array[ylength - i - 1] = temp;
        }
    }

    protected void updateSelectionLabel() {
        selectionLabel.setText(String.format("%d/%d", selectedDieInfos.size(), dieInfos.size()));
    }

    public Boolean hasSelected(int x, int y) {
        WaferDieInfo dieInfo = getDieInfo(x, y);
        if (dieInfo == null) {
            return null;
        }
        return selectedDieInfos.contains(dieInfo);
    }

    public void setSelected(int x, int y, boolean selected) {
        WaferDieInfo dieInfo = getDieInfo(x, y);
        if (dieInfo == null) {
            return;
        }
        if (selected) {
            selectedDieInfos.add(dieInfo);
        } else {
            selectedDieInfos.remove(dieInfo);
        }
    }

    public List<WaferDieInfo> getSelectedDieInfos() {
        return selectedDieInfos;
    }

    public static List<Integer> getDistinctXValues(List<WaferDieInfo> dieInfos) {
        List<Integer> distincts = new ArrayList<Integer>();
        for (WaferDieInfo dieInfo : dieInfos) {
            int value = dieInfo.getX();
            boolean existed = false;
            for (Integer integer : distincts) {
                if (value == integer.intValue()) {
                    existed = true;
                    break;
                }
            }
            if (!existed) {
                distincts.add(value);
            }
        }
        Collections.sort(distincts);
        return distincts;
    }

    public static List<Integer> getDistinctYValues(List<WaferDieInfo> dieInfos) {
        List<Integer> distincts = new ArrayList<Integer>();
        for (WaferDieInfo dieInfo : dieInfos) {
            int value = dieInfo.getY();
            boolean existed = false;
            for (Integer integer : distincts) {
                if (value == integer.intValue()) {
                    existed = true;
                    break;
                }
            }
            if (!existed) {
                distincts.add(value);
            }
        }

        Collections.sort(distincts);
        return distincts;
    }

    class CheckBoxIcon implements Icon {

        Color color;

        public CheckBoxIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component component, Graphics g, int x, int y) {
            AbstractButton abstractButton = (AbstractButton) component;
            ButtonModel buttonModel = abstractButton.getModel();
            g.setColor(color);
            g.drawRect(1, 1, 20, 20);
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }
    }

    final class DieCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component comp = null;
            if (value == null) {
                JLabel label = new JLabel("");
                label.setOpaque(true);
                comp = label;
            } else {
                WaferDieInfo dieInfo = getDieInfo(xValues[column - 1], yValues[row]);
                int status = dieInfo.getStatus();
                if (statusProvider != null) {
                    status = statusProvider.getStatus(dieInfo);
                }
                Color color = WaferDieInfo.COLORS[status];
                JCheckBox checkBox = new JCheckBox(String.valueOf(dieInfo.getDieIndex()));
                if (color != null) {
                    checkBox.setForeground(color);
                }
                checkBox.setSelected(Boolean.valueOf(value.toString()));
                checkBox.setHorizontalAlignment(JCheckBox.CENTER);
                comp = checkBox;
            }
            if (isSelected) {
                if (value == null) {
                    comp.setForeground(UIManager.getColor("Table.selectionForeground"));
                }
                comp.setBackground(UIManager.getColor("Table.selectionBackground"));
            } else {
                if (value == null) {
                    comp.setForeground(table.getForeground());
                }
                comp.setBackground(table.getBackground());
            }

            return comp;
        }
    }

    final class DieCellEditor extends DefaultCellEditor {

        JLabel label;
        JCheckBox checkBox;

        public DieCellEditor(JCheckBox checkBox) {
            super(checkBox);
            label = new JLabel("");
            label.setOpaque(true);
            this.checkBox = checkBox;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component comp = null;
            if (value == null) {
                comp = label;
            } else {
                comp = checkBox;

                WaferDieInfo dieInfo = getDieInfo(xValues[column - 1], yValues[row]);
                int status = dieInfo.getStatus();
                if (statusProvider != null) {
                    status = statusProvider.getStatus(dieInfo);
                }
                Color color = WaferDieInfo.COLORS[status];
                checkBox.setText(String.valueOf(dieInfo.getDieIndex()));
                if (color != null) {
                    checkBox.setForeground(color);
                }

                checkBox.setSelected(Boolean.valueOf(value.toString()));
                checkBox.setHorizontalAlignment(JCheckBox.CENTER);
            }
            if (isSelected) {
                if (value == null) {
                    comp.setForeground(UIManager.getColor("Table.selectionForeground"));
                }
                comp.setBackground(UIManager.getColor("Table.selectionBackground"));
            } else {
                if (value == null) {
                    comp.setForeground(table.getForeground());
                }
                comp.setBackground(table.getBackground());
            }
            return comp;
        }

        @Override
        public Object getCellEditorValue() {
            return checkBox.isSelected();
        }
    }

    final class DieGeometrySelectionTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            if (yValues == null) {
                return 0;
            }
            return yValues.length;
//            return ySweepConsts.length + 1;
        }

        @Override
        public int getColumnCount() {
            if (xValues == null) {
                return 0;
            }
            return xValues.length + 1;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex != 0) {
                return Boolean.class;
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "";
            }
            int x = xValues[columnIndex - 1];
            return String.format("x=%d", x);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                int y = yValues[rowIndex];
                return String.format("y=%d", y);
            }

            return hasSelected(xValues[columnIndex - 1], yValues[rowIndex]);
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            boolean selected = Boolean.valueOf(value.toString());
            setSelected(xValues[columnIndex - 1], yValues[rowIndex], selected);
            updateSelectionLabel();
        }
    }
}
