/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.view.api.ViewDisplayer;
import com.platformda.view.api.ViewDisplayerContext;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author junyi
 */
public class OverlapPopupMenu extends BaseMeaPopupMenu implements ActionListener {

    ViewDisplayerContext viewDisplayerContext;
    ViewDisplayer viewDisplayer;
    JFreeChart chart;
    MeaView view;

    public OverlapPopupMenu() {
        init();
    }

    private void init() {
        add(dumpToPNGItem);

        dumpToPNGItem.addActionListener(this);
    }

    public void updateData(JFreeChart chart, ViewDisplayer viewDisplayer, ViewDisplayerContext viewDisplayerContext, MeaView view) {
        this.viewDisplayerContext = viewDisplayerContext;
        this.viewDisplayer = viewDisplayer;
//        this.chartPanel = chartPanel;
        this.chart = chart;
        this.view = view;
        updateItemState();
    }

    @Override
    public void updateData() {
        updateItemState();
    }

    void updateItemState() {
    }

    void updateDataViewerForm() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object eventSource = e.getSource();
        if (eventSource.equals(dumpToPNGItem)) {
            dumpToPng(viewDisplayerContext, viewDisplayer, view);
        }
    }

    @Override
    public void dataViewerDialogDisposed() {
        super.dataViewerDialogDisposed();
    }
}