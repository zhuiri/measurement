/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.algorithm.AlgorithmContext;
import com.platformda.chartview.util.ExLegendItem;
import com.platformda.chartview.util.XYPlainSeries;
import com.platformda.chartview.util.XYPlainSeriesCollection;
import com.platformda.iv.project.MeaProject;
import com.platformda.iv.MeaSpace;
import com.platformda.view.api.AbstractView;
import com.platformda.view.api.ViewBrowser;
import com.platformda.view.api.ViewDisplayer;
import com.platformda.view.api.ViewDisplayerContext;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Drawable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author junyi
 */
public class OverlapView extends AbstractView implements MeaView, AlgorithmContext {

    List<MeaView> subViews = new ArrayList<MeaView>();
    protected Lookup lkp;

    public OverlapView(List<MeaView> subViews) {
        this.subViews = subViews;
    }

    public List<MeaView> getSubViews() {
        return subViews;
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
        MeaView firstView = subViews.get(0);
        JFreeChart firstChart = null;
        firstChart = firstView.getChart(style);
        // apply properties
        MeaViewProperties viewProperties = firstView.getViewProperties();
        viewProperties.apply(firstView, firstChart, true);

        XYPlot firstPlot = firstChart.getXYPlot();
        int datasetIndex = firstPlot.getDatasetCount();

//        List<String> xLabels = new ArrayList<String>();
//        List<String> yLabels = new ArrayList<String>();
//        xLabels.add(firstChart.getXLabel());
//        yLabels.add(firstChart.getYLabel());

        for (int i = 1; i < subViews.size(); i++) {
            MeaView subView = subViews.get(i);
            JFreeChart subChart = null;
            subChart = subView.getChart(style);
            XYPlot xyPlot = subChart.getXYPlot();

//            String xlabel = subChart.getXLabel();
//            if (!StringUtil.contains(xLabels, xlabel)) {
//                xLabels.add(xlabel);
//            }
//            String ylabel = subChart.getYLabel();
//            if (!StringUtil.contains(yLabels, ylabel)) {
//                yLabels.add(ylabel);
//            }
            int datasetNumber = xyPlot.getDatasetCount();
            int seriesCount = 0;
            int yNumber = 0;
            int pNumber = 1;
            if(subView instanceof MeaPageView ){
                MeaPageView pageView = (MeaPageView) subView; 
                yNumber = pageView.getPage().getYNumber();
                pNumber = pageView.getPage().getPNumber();
            }
           
            for (int j = 0; j < datasetNumber; j++) {
                XYItemRenderer itemRenderer = xyPlot.getRenderer(j);
                if (itemRenderer instanceof MeaXYLineAndShapeRenderer) {
                    MeaXYLineAndShapeRenderer lineAndShapeRenderer = (MeaXYLineAndShapeRenderer) itemRenderer;
                    lineAndShapeRenderer.setDatasetIndex(datasetIndex);
                     XYDataset dataset = xyPlot.getDataset(j);
                     if(dataset instanceof XYPlainSeriesCollection){
                         XYPlainSeriesCollection xydataset = (XYPlainSeriesCollection) dataset;
                         seriesCount = xydataset.getSeriesCount();
                         for(int seriesIndex =0;seriesIndex <seriesCount;seriesIndex++){
                            XYPlainSeries series =  xydataset.getSeries(seriesIndex);
                            lineAndShapeRenderer.setSeriesPaint(seriesIndex, ChartOptions.getDefaultColor(i*datasetNumber*seriesCount+seriesIndex));
                         }
                        
                     }
                     
                    
                }
                firstPlot.setRenderer(datasetIndex, itemRenderer);
                firstPlot.setDataset(datasetIndex, xyPlot.getDataset(j));
                datasetIndex++;
            }

            LegendTitle subLegendTitle = subChart.getLegend();
            if (subLegendTitle != null) {
               LegendItemSource[] sources =  subLegendTitle.getSources();
               if(sources.length == 1){
                   LegendItemSource source = sources[0];
                   if(source instanceof LegendItemSourceImpl){
                       LegendItemSourceImpl sourceImlpl = (LegendItemSourceImpl)source;
                       LegendItemCollection itemCollection = sourceImlpl.getLegendItems();
                       for(int itemIndex = 0;itemIndex<itemCollection.getItemCount();itemIndex++){
                           LegendItem item = itemCollection.get(itemIndex);
                           if(itemIndex<yNumber){
                               item.setFillPaint(ChartOptions.getDefaultColor(i*datasetNumber*seriesCount+itemIndex));
                           }else if(itemIndex>yNumber){
                               int pIndex  = (itemIndex - yNumber -1)%pNumber;
                               item.setLabelPaint(ChartOptions.getDefaultColor(i*datasetNumber*seriesCount+pIndex*yNumber));
                           }
                           
                       }
                   }
               }
                firstChart.addLegend(subLegendTitle);
            }
        }

//        if (xLabels.size() > 1) {
//            ValueAxis valueAxis = firstPlot.getDomainAxis();
//            valueAxis.setLabel(StringUtil.concatenate(xLabels, ","));
//        }
//        if (yLabels.size() > 1) {
//            ValueAxis valueAxis = firstPlot.getRangeAxis();
//            valueAxis.setLabel(StringUtil.concatenate(yLabels, ","));
//        }

        return firstChart;
    }

//    @Override
//    public String getXLabel() {
//        return null;
//    }
//
//    @Override
//    public String getYLabel() {
//        return null;
//    }
    @Override
    public String getTitle() {
        return "Overlap";
    }

    // customize popup
    @Override
    public MeaPopupMenu getPopupMenu(MeaPopupMenu lastPopupMenu, ChartPanel exChartPanel, JFreeChart chart, ViewDisplayer viewDisplayer, ViewDisplayerContext viewDisplayerContext) {
        // TODO:
        OverlapPopupMenu popupMenu;
        if (lastPopupMenu != null && lastPopupMenu instanceof OverlapPopupMenu) {
            popupMenu = (OverlapPopupMenu) lastPopupMenu;
        } else {
            popupMenu = new OverlapPopupMenu();
            popupMenu.updateViewerStatus(lastPopupMenu);
        }
        popupMenu.updateData(chart, viewDisplayer, viewDisplayerContext, this);
        return popupMenu;
    }

    
    public List<MeaViewProperties> getAllViewProperties(){
        List<MeaViewProperties> result =  new ArrayList<MeaViewProperties>();
        for(int index = 0;index<subViews.size();index++){
            result.add(getViewProperties(index));
        }        
        return result ;
    }
    
    
    @Override
    public MeaViewProperties getViewProperties() {
        return subViews.get(0).getViewProperties();
    }

    @Override
    public MeaViewProperties getViewProperties(int legendIndex) {
        if (legendIndex >= 0 && legendIndex < subViews.size()) {
            return subViews.get(legendIndex).getViewProperties();
        }
        return null;
    }

//    @Override
//    public Element getPropertiesAsElement(FilterData filterData) {
//        return null;
//    }
//
//    @Override
//    public void loadPropertiesFromElement(Element elem, Workspace workspace, FilterData filterData) {
//    }
    @Override
    public AlgorithmContext getAlgorithmContext() {
        return this;
    }

    @Override
    public String getLocatedPath(String path) {
        return path;
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
}
