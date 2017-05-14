/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.algorithm.AlgorithmContext;
import com.platformda.chartview.api.ChartUtil;
import com.platformda.chartview.util.DefaultSeriesProp;
import com.platformda.chartview.util.ExLegendItem;
import com.platformda.chartview.util.SeriesProp;
import com.platformda.chartview.util.Tag;
import com.platformda.chartview.util.TagMutableProvider;
import com.platformda.chartview.util.XYPlainSeries;
import com.platformda.chartview.util.XYPlainSeriesCollection;
import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.EntityPageType;
import com.platformda.datacore.PXYPage;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.project.MeaProject;
import com.platformda.iv.MeaSpace;
import com.platformda.iv.admin.Routine;
import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.measure.AnalysisFixedPage;
import com.platformda.utility.common.MathUtil;
import com.platformda.view.api.AbstractView;
import com.platformda.view.api.ViewBrowser;
import com.platformda.view.api.ViewDisplayer;
import com.platformda.view.api.ViewDisplayerContext;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.ui.Drawable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Junyi
 */
public class MeaPageView extends AbstractView implements MeaView, AlgorithmContext {

    DefaultMeaViewProperties viewProperties = new DefaultMeaViewProperties();
    PXYPage page;
    Routine routine;
    EntityPagePattern pagePattern;
    protected Lookup lkp;
//    protected String[] keys = new String[]{SeriesProp.PROP_DEVICE, SeriesProp.PROP_PAGE};
    protected String[] keys = new String[]{SeriesProp.PROP_PAGE};

    public MeaPageView(PXYPage page, Routine routine, EntityPagePattern pagePattern) {
        this.page = page;
        this.routine = routine;
        this.pagePattern = pagePattern;
    }

    public MeaPageView(AnalysisFixedPage fixedPage) {
        this.page = fixedPage.toPage(fixedPage.toPageType());
        MeaAnalysis analysis = fixedPage.getAnalysis();
        routine = analysis.getRoutine();
        pagePattern = analysis.getPagePattern();
    }

    public EntityDevice getDevice() {
        return page.getDevice();
    }

    public Routine getRoutine() {
        return routine;
    }

    public EntityPagePattern getPagePattern() {
        return pagePattern;
    }
    
    public PXYPage getPage(){
        return page;
    }

    public EntityPageType getPageType() {
        return page.getPageType();
    }

    @Override
    public Drawable getDrawable(int style) {
        return getChart(style);
    }

    @Override
    public ViewBrowser createViewBrowser(ViewDisplayerContext viewDisplayerContext) {
        MeaProject mp = viewDisplayerContext.getLookup().lookup(MeaProject.class);
        MeaSpace meaSpace = null;
        if (mp != null) {
            meaSpace = mp.getMeaSpace();
        }
        return new MeaViewBrowser(viewDisplayerContext, meaSpace);
    }

    @Override
    public String getTitle() {
        return "Title";
    }

    @Override
    public Lookup getLookup() {
        if (lkp == null) {
            lkp = Lookups.fixed(new Object[]{this,});
        }
        return lkp;
    }

