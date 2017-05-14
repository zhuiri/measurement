/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.chartview.util.PopupViewerPanel;
import com.platformda.chartview.util.PopupViewerPanelOwner;
import com.platformda.report.ReportUtil;
import com.platformda.utility.common.BasicFileFilter;
import com.platformda.utility.common.LoadSaveUtil;
import com.platformda.view.api.View;
import com.platformda.view.api.ViewDisplayer;
import com.platformda.view.api.ViewDisplayerContext;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import org.jfree.ui.Drawable;
import org.openide.util.Exceptions;

/**
 *
 * @author junyi
 */
public abstract class BaseMeaPopupMenu extends MeaPopupMenu implements PopupViewerPanelOwner {

    protected JMenuItem dumpToPNGItem = new JMenuItem("Dump To PNG");
    PopupViewerPanel dataViewerPanel;
    boolean dataViewerVisible = false;

    @Override
    public PopupViewerPanel getDataViewerPanel() {
        return dataViewerPanel;
    }

    @Override
    public void setDataViewerPanel(PopupViewerPanel dataViewerPanel) {
        this.dataViewerPanel = dataViewerPanel;
        if (dataViewerPanel != null) {
            dataViewerPanel.setOwner(this);
        }
    }

    @Override
    public boolean isDataViewerVisible() {
        return dataViewerVisible;
    }

    @Override
    public void setDataViewerVisible(boolean dataViewerVisible) {
        this.dataViewerVisible = dataViewerVisible;
    }

    @Override
    public void updateViewerStatus(MeaPopupMenu lastPopupMenu) {
        if (lastPopupMenu != null) {
            this.setDataViewerVisible(lastPopupMenu.isDataViewerVisible());
            this.setDataViewerPanel(lastPopupMenu.getDataViewerPanel());

        }
    }

    @Override
    public void clear() {
        if (dataViewerPanel != null) {
            dataViewerPanel.dispose();
        }
    }

    @Override
    public void viewerPanelDisposed(PopupViewerPanel viewerPanel) {
        if (viewerPanel == dataViewerPanel) {
            dataViewerDialogDisposed();
        }
    }

    public void dataViewerDialogDisposed() {
        dataViewerVisible = false;
    }

    public void dumpToPng(ViewDisplayerContext viewDisplayerContext, ViewDisplayer viewDisplayer, MeaView view) {
        String path = String.format("%s.png", view.getTitle());
        path = path.replaceAll("[^a-zA-Z0-9_\\-\\.]", "_");
        final File resultFile = LoadSaveUtil.saveFile("Dump To PNG", JFileChooser.FILES_ONLY, "DirPNG", null,
                new BasicFileFilter("png"), path, true);
        if (resultFile == null) {
            return;
        }

        Map hints = ReportUtil.getRenderingHints();

        Dimension dimension = new Dimension(800, 480);
        BufferedImage image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHints(hints);

        Drawable drawable = view.getDrawable(View.STYLE_PDF);
        if (drawable != null) {
            Rectangle subRec = new Rectangle(0, 0, dimension.width, dimension.height);
            drawable.draw(g2d, subRec);
        }
        g2d.dispose();
        try {
            ImageIO.write(image, "png", resultFile);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
