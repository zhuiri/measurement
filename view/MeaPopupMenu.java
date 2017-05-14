/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.chartview.util.PopupViewerPanel;
import javax.swing.JPopupMenu;

/**
 *
 * @author junyi
 */
public abstract class MeaPopupMenu extends JPopupMenu {

    // call back 
    public abstract void updateData();

    public abstract PopupViewerPanel getDataViewerPanel();

    public abstract void setDataViewerPanel(PopupViewerPanel dataViewerPanel);

    public abstract boolean isDataViewerVisible();

    public abstract void setDataViewerVisible(boolean dataViewerVisible);

    public abstract void updateViewerStatus(MeaPopupMenu lastPopupMenu);

    public abstract void clear();
}
