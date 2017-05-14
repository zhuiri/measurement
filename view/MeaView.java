/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.algorithm.AlgorithmContext;
import com.platformda.view.api.View;
import com.platformda.view.api.ViewDisplayer;
import com.platformda.view.api.ViewDisplayerContext;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author Junyi
 */
public interface MeaView extends View {

    public JFreeChart getChart(int style);

    public MeaViewProperties getViewProperties();

    public MeaViewProperties getViewProperties(int legendIndex);

    public MeaPopupMenu getPopupMenu(MeaPopupMenu lastPopupMenu, ChartPanel chartPanel, JFreeChart chart, ViewDisplayer viewDisplayer, ViewDisplayerContext viewDisplayerContext);

    public AlgorithmContext getAlgorithmContext();
}
