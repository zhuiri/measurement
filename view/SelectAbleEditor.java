/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;



import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 *
 * @author renjing
 */
public class SelectAbleEditor extends JTable {

    private JPopupMenu popup;

    public SelectAbleEditor(boolean autoSort) {
        super(new Model(autoSort));
        setShowVerticalLines(false);
        TableUtil.configTableColumeWidth(this, 1, 20, 20, 20);
        setTableHeader(null);
        configPopup();
        setComponentPopupMenu(popup);
        setDefaultRenderer(ThreeStateCheckBox.SelectState.class, new Renderer());
        setDefaultEditor(ThreeStateCheckBox.SelectState.class, new Editor());
    }

    private void configPopup() {
        Action checkAll = new AbstractAction("Select All") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllSelect(true);
            }
        };
        Action UncheckAll = new AbstractAction("Deselect All") {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAllSelect(false);
            }
        };
        popup = new JPopupMenu();
        popup.add(checkAll);
        popup.add(UncheckAll);
//        popup.add(CheckAllSelected);
//        popup.add(UncheckAllSelected);
    }


    public void setAllSelect(boolean select) {
        ((Model) getModel()).AllSelect(select);
    }

    public void setCheckerInFront(boolean infront) {
        if (infront) {
            if (convertColumnIndexToModel(0) == 0) {
                getColumnModel().moveColumn(0, 1);
            }
        } else {
            if (convertColumnIndexToModel(0) != 0) {
                getColumnModel().moveColumn(0, 1);
            }
        }
    }

    public void setValues(Collection<? extends SelectAble> values) {
        ((Model) getModel()).setValues(values);
    }

    private static class Model extends AbstractTableModel {

        private List<SelectAble> values = new ArrayList<>();
        private final Comparator<SelectAble> cp = new Comparator<SelectAble>() {
            @Override
            public int compare(SelectAble o1, SelectAble o2) {
                return o1.getShowName().compareTo(o2.getShowName());
            }
        };
        private final boolean autoSort;

        private Model(boolean autoSort) {
            this.autoSort = autoSort;
        }

        @Override
        public int getRowCount() {
            return values.size();
        }

        public void setValues(Collection<? extends SelectAble> v) {
            this.values.clear();
            values.addAll(v);
            if (autoSort) {
                Collections.sort(values, cp);
            }
            fireTableDataChanged();
        }

        public void AllSelect(boolean select) {
            for (SelectAble sa : values) {
                sa.setSelected(select ? ThreeStateCheckBox.SelectState.SELECT : ThreeStateCheckBox.SelectState.UN_SELECT);
            }
            fireTableDataChanged();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? String.class : ThreeStateCheckBox.SelectState.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return columnIndex == 0 ? values.get(rowIndex).getShowName()
                    : values.get(rowIndex).getSelectSate();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                values.get(rowIndex).setSelected((ThreeStateCheckBox.SelectState) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }

    static class Renderer extends ThreeStateCheckBox  implements TableCellRenderer, UIResource {

        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public Renderer() {
            super("");
            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setCheckState((SelectState) value);

            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            } else {
                setBorder(noFocusBorder);
            }

            return this;
        }
    }

    static class Editor extends AbstractCellEditor implements TableCellEditor {

        private ThreeStateCheckBox comp = new ThreeStateCheckBox("");

        public Editor() {
            comp.setOpaque(true);
            comp.setBorder(new EmptyBorder(1, 1, 1, 1));
            comp.setHorizontalAlignment(JLabel.CENTER);
            comp.setBorderPainted(true);
            comp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    stopCellEditing();
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return comp.getState();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            comp.setCheckState((ThreeStateCheckBox.SelectState) value);
            return comp;
        }
    }
}