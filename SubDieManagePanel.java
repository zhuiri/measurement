/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.api.SubDieMeta;
import com.platformda.iv.api.Bus;
import com.platformda.iv.api.BusException;
import com.platformda.iv.api.Line;
import com.platformda.iv.api.Probe;
import com.platformda.iv.api.Profile;
//import com.platformda.mea.instrument.probe.pa300.PA300;
import com.platformda.utility.common.FileUtil;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.utility.ui.JTableUtil;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;
import org.netbeans.swing.etable.ETable;
import org.openide.util.Exceptions;

/**
 *
 * @author Junyi
 */
public class SubDieManagePanel extends JPanel implements ActionListener {

    public static String[] columnNames = {"Index", "X offset", "Y offset", "Name"};
    List<SubDieMeta> metas = new ArrayList<SubDieMeta>();
    SubDieTableModel tableModel;
    ETable table;
    JButton addButton = new JButton("Add");
    JButton removeButton = new JButton("Remove");
    JButton loadButton; // load .stv
//    JButton exportButton; // export to .stv
    JButton downloadButton = new JButton("Download"); // refresh subdies from probe
    JButton uploadButton = new JButton("Upload"); // update to probe
    //
    DeviceManagePanel deviceManagePanel;

    public SubDieManagePanel(DeviceManagePanel deviceManagePanel) {
        this.deviceManagePanel = deviceManagePanel;
        initComponents();
    }

