/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.api.Bus;
import com.platformda.iv.api.BusException;
import com.platformda.iv.api.Creater;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.MeterProfile;
import com.platformda.iv.api.Profile;
import com.platformda.iv.api.Unit;
import com.platformda.iv.api.UnitBond;
import com.platformda.iv.constants.MeasureConstants;
import com.platformda.iv.instrument.meter.pda3536.PDA3536Creater;
import com.platformda.iv.instrument.meter.pda3536.PDA3536Profile;
import com.platformda.iv.instrument.meter.pda4139.PDA4139Creater;
import com.platformda.iv.instrument.meter.pda4139.PDA4139Profile;
import com.platformda.iv.tools.SettingManager;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbPreferences;

/**
 *
 * @author Junyi
 */
public class InstrumentManager {
    
    private static InstrumentManager instance;
    
    public static final String PROP_DUMMY = "dummy";
    boolean dummy = false;
    EntityInstrument dummyMeter;
    EntityInstrument dummyProbe;
    
    public static InstrumentManager getInstance() {
        if (instance == null) {
            instance = new InstrumentManager();
        }
        return instance;
    }
    
    private InstrumentManager() {
//        try {
//            load(PathUtil.getConfigPath());
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
        List<Creater> creaters = CreaterManager.getAllCreaters();
        for (int index = 0; index < creaters.size(); index++) {
            Creater creater = creaters.get(index);
            EntityInstrument entityInstrument = null;
            if (creater instanceof PDA4139Creater) {
                entityInstrument = new EntityInstrument(new PDA4139Creater(), MeasureConstants.PDASMU, MeasureConstants.PDASMU);
                PDA4139Profile profile = (PDA4139Profile) entityInstrument.getProfile();
                //set from setting.xml
                String ip = SettingManager.getInsance().readValue("ip");
                String port = SettingManager.getInsance().readValue("port");
                if (ip != null) {
                    profile.setIp(ip);
                } else {
                    profile.setIp("192.168.1.120");
                }

                if (port != null) {
                    profile.setPort(new Integer(port));
                } else {
                    profile.setPort(5000);
                }
            }else if (creater instanceof PDA3536Creater){
                entityInstrument = new EntityInstrument(new PDA3536Creater(), MeasureConstants.PDACMU, MeasureConstants.PDACMU);
                PDA3536Profile profile = (PDA3536Profile) entityInstrument.getProfile();
                //set from setting.xml
                String ip = SettingManager.getInsance().readValue("ip");
                String port = SettingManager.getInsance().readValue("port");
                if (ip != null) {
                    profile.setIp(ip);
                } else {
                    profile.setIp("192.168.1.120");
                }

                //set default one
                profile.setPort(8000);
//                if (port != null) {
//                    profile.setPort(new Integer(port));
//                } else {
//                    profile.setPort(5000);
//                }
            }

            instruments.add(entityInstrument);
        }

   
      
      
    }
    List<EntityInstrument> instruments = new ArrayList<EntityInstrument>();
    
    
      public int getGpibBoardId() {
        return gpibBoardId;
    }

    public void setGpibBoardId(int gpibBoardId) {
        this.gpibBoardId = gpibBoardId;
    }

    public boolean isDummy() {
        return dummy;
    }

    public void setDummy(boolean dummy) {
        this.dummy = dummy;
        NbPreferences.forModule(InstrumentManager.class).putBoolean(InstrumentManager.PROP_DUMMY, dummy);
    }

    public Bus createBus() throws BusException {
        int id = InstrumentManager.getInstance().getGpibBoardId();
        Bus bus = new GPIBBus(new JGPIB(), id, "GPIB" + id);
        return bus;
    }
    
    public EntityInstrument getProbe() {
//        if (isDummy()) {
//            if (dummyProbe == null) {
//                Creater creater = new DummyProbeCreater();
//                String name = creater.getNames()[0];
//                dummyProbe = new EntityInstrument(creater, name, name);
//            }
//            return dummyProbe;
//        }

        for (EntityInstrument ei : instruments) {
            if (ei.getType() == Instrument.TYPE_PROBE && ei.isEnabled()) {
                return ei;
            }
        }
        return null;
    }
    
    
    public EntityInstrument getInstrument(String instName) {
        for (EntityInstrument ei : instruments) {
            if (ei.getInstrumentName().equalsIgnoreCase(instName)) {
                return ei;
            }
        }
        return null;
    }
    
    public List<EntityInstrument> getInstruments(){
        return this.instruments;
    }
    
    public List<UnitBond> getTermBonds() {
        List<UnitBond> bonds = new ArrayList<UnitBond>();
        for (EntityInstrument ei : instruments) {
            if (ei.getType() == Instrument.TYPE_METER) {
//                Meter meter = (Meter) ei.getInstrument();
                MeterProfile profile = (MeterProfile) ei.getProfile();
                int termNumber = profile.getUnitNumber();
                for (int i = 0; i < termNumber; i++) {
                    Unit term = profile.getUnit(i);
                    UnitBond bond = new UnitBond(ei.getInstrumentName(), term);
                    bonds.add(bond);
                }
            }
        }
        
        return bonds;
    }
}
