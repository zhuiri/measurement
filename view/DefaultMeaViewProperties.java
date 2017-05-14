/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.algorithm.SeriesAlgorithm;
import com.platformda.chartview.api.ChartUtil;
import com.platformda.chartview.util.AlgorithmicDataset;
import com.platformda.chartview.util.CompatibleLogarithmicAxis;
import com.platformda.chartview.util.ExLegendItem;
import com.platformda.chartview.util.SeriesDeselector;
import com.platformda.chartview.util.SeriesProp;
import com.platformda.chartview.util.Tag;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;

/**
 *
 * @author Junyi
 */
public class DefaultMeaViewProperties implements MeaViewProperties, SeriesDeselector {

    int scaleSystem = ChartUtil.SCALE_CARTESIAN;
    transient MeaViewPropertyChangeSupport changeSupport = new MeaViewPropertyChangeSupport();
    SeriesAlgorithm algorithm;
    Map<String, String> props;
    protected List<SeriesProp> deselections = new ArrayList<SeriesProp>();
    boolean xLog = false;
    boolean yLog = false;
    double[] dataRange; // xmin, xmax, ymin, ymax
    double[] defaultDataRange;
    transient AxisChangeListener xAxisChangeListener = new AxisChangeListener() {
        @Override
        public void axisChanged(AxisChangeEvent ace) {
            NumberAxis numberAxis = (NumberAxis) ace.getAxis();
            if (numberAxis.isAutoRange()) {
                if (dataRange != null) {
                    dataRange[0] = Double.NaN;
                    dataRange[1] = Double.NaN;
                }
            } else {
                if (dataRange == null) {
                    dataRange = new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN};
                }
                Range range = numberAxis.getRange();
                dataRange[0] = range.getLowerBound();
                dataRange[1] = range.getUpperBound();
            }
        }
    };
    transient AxisChangeListener yAxisChangeListener = new AxisChangeListener() {
        @Override
        public void axisChanged(AxisChangeEvent ace) {
            NumberAxis numberAxis = (NumberAxis) ace.getAxis();
            if (numberAxis.isAutoRange()) {
                if (dataRange != null) {
                    dataRange[2] = Double.NaN;
                    dataRange[3] = Double.NaN;
                }
            } else {
                if (dataRange == null) {
                    dataRange = new double[]{Double.NaN, Double.NaN, Double.NaN, Double.NaN};
                }
                Range range = numberAxis.getRange();
                dataRange[2] = range.getLowerBound();
                dataRange[3] = range.getUpperBound();
            }
        }
    };

    @Override
    public int getScaleSystem() {
        return scaleSystem;
    }

    @Override
    public SeriesAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public Map<String, String> getSeriesAlgorithmProps() {
        return props;
    }

    @Override
    public void setAlgorithm(SeriesAlgorithm algorithm) {
        setAlgorithm(algorithm, false);
    }

    @Override
    public void setAlgorithm(SeriesAlgorithm algorithm, boolean notify) {
        if ((this.algorithm == null && algorithm != null) || this.algorithm != algorithm) {
            SeriesAlgorithm oldAlgorithm = this.algorithm;
            this.algorithm = algorithm;
            clearSelection();
            clearDataRange();
            props = null;

            if (notify) {
                fireAlgorithmChanged(oldAlgorithm, algorithm);
            }
        }
    }

    void fireAlgorithmChanged(SeriesAlgorithm oldAlgorithm, SeriesAlgorithm newAlgorithm) {
        changeSupport.fireAlgorithmChanged(oldAlgorithm, newAlgorithm);
    }

    public void clearSelection() {
    }

    public void clearDataRange() {
        dataRange = null;
    }

    @Override
    public void applySelection(MeaView filterView, JFreeChart chart) {
        int legendNumber = chart.getSubtitleCount();
        CHECKLEGEND:
        for (int legendIndex = 0; legendIndex < legendNumber; legendIndex++) {
            LegendTitle legendTitle = chart.getLegend(legendIndex);
            if (legendTitle == null) {
                break;
            }
            if (legendTitle != null) {
                LegendItemSource[] sources = legendTitle.getSources();
                for (LegendItemSource legendItemSource : sources) {
                    LegendItemCollection legendItemCollection = legendItemSource.getLegendItems();
                    int itemNumber = legendItemCollection.getItemCount();
                    for (int i = 0; i < itemNumber; i++) {
                        LegendItem legendItem = legendItemCollection.get(i);
                        if (legendItem instanceof ExLegendItem) {
                            Comparable comparable = legendItem.getSeriesKey();
                            if (comparable instanceof SeriesProp) {
                                ExLegendItem exLegendItem = (ExLegendItem) legendItem;
                                if (exLegendItem.isDeselectable()) {
                                    if (exLegendItem.hasChildren()) {
                                        exLegendItem.setDeselected(true);
                                    } else {
                                        SeriesProp seriesProp = (SeriesProp) legendItem.getSeriesKey();
                                        MeaViewProperties viewProp = filterView.getViewProperties(legendIndex);
                                        if (viewProp != null) {
                                            exLegendItem.setDeselected(viewProp.isSeriesDeselected(seriesProp));
                                        }
                                        if (!exLegendItem.isDeselected()) {
                                            ExLegendItem parent = exLegendItem.getParent();
                                            if (parent != null) {
                                                parent.setDeselected(false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void applyAlgorithm(MeaView meaView, XYPlot xyPlot) {
        int datasetNumber = xyPlot.getDatasetCount();
        for (int i = 0; i < datasetNumber; i++) {
            AlgorithmicDataset algorithmicDataset = (AlgorithmicDataset) xyPlot.getDataset(i);
            algorithmicDataset.applyAlgorithm(algorithm, meaView.getAlgorithmContext(), props);
        }
    }

    @Override
    public void apply(MeaView meaView, JFreeChart chart, boolean applyRange) {
        XYPlot xyPlot = (XYPlot) chart.getPlot();

        applyAlgorithm(meaView, xyPlot);
        applyXLog(xyPlot);
        applyYLog(xyPlot);

        if (defaultDataRange == null) {
            defaultDataRange = new double[4];
            NumberAxis axis = (NumberAxis) xyPlot.getRangeAxis();
            Range yRange = axis.getRange();

            axis = (NumberAxis) xyPlot.getDomainAxis();
            Range xRange = axis.getRange();

            defaultDataRange[0] = xRange.getLowerBound();
            defaultDataRange[1] = xRange.getUpperBound();
            defaultDataRange[2] = yRange.getLowerBound();
            defaultDataRange[3] = yRange.getUpperBound();
        }

        if (applyRange && dataRange != null && dataRange.length == 4) {
            if (!Double.isNaN(dataRange[0]) || !Double.isNaN(dataRange[1])) {
                if (Double.isNaN(dataRange[0])) {
                    dataRange[0] = defaultDataRange[0];
                }
                if (Double.isNaN(dataRange[1])) {
                    dataRange[1] = defaultDataRange[1];
                }
                NumberAxis axis = (NumberAxis) xyPlot.getDomainAxis();
                axis.setRange(new Range(dataRange[0], dataRange[1]), true, false);
            }
            if (!Double.isNaN(dataRange[2]) || !Double.isNaN(dataRange[3])) {
                if (Double.isNaN(dataRange[2])) {
                    dataRange[2] = defaultDataRange[2];
                }
                if (Double.isNaN(dataRange[3])) {
                    dataRange[3] = defaultDataRange[3];
                }
                NumberAxis axis = (NumberAxis) xyPlot.getRangeAxis();
                axis.setRange(new Range(dataRange[2], dataRange[3]), true, false);
            }
        }
    }

    public boolean isXLog() {
        return xLog;
    }

    public void setXLog(boolean xLog) {
        if (this.xLog != xLog) {
            clearDataRange();
        }
        this.xLog = xLog;
    }

    public boolean isYLog() {
        return yLog;
    }

    public void setYLog(boolean yLog) {
        if (this.yLog != yLog) {
            clearDataRange();
        }
        this.yLog = yLog;
    }

    @Override
    public void setXLog(int xAxisIndex, boolean log) {
        setXLog(log);
    }

    @Override
    public void setYLog(int yAxisIndex, boolean log) {
        setYLog(log);
    }

    @Override
    public boolean isXLog(int xAxisIndex) {
        return xLog;
    }

    @Override
    public boolean isYLog(int yAxisIndex) {
        return yLog;
    }

    @Override
    public void applyXLog(int xAxisIndex, XYPlot xyPlot) {
        applyXLog(xyPlot);
    }

    @Override
    public void applyYLog(int yAxisIndex, XYPlot xyPlot) {
        applyYLog(xyPlot);
    }

    public void applyXLog(XYPlot xyPlot) {
        NumberAxis domainAxis = (NumberAxis) xyPlot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        if (xLog) {
            LogarithmicAxis numberAxis = new CompatibleLogarithmicAxis(domainAxis.getLabel(), true);
            ChartUtil.customizeLogarithmicAxis(numberAxis);
            numberAxis.setUpperMargin(domainAxis.getUpperMargin());
            numberAxis.setLowerMargin(domainAxis.getLowerMargin());
            numberAxis.setLabelFont(domainAxis.getLabelFont());
            numberAxis.setTickLabelFont(domainAxis.getTickLabelFont());
            numberAxis.setLabelPaint(domainAxis.getLabelPaint());
            numberAxis.setTickLabelPaint(domainAxis.getTickLabelPaint());

            numberAxis.setAutoRange(domainAxis.isAutoRange());
            xyPlot.setDomainAxis(numberAxis);

            rangeAxis.configure();
            numberAxis.addChangeListener(xAxisChangeListener);
        } else {
            NumberAxis numberAxis = new NumberAxis(domainAxis.getLabel());
            ChartUtil.customizeLinearDomainAxis(numberAxis);
            numberAxis.setUpperMargin(domainAxis.getUpperMargin());
            numberAxis.setLowerMargin(domainAxis.getLowerMargin());
            numberAxis.setLabelFont(domainAxis.getLabelFont());
            numberAxis.setTickLabelFont(domainAxis.getTickLabelFont());
            numberAxis.setLabelPaint(domainAxis.getLabelPaint());
            numberAxis.setTickLabelPaint(domainAxis.getTickLabelPaint());

            numberAxis.setAutoRange(domainAxis.isAutoRange());
            xyPlot.setDomainAxis(numberAxis);

            rangeAxis.configure();
            numberAxis.addChangeListener(xAxisChangeListener);
        }
    }

    public void applyYLog(XYPlot xyPlot) {
        NumberAxis domainAxis = (NumberAxis) xyPlot.getDomainAxis();
        NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
        if (yLog) {
            LogarithmicAxis numberAxis = new CompatibleLogarithmicAxis(rangeAxis.getLabel(), false);
            ChartUtil.customizeLogarithmicAxis(numberAxis);
            numberAxis.setUpperMargin(rangeAxis.getUpperMargin());
            numberAxis.setLowerMargin(rangeAxis.getLowerMargin());
            numberAxis.setLabelFont(rangeAxis.getLabelFont());
            numberAxis.setTickLabelFont(rangeAxis.getTickLabelFont());
            numberAxis.setLabelPaint(rangeAxis.getLabelPaint());
            numberAxis.setTickLabelPaint(rangeAxis.getTickLabelPaint());

            numberAxis.setAutoRange(rangeAxis.isAutoRange());
            xyPlot.setRangeAxis(numberAxis);

            domainAxis.configure();
            numberAxis.addChangeListener(yAxisChangeListener);
        } else {
            NumberAxis numberAxis = new NumberAxis(rangeAxis.getLabel());
            ChartUtil.customizeLinearRangeAxis(numberAxis, getDefaultRangeUpperMargin());
            numberAxis.setUpperMargin(rangeAxis.getUpperMargin());
            numberAxis.setLowerMargin(rangeAxis.getLowerMargin());
            numberAxis.setLabelFont(rangeAxis.getLabelFont());
            numberAxis.setTickLabelFont(rangeAxis.getTickLabelFont());
            numberAxis.setLabelPaint(rangeAxis.getLabelPaint());
            numberAxis.setTickLabelPaint(rangeAxis.getTickLabelPaint());

            numberAxis.setAutoRange(rangeAxis.isAutoRange());
            xyPlot.setRangeAxis(numberAxis);

            domainAxis.configure();
            numberAxis.addChangeListener(yAxisChangeListener);
        }
    }

    public double getDefaultRangeUpperMargin() {
        if (scaleSystem == ChartUtil.SCALE_CARTESIAN) {
            return 0.1;
        }
        return 0;
    }

    @Override
    public boolean isSeriesDeselected(SeriesProp seriesProp) {
        for (SeriesProp deselection : deselections) {
            if (deselection.equals(seriesProp)) {
                return true;
            } else {
                Set<String> keys = deselection.keySet();

                boolean matched = true;
                for (String string : keys) {
                    if (seriesProp.containsKey(string)) {
                        Object obj1 = deselection.getProp(string);
                        if (obj1.equals(SeriesProp.VALUE_WILDCARD)) {
                        } else {
                            Object obj2 = seriesProp.getProp(string);
                            if (!obj1.equals(obj2)) {
                                matched = false;
                                break;
                            }
                        }
                    } else {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setSeriesDeselected(SeriesProp seriesProp, boolean deselected) {
        for (SeriesProp chartSeriesProp : deselections) {
            if (chartSeriesProp.equals(seriesProp)) {
                if (!deselected) {
                    deselections.remove(chartSeriesProp);
                }
                return;
            }
        }

        if (deselected) {
            deselections.add(seriesProp);
        }
    }

    public void addPropertyChangeListener(MeaViewPropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(MeaViewPropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void addTag(Tag tag) {
    }

    @Override
    public void removeTag(Tag tag) {
    }

    @Override
    public int getTagNumber() {
        return 0;
    }

    @Override
    public Tag getTag(int index) {
        return null;
    }
}
