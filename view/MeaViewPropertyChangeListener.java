/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.algorithm.SeriesAlgorithm;

/**
 *
 * @author junyi
 */
public interface MeaViewPropertyChangeListener {

    public void viewPropertyChanged();

    public void algorithmChanged(SeriesAlgorithm oldAlgorithm, SeriesAlgorithm newAlgorithm);

    public void selectionChanged();
}
