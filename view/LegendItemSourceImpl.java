/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import java.io.Serializable;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.LegendItemSource;

/**
 *
 * @author Junyi
 */
public class LegendItemSourceImpl implements LegendItemSource, Serializable {

    LegendItemCollection itemCollection;

    public LegendItemSourceImpl(LegendItemCollection itemCollection) {
        this.itemCollection = itemCollection;
    }

    @Override
    public LegendItemCollection getLegendItems() {
        return itemCollection;
    }
}
