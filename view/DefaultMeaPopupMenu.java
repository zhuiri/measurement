/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.algorithm.SeriesAlgorithm;
import com.platformda.algorithm.SeriesAlgorithmManager;
import com.platformda.chartview.util.PopupViewerPanel;
import com.platformda.iv.panelTemplates.XYDataViewerPanel;
//import com.platformda.chartview.util.XYDataViewerPanel;
import com.platformda.view.api.View;
import com.platformda.view.api.ViewDisplayer;
import com.platformda.view.api.ViewDisplayerContext;
import com.platformda.view.api.ViewGroup;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.jfree.chart.JFreeChart;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author junyi
 */
public class DefaultMeaPopupMenu extends BaseMeaPopupMenu implements ActionListener {

//    XYChartPanel chartPanel;
    ViewDisplayerContext viewDisplayerContext;
    ViewDisplayer viewDisplayer;
    JFreeChart chart;
    MeaViewProperties properties;
    MeaView view;
    String[] keys;
    JMenuItem clearItem = new JMenuItem("Clear Selection");
    JMenuItem clearAllItem = new JMenuItem("Clear All Selection");
    JMenuItem mapItem = new JMenuItem("Map Properties");
//    JMenuItem resetZoomItem = new JMenuItem("Reset Zoom");
    JMenuItem viewDataItem = new JMenuItem("View Data");
    JMenuItem nonAlgorithmItem = new JMenuItem("none");
    JMenu algorithmMenu = new JMenu("Algorithm");
    List<JMenuItem> algorithmItems = new ArrayList<JMenuItem>();
    List<SeriesAlgorithm> algorithms = SeriesAlgorithmManager.getAllAlgorithms();
    //
    XYDataViewerPanel dataViewerForm;

    public DefaultMeaPopupMenu(ViewDisplayerContext viewDisplayerContext) {
        this.viewDisplayerContext = viewDisplayerContext;
        init();
        composite();
    }

    protected void init() {
        nonAlgorithmItem.addActionListener(this);
        algorithmMenu.add(nonAlgorithmItem);
        for (SeriesAlgorithm pageAlgorithm : algorithms) {
            JMenuItem menuItem = new JMenuItem(pageAlgorithm.getName());
            algorithmItems.add(menuItem);
            algorithmMenu.add(menuItem);
            menuItem.addActionListener(this);
        }

        clearItem.setActionCommand("clearSelection");
        clearItem.addActionListener(this);

        clearAllItem.setActionCommand("clearAllSelection");
        clearAllItem.addActionListener(this);

        mapItem.addActionListener(this);

//        resetZoomItem.setActionCommand("resetZoom");
//        resetZoomItem.addActionListener(this);

        viewDataItem.setActionCommand("viewData");
        viewDataItem.addActionListener(this);

        dumpToPNGItem.addActionListener(this);
    }

    protected void composite() {
        add(algorithmMenu);
//        addSeparator();
//        add(mapItem);
//        add(clearAllItem);
//        addSeparator();
//        add(clearItem);
//        add(resetZoomItem);
        addSeparator();
        add(dumpToPNGItem);
        addSeparator();
        add(viewDataItem);
    }

    public void updateData(ViewDisplayer viewDisplayer, JFreeChart chart, MeaViewProperties properties, MeaView view, String[] keys) {
//        this.chartPanel = chartPanel;
        this.viewDisplayer = viewDisplayer;
        this.chart = chart;
        this.properties = properties;
        this.view = view;
        this.keys = keys;

        updateAlgorithmState();
        if (dataViewerVisible) {
            updateDataViewerForm();
        }
    }

    @Override
    public void updateData() {
        updateAlgorithmState();

        if (dataViewerVisible) {
            dataViewerForm.updateData();
        }
    }

    void updateAlgorithmState() {
        nonAlgorithmItem.setEnabled(true);
        for (JMenuItem jMenuItem : algorithmItems) {
            jMenuItem.setEnabled(true);
        }
        if (properties.getAlgorithm() != null) {
            int index = algorithms.indexOf(properties.getAlgorithm());
            algorithmItems.get(index).setEnabled(false);
        } else {
            nonAlgorithmItem.setEnabled(false);
        }
    }

    void updateDataViewerForm() {
        viewDataItem.setEnabled(false);
        if (dataViewerForm == null) {
            dataViewerForm = new XYDataViewerPanel();
            if (dataViewerPanel != null) {
                dataViewerPanel.removeAll();
                dataViewerPanel.add(dataViewerForm, BorderLayout.CENTER);
            }
        }
        if (dataViewerPanel == null) {
            dataViewerPanel = new PopupViewerPanel(new BorderLayout());
            dataViewerPanel.setOwner(this);
            dataViewerPanel.add(dataViewerForm, BorderLayout.CENTER);

            dataViewerPanel.setPreferredSize(new Dimension(640, 540));
        }

        String xLabel = chart.getXYPlot().getDomainAxis().getLabel();
        String yLabel = chart.getXYPlot().getRangeAxis().getLabel();
        dataViewerForm.updateData(chart.getXYPlot(), keys, xLabel, yLabel);
        dataViewerPanel.updateUI();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object eventSource = e.getSource();
        if (eventSource.equals(dumpToPNGItem)) {
            dumpToPng(viewDisplayerContext, viewDisplayer, view);
        } else if (eventSource.equals(clearItem)) {
//            view.clearSelection();
        } else if (eventSource.equals(clearAllItem)) {
            clearAllSelection();
        } //        else if (eventSource.equals(resetZoomItem)) {
        //            chartPanel.restoreAutoBounds();
        //        } 
        else if (eventSource.equals(viewDataItem)) {
            dataViewerVisible = true;
            updateDataViewerForm();

            DialogDescriptor desc = new DialogDescriptor(dataViewerPanel, "Data Viewer", false, null);
            desc.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getNewValue().equals(DialogDescriptor.CLOSED_OPTION)) {
//                        LifecycleManager.getDefault().exit();
                        dataViewerPanel.disposed();
                    }
                }
            });
            desc.setOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
//            DialogDisplayer.getDefault().notify(desc); // displays the dialog
            Dialog dataViewerDialog = DialogDisplayer.getDefault().createDialog(desc);
            dataViewerPanel.setDialog(dataViewerDialog);
            dataViewerDialog.setVisible(true);
        } else if (eventSource.equals(nonAlgorithmItem)) {
            applyAlgorithm(null);
        } else if (algorithmItems.contains(eventSource)) {
            int index = algorithmItems.indexOf(eventSource);
            SeriesAlgorithm algorithm = algorithms.get(index);
            applyAlgorithm(algorithm);
        }
    }

    void clearAllSelection() {
        ViewGroup viewGroup = view.getViewGroup();
        if (viewGroup != null) {
            int number = viewGroup.getRow() * viewGroup.getColumn() * viewGroup.getScreenNumber();
            for (int i = 0; i < number; i++) {
                View viewInGroup = viewGroup.getView(i);
                if (view != null && viewInGroup instanceof MeaPageView) {
                    // 
                    MeaPageView filterView = (MeaPageView) viewInGroup;
//                    filterView.clearSelection();
                }
            }
        }
    }

    void applyAlgorithm(SeriesAlgorithm algorithm) {
        properties.setAlgorithm(algorithm, true);
    }

    @Override
    public void dataViewerDialogDisposed() {
        super.dataViewerDialogDisposed();
        viewDataItem.setEnabled(true);
    }
}
