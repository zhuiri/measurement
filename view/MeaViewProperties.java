/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.algorithm.SeriesAlgorithm;
import com.platformda.chartview.util.SeriesProp;
import com.platformda.chartview.util.TagMutableProvider;
import com.platformda.utility.ScriptMethod;
import java.util.Map;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

/**
 *
 * @author Junyi
 */
public interface MeaViewProperties extends TagMutableProvider {

    public int getScaleSystem();

    public SeriesAlgorithm getAlgorithm();

    public Map<String, String> getSeriesAlgorithmProps();

    @ScriptMethod
    public void setAlgorithm(SeriesAlgorithm algorithm);

    public void setAlgorithm(SeriesAlgorithm algorithm, boolean notify);

    public void applySelection(MeaView filterView, JFreeChart chart);

    public void apply(MeaView filterView, JFreeChart chart, boolean applyRange);

    public boolean isXLog(int xAxisIndex);

    public boolean isYLog(int yAxisIndex);

    public void setXLog(int xAxisIndex, boolean log);

    public void setYLog(int yAxisIndex, boolean log);

    public void applyXLog(int xAxisIndex, XYPlot xyPlot);

    public void applyYLog(int yAxisIndex, XYPlot xyPlot);

    public boolean isSeriesDeselected(SeriesProp seriesProp);

    public void setSeriesDeselected(SeriesProp seriesProp, boolean deselected);
}
