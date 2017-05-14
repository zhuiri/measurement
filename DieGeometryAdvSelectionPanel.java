/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.chartview.grid.GridAxis;
import com.platformda.chartview.grid.GridPlot;
import com.platformda.chartview.grid.GridProvider;
import com.platformda.chartview.util.EnhancedChartPanel;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * select dies in plot
 *
 * @author Junyi
 */
public class DieGeometryAdvSelectionPanel extends JPanel {

    DieHighlighter dieHighlighter;
    WaferInfo waferInfo;
    List<WaferDieInfo> dieInfos = new ArrayList<WaferDieInfo>();
//    List<WaferDieInfo> selectedDieInfos = new ArrayList<WaferDieInfo>();
    List<WaferDieInfo> selectedDieInfos;
    StatusProvider statusProvider;

    private void initComponents() {
        GridAxis domainAxis = new GridAxis("X");
        GridAxis rangeAxis = new GridAxis("Y");
        GridProvider gridProvider = null;
        GridPlot plot = new GridPlot(gridProvider, domainAxis, rangeAxis);
        plot.setFontSize(2);
        JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        ChartFactory.getChartTheme().apply(chart);

        plot.setBackgroundPaint(null);
        chart.setBackgroundPaint(null);
        ChartPanel chartPanel = new EnhancedChartPanel(chart);

    }
}
