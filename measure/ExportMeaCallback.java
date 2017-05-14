/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.iv.MeaData;
import com.platformda.iv.MeaSpace;
import com.platformda.iv.actions.ExportAction;
import com.platformda.iv.admin.ConsoleImpl;
import com.platformda.iv.admin.RoutineSelector;
import java.io.File;
import java.util.logging.Logger;

/**
 *
 * @author Junyi
 */
public class ExportMeaCallback implements MeaCallback {
   private static final Logger logger = Logger.getLogger(ExportMeaCallback.class.getName());
//    public 
    ExportAction exportAction = new ExportAction();
    MeaSpace meaSpace;
    MeaData meaData;
    RoutineSelector routineTupleFilter;
    File dir;

    public ExportMeaCallback(MeaSpace meaSpace, MeaData meaData, RoutineSelector routineTupleFilter, File dir) {
        this.meaSpace = meaSpace;
        this.meaData = meaData;
        this.routineTupleFilter = routineTupleFilter;
        this.dir = dir;
    }

    @Override
    public void start() {
        
    }

    @Override
    public void finish() {
        // TODO:
       exportAction.dumpDevicesAndRoutinesCycle(meaData.getRoutines(),meaData,routineTupleFilter,dir,false);
       //log
       logger.info("save data to file is finished.");
       final ConsoleImpl console = new ConsoleImpl("Export Log");
       console.info("save data finished to "+dir.getPath());
    }
}
