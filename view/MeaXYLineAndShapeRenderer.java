/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.chartview.util.Selectable;
import com.platformda.chartview.util.SeriesDeselector;
import com.platformda.chartview.util.SeriesProp;
import java.awt.Paint;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

/**
 *
 * @author junyi
 */
public class MeaXYLineAndShapeRenderer extends XYLineAndShapeRenderer {

    private static final long serialVersionUID = 1L;
    SeriesDeselector seriesDeselector;
    int datasetIndex = 0;

    public MeaXYLineAndShapeRenderer(SeriesDeselector seriesDeselector) {
        super();
        this.seriesDeselector = seriesDeselector;
        setAutoPopulateSeriesFillPaint(false);
        setAutoPopulateSeriesOutlinePaint(false);
        setAutoPopulateSeriesOutlineStroke(false);
        setAutoPopulateSeriesPaint(false);
        setAutoPopulateSeriesShape(false);
        setAutoPopulateSeriesStroke(false);
//        setDrawSeriesLineAsPath(true);
//            setBaseCreateEntities(false);
//            setUseFillPaint(false);
//            setUseOutlinePaint(false);
    }

    public void setDatasetIndex(int datasetIndex) {
        this.datasetIndex = datasetIndex;
    }

    @Override
    public Paint getSeriesPaint(int series) {
        Selectable dataset = (Selectable) getPlot().getDataset(datasetIndex);
        SeriesProp seriesProp = dataset.getSeriesProp(series);
        if (seriesDeselector.isSeriesDeselected(seriesProp)) {
            return SeriesDeselector.DESELECTED_COLOR;
        }
        return super.getSeriesPaint(series);
    }

    @Override
    public boolean isSeriesVisible(int series) {
        Selectable dataset = (Selectable) getPlot().getDataset(datasetIndex);
        SeriesProp seriesProp = dataset.getSeriesProp(series);
        if (seriesDeselector.isSeriesDeselected(seriesProp)) {
//            boolean visible = NbPreferences.forModule(GeneralPanel.class).getBoolean(GeneralPanel.PROP_VISIBLE_ON_DESELECTED, false);
            boolean visible = false;
            return visible;
        }
        return super.isSeriesVisible(series);
    }
}