/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.algorithm.SeriesAlgorithm;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author junyi
 */
public class MeaViewPropertyChangeSupport {

    List<Reference<MeaViewPropertyChangeListener>> listeners = new ArrayList<Reference<MeaViewPropertyChangeListener>>();

    public void addPropertyChangeListener(MeaViewPropertyChangeListener listener) {
        for (Iterator<Reference<MeaViewPropertyChangeListener>> it = listeners.iterator(); it.hasNext();) {
            Reference<MeaViewPropertyChangeListener> tc = it.next();
            MeaViewPropertyChangeListener tpListener = tc.get();
            if (listener == tpListener) {
                return;
            }
        }
        listeners.add(new WeakReference<MeaViewPropertyChangeListener>(listener));
    }

    public void removePropertyChangeListener(MeaViewPropertyChangeListener listener) {
        for (Iterator<Reference<MeaViewPropertyChangeListener>> it = listeners.iterator(); it.hasNext();) {
            Reference<MeaViewPropertyChangeListener> tc = it.next();
            MeaViewPropertyChangeListener tpListener = tc.get();
            if (listener == tpListener) {
                it.remove();
                return;
            }
        }
    }

    public void firePropertyChanged() {
        for (Iterator<Reference<MeaViewPropertyChangeListener>> it = listeners.iterator(); it.hasNext();) {
            Reference<MeaViewPropertyChangeListener> tc = it.next();
            MeaViewPropertyChangeListener listener = tc.get();
            if (listener == null) {
                it.remove();
            } else {
                listener.viewPropertyChanged();
            }
        }
    }

    public void fireAlgorithmChanged(SeriesAlgorithm oldAlgorithm, SeriesAlgorithm newAlgorithm) {
        for (Iterator<Reference<MeaViewPropertyChangeListener>> it = listeners.iterator(); it.hasNext();) {
            Reference<MeaViewPropertyChangeListener> tc = it.next();
            MeaViewPropertyChangeListener listener = tc.get();
            if (listener == null) {
                it.remove();
            } else {
                listener.algorithmChanged(oldAlgorithm, newAlgorithm);
            }
        }
    }

    public void fireSelectionChanged() {
        for (Iterator<Reference<MeaViewPropertyChangeListener>> it = listeners.iterator(); it.hasNext();) {
            Reference<MeaViewPropertyChangeListener> tc = it.next();
            MeaViewPropertyChangeListener listener = tc.get();
            if (listener == null) {
                it.remove();
            } else {
                listener.selectionChanged();
            }
        }
    }
}
