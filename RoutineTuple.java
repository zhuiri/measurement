/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.admin.VarProviderEditingTableModel.IconableImpl;
import com.platformda.iv.measure.MeaDevice;
import com.platformda.iv.measure.MeaDeviceGroup;
import com.platformda.iv.measure.MeaSubDie;
import com.platformda.utility.Iconable;

/**
 * it's more like MeaDevice x Routine
 *
 * @author Junyi
 */
public class RoutineTuple {

    // auxiliary fields
    public MeaSubDie subDie;
    public MeaDeviceGroup deviceGroup;
    // critical
    public MeaDevice meaDevice;
    public Routine routine;
    //
    Iconable[] iconables = null;

    public Iconable getDefaultIconable() {
        return getIconable(WaferDieInfo.STATUS_NOT_MEAED);
    }

    public Iconable getIconable(int status) {
        if (iconables == null) {
            iconables = new Iconable[WaferDieInfo.STATUS_NUMBER];
        }

        if (iconables[status] == null) {
            iconables[status] = new IconableImpl(routine.getName(), routine.getIcon(status));
        }
        return iconables[status];
    }
}