    private void initComponents() {
        tableModel = new SubDieTableModel();
        table = new ETable(tableModel);
        JScrollPane tableScroll = new JScrollPane(table);

        loadButton = new JButton("Load");
//        loadButton.setToolTipText("Load .stv");
        loadButton.setToolTipText("Load sub dies from file");
        downloadButton.setToolTipText("Download/Refresh sub dies from probe");
        uploadButton.setToolTipText("Upload sub dies to probe");

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(loadButton);
//        buttonPanel.add(exportButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(uploadButton);

        addButton.addActionListener(this);
        removeButton.addActionListener(this);
        loadButton.addActionListener(this);
//        exportButton.addActionListener(this);
        downloadButton.addActionListener(this);
        uploadButton.addActionListener(this);

        JPanel buttonWrapPanel = new JPanel(new BorderLayout(5, 5));
        buttonWrapPanel.add(buttonPanel, BorderLayout.NORTH);

        setLayout(new BorderLayout(5, 5));
        add(tableScroll, BorderLayout.CENTER);
        add(buttonWrapPanel, BorderLayout.EAST);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();

        if (s == addButton) {
            SubDieMeta meta = new SubDieMeta();

            int index = table.getSelectedRow();
            index = table.convertRowIndexToModel(index);
            if (index >= 0) {
                SubDieMeta target = metas.get(index);
                meta.index = target.index + 1;
                shift(meta.index, 1);
                metas.add(meta.index, meta);
            } else {
                meta.index = metas.size();
                metas.add(meta);
            }
            tableModel.fireTableDataChanged();

            JTableUtil.setSelectedIndex(table, tableModel, index + 1);

        } else if (s == removeButton) {
            int index = table.getSelectedRow();
            index = table.convertRowIndexToModel(index);
            if (index >= 0) {
                SubDieMeta target = metas.get(index);
                shift(target.index, -1);
                metas.remove(index);
                tableModel.fireTableDataChanged();
                if (!metas.isEmpty()) {
                    JTableUtil.setSelectedIndex(table, tableModel, index - 1);
                }
            }
        } else if (s == loadButton) {
            try {
//                loadSTV();
                load();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (s == downloadButton) {
            try {
                download();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (BusException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else if (s == uploadButton) {
            try {
                upload();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (BusException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    protected void load() throws IOException {
        EntityInstrument entityProbe = InstrumentManager.getInstance().getProbe();
        Probe probe = (Probe) entityProbe.getInstrument();
        String ext = probe.getSubDieSettingsExtension();
        // load file
//        File file = LoadSaveUtil.openFile("Load ." + ext, JFileChooser.FILES_ONLY, ext.toUpperCase(), null, false);
        File file = LoadSaveUtil.openFile("Load sub dies from file", JFileChooser.FILES_ONLY, ext.toUpperCase(), null, false);
        if (file == null) {
            return;
        }

        List<SubDieMeta> subs = probe.loadSubDies(file);
        metas.clear();
        metas.addAll(subs);

        tableModel.fireTableDataChanged();
    }

    protected void download() throws IOException, BusException {
        EntityInstrument entityProbe = InstrumentManager.getInstance().getProbe();

        Probe probe = (Probe) entityProbe.getInstrument();
        Profile conf = entityProbe.getProfile();
        probe.setProfile(conf);
        Bus bus = null;
        Line line = null;
        String[] subDieNames = null;
        try {
            if (probe.isOnGlobalBus()) {
                bus = InstrumentManager.getInstance().createBus();
                bus.open();
                line = bus.connect(conf.getAddress());
                probe.setLine(line);
            }
            probe.open();

            List<SubDieMeta> subs = new ArrayList<SubDieMeta>();
            int subDieNumber = probe.getSubDieNumber();
            subDieNames = new String[subDieNumber];
            for (int i = 0; i < subDieNumber; i++) {
                subDieNames[i] = probe.getSubDieName(i);
                int[] coords = probe.getSubDieCoordsByIndex(i);
                SubDieMeta meta = new SubDieMeta();
                meta.index = i;
//                meta.x = coords[0];
//                meta.y = coords[1];
                meta.x = String.valueOf(coords[0]);
                meta.y = String.valueOf(coords[1]);
                meta.name = subDieNames[i];
                subs.add(meta);
            }

            metas.clear();
            metas.addAll(subs);

            tableModel.fireTableDataChanged();
            deviceManagePanel.syncSubDie(metas);
        } finally {
            if (line != null) {
                line.disconnect();
            }
            if (bus != null) {
                bus.close();
            }
        }
    }

    protected void upload() throws IOException, BusException {
        EntityInstrument entityProbe = InstrumentManager.getInstance().getProbe();
        Probe probe = (Probe) entityProbe.getInstrument();

//        PA300 pa300 = null;
//        if (!(probe instanceof PA300)) {
//            return;
//        }

//        pa300 = (PA300) probe;
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

//            int subDieNumber = probe.getSubDieNumber();
//            int min = Math.min(subDieNumber, metas.size());
//            for (int subdieIndex = subDieNumber - 1; subdieIndex >= min; subdieIndex--) {
//                pa300.deleteSubDie(subdieIndex);
//            }
//            for (int subdieIndex = 0; subdieIndex < min; subdieIndex++) {
//                SubDieMeta meta = metas.get(subdieIndex);
//                pa300.setSubDieData(subdieIndex, String.valueOf(meta.x), String.valueOf(meta.y), meta.name);
//            }
//            for (int subdieIndex = min; subdieIndex < metas.size(); subdieIndex++) {
//                SubDieMeta meta = metas.get(subdieIndex);
//                pa300.setSubDieData(subdieIndex, String.valueOf(meta.x), String.valueOf(meta.y), meta.name);
//            }
            probe.setSubDies(metas);

            // verify
            int subDieNumber = probe.getSubDieNumber();
            String[] subDieNames = new String[subDieNumber];
            for (int i = 0; i < subDieNumber; i++) {
                subDieNames[i] = probe.getSubDieName(i);
            }
            probe.close();
            deviceManagePanel.syncSubDie(metas);
        } finally {
            if (line != null) {
                line.disconnect();
            }
            if (bus != null) {
                bus.close();
            }
        }
    }

    public void shift(int fromIndex, int shift) {
        for (int i = fromIndex; i < metas.size(); i++) {
            SubDieMeta meta = metas.get(i);
            meta.index += shift;
        }
    }

    final class SubDieTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return metas.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex != 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SubDieMeta meta = metas.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return meta.index;
                case 1:
                    return meta.x;
                case 2:
                    return meta.y;
                default:
                    return meta.name;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            String s = aValue.toString();
            SubDieMeta meta = metas.get(rowIndex);
            switch (columnIndex) {
                case 1:
//                    meta.x = Integer.parseInt(s);
                    meta.x = s;
                    break;
                case 2:
//                    meta.y = Integer.parseInt(s);
                    meta.y = s;
                    break;
                case 3:
                    meta.name = s;
                    break;
            }
        }
    }
}
