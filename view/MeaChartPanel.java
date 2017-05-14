/**
 *
 */
package com.platformda.iv.view;

import com.platformda.algorithm.SeriesAlgorithm;
import com.platformda.chartview.api.ChartUtil;
import com.platformda.chartview.util.EnhancedChartPanel;
import com.platformda.chartview.util.ExLegendItem;
import com.platformda.chartview.util.SeriesProp;
import com.platformda.utility.common.PathLocator;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;

/**
 *
 */
public class MeaChartPanel extends EnhancedChartPanel implements ChartMouseListener, MeaViewPropertyChangeListener {

    PathLocator pathLocator;
    MeaViewProperties properties;
    //allProperties for combine serveral views
    //set the  firstview as the default properties 
    List<MeaViewProperties> allProperties;
    MeaPopupMenu popupMenu;
    MeaView meaView;
    protected int selectMask = InputEvent.SHIFT_MASK;

    /**
     * @param chart
     */
    public MeaChartPanel(PathLocator pathLocator, JFreeChart chart, MeaViewProperties properties, MeaView view) {
        super(chart);
        this.properties = properties;
        //add this current properties into allProperties
        this.allProperties = new ArrayList<MeaViewProperties>();
        this.allProperties.add(properties);
        init(pathLocator, view);
    }
    
     public MeaChartPanel(PathLocator pathLocator, JFreeChart chart, List<MeaViewProperties> properties, MeaView view) {
        super(chart);
        this.allProperties = properties;
        //default select the first view
        this.properties = properties.get(0);
        init(pathLocator, view);
    }

    private void init(PathLocator pathLocator, MeaView view) {
        processMouseEventAgainstTag = false;
        this.pathLocator = pathLocator;
        this.meaView = view;

        selectMask = InputEvent.CTRL_MASK;
        super.setPanMask(InputEvent.SHIFT_MASK);

        this.addChartMouseListener(this);
        resetSelection();
    }

    public void setFilterViewPopupMenu(MeaPopupMenu popupMenu) {
        setPopupMenu(popupMenu);
        this.popupMenu = popupMenu;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (tagEnabled && mousePressedAgainstTag(event)) {
            return;
        }
        super.mousePressed(event);
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (tagEnabled && mouseDraggedAgainstTag(event)) {
            return;
        }
        super.mouseDragged(event);
    }

    @Override
    public void zoom(Rectangle2D selection) {
        int scaleSystem = properties.getScaleSystem();
        if (scaleSystem == ChartUtil.SCALE_CARTESIAN) {
            super.zoom(selection);
            return;
        }
        double width = selection.getWidth();
        double height = selection.getHeight();
        if (width < height) {
            double diff = height - width;
            selection.setRect(selection.getX() - diff / 2, selection.getY(), height, height);
        } else {
            double diff = height - width;
            selection.setRect(selection.getX(), selection.getY() + diff / 2, width, width);
        }
        super.zoom(selection);
    }

    MeaViewProperties getViewProperties(int legendIndex) {
       // return properties;
        return allProperties.get(legendIndex);
    }

