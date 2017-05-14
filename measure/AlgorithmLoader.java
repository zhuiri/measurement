/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.iv.admin.RoutinePatternManager;
import com.platformda.iv.tools.LogHandlerManager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author renjing
 */
public final class AlgorithmLoader {
    private static final Logger logger = Logger.getLogger(AlgorithmLoader.class.getName());
    private static AlgorithmLoader instance;
    
    static {
        String lib = "DaReWrapper";
        try {           
            System.loadLibrary(lib);         
        }catch(Exception e){
//            e.printStackTrace();
            logger.log(Level.SEVERE, "load library {0} falied!", lib);
            LogHandlerManager.getInstance().logException(logger, e);
        }
       
    }
    
    public AlgorithmLoader() {
        if (logger.getHandlers().length == 0) {
            logger.addHandler(LogHandlerManager.getInstance().getDefault());
        }
        File etcFile = InstalledFileLocator.getDefault().locate("etc", RoutinePatternManager.CODE_BASE, false);
        String path = etcFile.getAbsolutePath() + "/" + "all_mea_info.json";
        int result = init_all_measure_info_wrapper(path);
        if(result!=0){
              logger.log(Level.SEVERE, "all_mea_info falied!");
        }
    }
    
    public static AlgorithmLoader getInstance(){
        if(instance == null){
            instance = new AlgorithmLoader();      
        }
        
        return instance;
    }
    
    
    
    
public native int init_all_measure_info_wrapper(String path);
    
public native int[] data_sample_wrapper(String measure_info, int[] x_sample_indexes,double ratio,double[] x_sample);

public native int data_recovery_wrapper(double[] x_sample, double[][] y_sample,
	  double machine_acc, String measure_info, double[][] y);
    
}
