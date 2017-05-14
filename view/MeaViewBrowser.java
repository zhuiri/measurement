/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.chartview.api.ChartUtil;
import com.platformda.chartview.util.EnhancedChartPanel;
import com.platformda.iv.MeaSpace;
import com.platformda.iv.actions.DataInfoAction;
//import com.platformda.system.DataInfoAction;
import com.platformda.system.EditableProject;
import com.platformda.view.api.View;
import com.platformda.view.api.ViewBrowser;
import com.platformda.view.api.ViewDisplayer;
import com.platformda.view.api.ViewDisplayerContext;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.util.List;
import javax.swing.JPopupMenu;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.openide.util.NbPreferences;

/**
 *
 * @author Junyi
 */
public class MeaViewBrowser implements ViewBrowser {

    ViewDisplayerContext viewDisplayerContext;
    MeaSpace meaSpace;

    public MeaViewBrowser(ViewDisplayerContext viewDisplayerContext, MeaSpace meaSpace) {
        this.viewDisplayerContext = viewDisplayerContext;
        this.meaSpace = meaSpace;
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public Component getViewComponent(ViewDisplayer viewDisplayer, View lastView, Component lastComponent, View view) {
        MeaPopupMenu lastPopupMenu = null;

        if (lastView != null && lastComponent != null && (lastView instanceof MeaView)) {
            ChartPanel chartPanel = (ChartPanel) lastComponent;
            JPopupMenu popupMenu = chartPanel.getPopupMenu();
            if (popupMenu != null && popupMenu instanceof MeaPopupMenu) {
                lastPopupMenu = (MeaPopupMenu) popupMenu;
            }
        }

        EnhancedChartPanel chartPanel = null;
        if (view instanceof MeaPageView) {
            MeaPageView chartView = (MeaPageView) view;
            DefaultMeaViewProperties properties = (DefaultMeaViewProperties) chartView.getViewProperties();

            JFreeChart chart = chartView.getChart(View.STYLE_NORMAL);
            properties.apply(chartView, chart, true);

            MeaChartPanel xyChartPanel = new MeaChartPanel(meaSpace, chart, properties, chartView);

            MeaPopupMenu popupMenu = chartView.getPopupMenu(lastPopupMenu, chartPanel, chart, viewDisplayer, viewDisplayerContext);
            xyChartPanel.setFilterViewPopupMenu(popupMenu);
            properties.addPropertyChangeListener(xyChartPanel);

            chartPanel = xyChartPanel;
            chartPanel.setTagEnabled(true);
            chartPanel.setTagMutableProvider(properties);
        } else if (view instanceof OverlapView) {
            OverlapView overlapView = (OverlapView) view;
            JFreeChart chart = overlapView.getChart(View.STYLE_NORMAL);
            if (chart != null) {
                ChartUtil.applyStyle(chart, View.STYLE_NORMAL);
//                chartPanel = new EnhancedChartPanel(chart);
                List<MeaViewProperties> viewProperties = overlapView.getAllViewProperties();

                chartPanel = new MeaChartPanel(meaSpace, chart, viewProperties, overlapView);
                MeaPopupMenu popupMenu = overlapView.getPopupMenu(lastPopupMenu, chartPanel, chart, viewDisplayer, viewDisplayerContext);
                chartPanel.setPopupMenu(popupMenu);

                chartPanel.setTagEnabled(true);
                chartPanel.setTagMutableProvider(overlapView.getViewProperties());
            }
        }

        if (chartPanel != null) {
            boolean dataInfoEnabled = NbPreferences.forModule(DataInfoAction.class).getBoolean(DataInfoAction.PREFERENCE_DATAINFO, false);
            chartPanel.setCrossHairEnalbed(dataInfoEnabled);
            chartPanel.setMouseWheelEnabled(true);
//            chartPanel.setTagEnabled(true);
//            chartPanel.setTagMutableProvider(afv.getViewProperties());
//            chartPanel.setInitialDelay(200);
            chartPanel.setDismissDelay(10000);
        }
        return chartPanel;
    }

    @Override
    public void discardViewComponent(ViewDisplayer viewDisplayer, View lastView, Component lastComponent, View view) {
        if (lastView instanceof MeaView) {
            MeaView meaView = (MeaView) lastView;
            MeaViewProperties properties = meaView.getViewProperties();
            if (properties instanceof DefaultMeaViewProperties && lastComponent instanceof MeaChartPanel) {
                DefaultMeaViewProperties xyvp = (DefaultMeaViewProperties) properties;
                MeaChartPanel chartPanel = (MeaChartPanel) lastComponent;
                xyvp.removePropertyChangeListener(chartPanel);
            }
        }

        if (view == null) {
            if (lastComponent != null) {
                ChartPanel chartPanel = (ChartPanel) lastComponent;

                JPopupMenu popupMenu = chartPanel.getPopupMenu();
                if (popupMenu != null && popupMenu instanceof MeaPopupMenu) {
                    MeaPopupMenu filterViewPopupMenu = (MeaPopupMenu) popupMenu;
                    filterViewPopupMenu.clear();
                }
            }
        }
    }

    @Override
    public void propertyChange(ViewDisplayer viewDisplayer, View lastView, Component lastComponent, View view, PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(EditableProject.PROP_DATAINFO)) {
            if (lastComponent != null && lastComponent instanceof EnhancedChartPanel) {
                EnhancedChartPanel chartPanel = (EnhancedChartPanel) lastComponent;
                boolean dataInfoEnabled = NbPreferences.forModule(DataInfoAction.class).getBoolean(DataInfoAction.PREFERENCE_DATAINFO, false);
                chartPanel.setCrossHairEnalbed(dataInfoEnabled);
            }
        }
    }
}
