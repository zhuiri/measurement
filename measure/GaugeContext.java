package com.platformda.iv.measure;

import com.platformda.datacore.EntityDevice;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.MeaRequest;
import com.platformda.iv.api.NodeBond;
import com.platformda.utility.common.Console;

/**
 * A context for gauge
 *
 */
public interface GaugeContext {

    public MeaRequest getRequest();

    public Instrument[] getInstruments();

    public NodeBond[] getBonds();
    
    public EntityDevice getDevice();
    
    public int getStressTime(); // <=0 means not stress

    public Console getConsole();

    public PageCallback getPageCallback();
}