    @Override
    public JFreeChart getChart(int style) {
        EntityPageType pageType = page.getPageType();

        String[] yNames = pageType.getYNames();
        int yNumber = yNames.length;
        String x = pageType.getXName();
        String y = pageType.getYName();
        //recomponent the y label
        if(yNames.length>1){
           StringBuilder builder = new StringBuilder();
           for(int index =0;index < yNames.length-1;index++){
               builder.append(yNames[index]);
               builder.append("/");
           }
           builder.append(yNames[ yNames.length-1]);
           y = builder.toString();
        }
       

        boolean firstYAsX = false;
        int yStartIndex = 0;
        if (pageType.isFirstYAsX()) {
            firstYAsX = true;
            yStartIndex = 1;
            x = y;
            y = pageType.getYNames()[1];
            yNumber--;
        }
        XYPlainSeriesCollection dataset = new XYPlainSeriesCollection();
        XYLineAndShapeRenderer renderer = new MeaXYLineAndShapeRenderer(viewProperties);

        double xScale = pageType.getDevicePolarity().getScale();
        double yScale = pageType.getDevicePolarity().getScale();

        // cv not revert
        if (y.toLowerCase().startsWith("c")) {
            yScale = 1;
        }

//        xScale = 1;
//        yScale = 1;
        if (xScale < 0) {
            x = "-" + x;
        }
        if (yScale < 0) {
            y = "-" + y;
        }

        double[] pValues = page.getPValues();
        double[] xValues = page.getXValues();

        for (int pIndex = 0; pIndex < pValues.length; pIndex++) {
            for (int yIndex = yStartIndex; yIndex < yNames.length; yIndex++) {
                int seriesIndex = dataset.getSeriesCount();
                XYPlainSeries series = new XYPlainSeries("Series" + dataset.getSeriesCount());
                SeriesProp seriesProp = new DefaultSeriesProp();
                series.setSeriesProp(seriesProp);
                seriesProp.putProp(SeriesProp.PROP_DEVICE, page.getDevice());
                //seriesProp.putProp(SeriesProp.PROP_PAGE, pageType);
                seriesProp.putProp(SeriesProp.PROP_PAGE, pageType.getName());
                seriesProp.putProp(SeriesProp.PROP_P, pValues[pIndex]);

                double[] yValues = null;
                int size = page.getYValues(0, 0).length;
                yValues = new double[size];
                //yValues = page.getYValues(yIndex, pIndex);
                ////allow beta/betar! 
                //todo : should modify save pages
                //set yavlues = new double[1000] when save page and yvaules == null
                if(yNames[yIndex].equalsIgnoreCase("beta")
                        ||yNames[yIndex].equalsIgnoreCase("betar")){
                    if(yIndex == 2){
                        double[] y1 = page.getYValues(0, pIndex);
                        double[] y2 = page.getYValues(1, pIndex);
                        for(int index = 0;index<y1.length;index++){
                            double value = 0.0;
                            if(Math.abs(y2[index])<1e-20){
                                value = Double.NaN;
                            }else{
                                value = y1[index]/y2[index];
                            }
                            yValues[index ] = value;
                            //cant set yvalues to page
                            //page not support beta/betar
                            //page.setYValue(yIndex, pIndex, index, value);
                        }
                        
                    }
                }else{
                    yValues = page.getYValues(yIndex, pIndex);
                }
                
                if (yValues != null) {
                    series.setRawValues(MathUtil.multiply(Arrays.copyOf(xValues, xValues.length), xScale), MathUtil.multiply(Arrays.copyOf(yValues, yValues.length), yScale));
                    series.setValuesFromRaw();

                    dataset.addSeries(series);
                    renderer.setSeriesPaint(seriesIndex, ChartOptions.getDefaultColor(seriesIndex));
                    renderer.setSeriesLinesVisible(seriesIndex, false);
                    renderer.setSeriesShapesVisible(seriesIndex, true);
                    //set the paint shape as the y label
                    renderer.setSeriesShape(seriesIndex, ChartUtil.getDefaultShape(yIndex));

                    //set line
                    renderer.setSeriesLinesVisible(seriesIndex, true);
                    renderer.setSeriesStroke(seriesIndex, ChartUtil.getDefaultStroke(0));
                }

            }
        }
        
        //add unit to x
        if(x.toLowerCase().startsWith("i")){
            x+="(A)";
        }else{
            x+="(V)";
        }
        
         //add unit to y
        if(y.toLowerCase().startsWith("i")){
            y+="(A)";
        }else if (y.toLowerCase().startsWith("v")){
            y+="(V)";
        }else{
            y+="(F)";
        }

        NumberAxis xAxis = new NumberAxis(x);
        xAxis.setAutoRangeIncludesZero(false);
        NumberAxis yAxis = new NumberAxis(y);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        //format page type name, exclude group name
        String name = pageType.getName();
        if(name.contains(")")){
            name = name.substring(name.indexOf(")")+1);
        }
        JFreeChart chart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT, plot, false);

        LegendItemCollection itemCollection = new LegendItemCollection();
        
        
        
        String p = pageType.getPName();
        //add ylables legends
        int serialIndex = 0;
        for (int pIndex = 0; pIndex < pValues.length; pIndex++) {
            ExLegendItem pItem = new ExLegendItem(String.format("%s%d", p, pIndex));
            pItem.setShape(ExLegendItem.UNUSED_SHAPE);
            pItem.setShapeVisible(false);
           
            //itemCollection.add(pItem);
            //List<ExLegendItem> childern = new ArrayList<>();
            //pItem.setChildren(childern);           
            for (int index = 0; index < yNames.length; index++) {
                ExLegendItem item = new ExLegendItem(yNames[index]);
                //set shape for y legend
                item.setShapeVisible(true);
                item.setShape(ChartUtil.getLargeShape(index));
                item.setFillPaint(ChartOptions.getDefaultColor(serialIndex));
                if (pIndex == 0) {
                    itemCollection.add(item);
                }
                //item.setParent(pItem);
                //childern.add(item);               
                serialIndex++;
            }
           
        }
       

