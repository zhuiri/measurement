/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.iv.admin.EntityInstrument;
import com.platformda.iv.api.Bus;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.Line;
import com.platformda.iv.api.Meter;
import com.platformda.iv.api.Profile;
import com.platformda.iv.constants.MeasureConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Junyi
 */
public class MeasurerHelper {

    Collection<EntityInstrument> meters;
    //
    List<Instrument> connecteds = new ArrayList();
    Instrument[] instruments;

    public MeasurerHelper(Collection<EntityInstrument> meters) {
        this.meters = meters;
    }

    public void connect(Bus bus, boolean shouldZero) throws IOException {
        // add all meters to bus
        for (EntityInstrument entityMeter : meters) {
            Instrument meter = entityMeter.getInstrument();
            Profile profile = entityMeter.getProfile();
            if (meter.isOnGlobalBus()) {
                Line line = bus.connect(profile.getAddress());
                meter.setLine(line);
                connecteds.add(meter);
            }
            meter.setProfile(profile);

            if (entityMeter.getDriverName().equalsIgnoreCase(MeasureConstants.PDACMU)&&
                shouldZero && entityMeter.getType() == Instrument.TYPE_METER) {
                // TODO Bug here: if we have several setups for the same meter
                // the zero process would only be done with one setup.
                ((Meter) meter).zeroCalibration();
            }
        }
    }

    public void disconnect() throws IOException {
        for (int i = connecteds.size() - 1; i >= 0; i--) {
            Instrument inst = (Instrument) connecteds.get(i);
            inst.getLine().disconnect();
        }
    }

    public void open() throws IOException {
        // initiate meters
        instruments = new Instrument[meters.size()];
        int meterIndex = 0;
        for (EntityInstrument ei : meters) {
            instruments[meterIndex] = ei.getInstrument();
            Profile config = ei.getProfile();
            instruments[meterIndex].setProfile(config);
            instruments[meterIndex].open();
            meterIndex++;
        }
    }

    public Instrument[] getInstruments() {
        return instruments;
    }

    public void close() throws IOException {
        for (int i = 0; i < instruments.length; i++) {
            instruments[i].close();
        }
    }

    public void reopenInstruments() throws IOException {
        for (int i = 0; i < instruments.length; i++) {
            instruments[i].close();
            instruments[i].open();
        }
    }
}