    @Override
    public void chartMouseClicked(ChartMouseEvent event) {
        ChartEntity e = event.getEntity();
        if (e != null && event.getTrigger().getClickCount() == 2) {
            if (e instanceof LegendItemEntity) {
                LegendItemEntity entity = (LegendItemEntity) e;
                Comparable comparable = entity.getSeriesKey();
                if (comparable != null && comparable instanceof SeriesProp) {
                    JFreeChart chart = getChart();
                    int legendNumber = chart.getSubtitleCount();
                    CHECKLEGEND:
                    for (int legendIndex = 0; legendIndex < legendNumber; legendIndex++) {
                        LegendTitle legendTitle = chart.getLegend(legendIndex);
                        if (legendTitle == null) {
                            break;
                        }
                        LegendItemSource[] sources = legendTitle.getSources();
                        for (LegendItemSource legendItemSource : sources) {
                            LegendItemCollection legendItemCollection = legendItemSource.getLegendItems();
                            int itemNumber = legendItemCollection.getItemCount();
                            for (int i = 0; i < itemNumber; i++) {
                                LegendItem legendItem = legendItemCollection.get(i);
                                if (legendItem instanceof ExLegendItem && legendItem.getSeriesKey() == comparable) {
                                    ExLegendItem exLegendItem = (ExLegendItem) legendItem;
                                    if (exLegendItem.isDeselectable()) {
                                        boolean newDeselected = !exLegendItem.isDeselected();
                                        if (exLegendItem.hasChildren()) {
                                            exLegendItem.setDeselected(newDeselected);
                                            List<ExLegendItem> children = exLegendItem.getChildren();
                                            for (ExLegendItem child : children) {
                                                SeriesProp seriesProp = (SeriesProp) child.getSeriesKey();
                                                child.setDeselected(newDeselected);
                                                MeaViewProperties viewProp = getViewProperties(legendIndex);
                                                if (viewProp != null) {
                                                    viewProp.setSeriesDeselected(seriesProp, newDeselected);
                                                }
                                            }
                                        } else {
                                            SeriesProp seriesProp = (SeriesProp) comparable;
                                            exLegendItem.setDeselected(newDeselected);
                                            MeaViewProperties viewProp = getViewProperties(legendIndex);
                                            if (viewProp != null) {
                                                viewProp.setSeriesDeselected(seriesProp, newDeselected);
                                            }

                                            ExLegendItem parent = exLegendItem.getParent();
                                            if (parent != null) {
                                                parent.setDeselected(true);
                                                List<ExLegendItem> children = parent.getChildren();
                                                for (ExLegendItem child : children) {
                                                    if (!child.isDeselected()) {
                                                        parent.setDeselected(false);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        setRefreshBuffer(true);
                                        repaint();
                                    }
                                    break CHECKLEGEND;
                                }
                            }
                        }
                    }

                }
            } else if (e instanceof AxisEntity) {
                AxisEntity entity = (AxisEntity) e;
                ValueAxis axis = (ValueAxis) entity.getAxis();

                int xAxisIndex = getChart().getXYPlot().getDomainAxisIndex(axis);
                if (xAxisIndex >= 0) {
                    properties.setXLog(xAxisIndex, !properties.isXLog(xAxisIndex));
                    properties.applyXLog(xAxisIndex, (XYPlot) getChart().getPlot());
                } else {
                    int yAxisIndex = getChart().getXYPlot().getRangeAxisIndex(axis);
                    if (yAxisIndex >= 0) {
                        properties.setYLog(yAxisIndex, !properties.isYLog(yAxisIndex));
                        properties.applyYLog(yAxisIndex, (XYPlot) getChart().getPlot());
                    }
                }
                getChart().fireChartChanged();
                setRefreshBuffer(true);
                repaint();
                updateUI();
                restoreAutoBounds();
            } else if (e instanceof PlotEntity) {
                // restore
                restoreAutoBounds();
            }
        }
    }

    @Override
    public void chartMouseMoved(ChartMouseEvent event) {
    }

    private void resetSelection() {
        JFreeChart chart = getChart();
        properties.applySelection(meaView, chart);
    }

    @Override
    public void algorithmChanged(SeriesAlgorithm oldAlgorithm, SeriesAlgorithm newAlgorithm) {
//        properties.apply(getChart().getXYPlot(), true);
//        JFreeChart chart = getChart();
//        ValueAxis yAxis = chart.getXYPlot().getRangeAxis();
//        String y = yAxis.getLabel()+"("+newAlgorithm.getName()+")";
//        yAxis = new NumberAxis(y);
//        chart.getXYPlot().setRangeAxis(yAxis);
//        properties.apply(meaView, chart, true);
        properties.apply(meaView, getChart(), true);
        popupMenu.updateData();

        setRefreshBuffer(true);
        repaint();
    }

    @Override
    public void viewPropertyChanged() {
        properties.apply(meaView, getChart(), true);
        popupMenu.updateData();

        resetSelection();
        setRefreshBuffer(true);
        repaint();
    }

    @Override
    public void selectionChanged() {
        resetSelection();
        setRefreshBuffer(true);
        repaint();
    }
}