        ExLegendItem item0 = null;
        List<ExLegendItem> children = null;
        if (pageType.hasP()) {
            item0 = new ExLegendItem(String.format("%s=", p));
//                        item.setFillPaint(Color.BLACK);
            item0.setShape(ExLegendItem.UNUSED_SHAPE);
            item0.setShapeVisible(false);
            item0.setLabelPaint(Color.BLACK);

            SeriesProp seriesProp = new DefaultSeriesProp();
            item0.setSeriesKey(seriesProp);
//                        seriesProp.putProp(SeriesProp.PROP_P, SeriesProp.VALUE_WILDCARD);
            itemCollection.add(item0);

            children = new ArrayList<ExLegendItem>();
            item0.setChildren(children);
        }
        for (int pIndex = 0; pIndex < pValues.length; pIndex++) {
            double pValue = pValues[pIndex];
//                            ExLegendItem item = new ExLegendItem(String.format("%s=%.3g", p, pValues[i]));
//                            ViewSeriesProp chartSeriesProp = new DefaultChartSeriesProp();
            ExLegendItem item;
            if (item0 == null) {
                item = new ExLegendItem(String.format("%s=%.4g", p, pValue));
            } else {
                item = new ExLegendItem(String.format("%.4g", pValue));
            }
//                            item = new ExLegendItem(String.format("%s=%.4g", p, pValues[i]));
            item.setShape(ExLegendItem.UNUSED_SHAPE);
            item.setShapeVisible(false);

            item.setLabelPaint(ChartOptions.getDefaultColor(pIndex*yNames.length));

            DefaultSeriesProp seriesProp = new DefaultSeriesProp();
            item.setSeriesKey(seriesProp);
            seriesProp.putProp(SeriesProp.PROP_P, pValue);
            itemCollection.add(item);
            if (item0 != null) {
                item.setParent(item0);
                children.add(item);
            }
        }
        LegendTitle legend = new LegendTitle(new LegendItemSourceImpl(itemCollection));
        ChartUtil.customizeLegendTitle(legend);
        chart.addLegend(legend);

        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setInsets(ChartUtil.DEFAULT_ONE_INSETS);
        TagMutableProvider tagMutableProvider = viewProperties;
        if (tagMutableProvider != null && tagMutableProvider.getTagNumber() > 0) {
            int tagNumber = tagMutableProvider.getTagNumber();
            for (int i = 0; i < tagNumber; i++) {
//                plot.addAnnotation(tagMutableProvider.getTag(i));
                Tag tag = tagMutableProvider.getTag(i);
                tag.setSelected(false);
                plot.addAnnotation(tag);
            }
        }

        plot.setDomainPannable(true);
        plot.setRangePannable(true);

        ChartUtil.customizeLinearRangeAxis((NumberAxis) plot.getRangeAxis(), 0.05);
        ChartUtil.customizeLinearDomainAxis((NumberAxis) plot.getDomainAxis());
        ChartUtil.applyStyle(chart, style);
        renderer.setBaseShapesFilled(false);

        return chart;
    }

    @Override
    public MeaViewProperties getViewProperties() {
        return viewProperties;
    }

    @Override
    public MeaViewProperties getViewProperties(int legendIndex) {
        return viewProperties;
    }

    @Override
    public AlgorithmContext getAlgorithmContext() {
        return this;
    }

    @Override
    public String getLocatedPath(String path) {
        return path;
    }

    @Override
    public MeaPopupMenu getPopupMenu(MeaPopupMenu lastPopupMenu, ChartPanel chartPanel, JFreeChart chart, ViewDisplayer viewDisplayer, ViewDisplayerContext viewDisplayerContext) {
        DefaultMeaPopupMenu popupMenu;
        if (lastPopupMenu != null && lastPopupMenu instanceof DefaultMeaPopupMenu) {
            popupMenu = (DefaultMeaPopupMenu) lastPopupMenu;
        } else {
            popupMenu = new DefaultMeaPopupMenu(viewDisplayerContext);
            popupMenu.updateViewerStatus(lastPopupMenu);
        }

        popupMenu.updateData(viewDisplayer, chart, getViewProperties(), this, keys);
        return popupMenu;
    }
}
