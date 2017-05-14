package com.platformda.iv.measure;

import com.platformda.iv.admin.EntityInstrument;
import com.platformda.iv.admin.Routine;
import com.platformda.iv.api.Bus;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.MeterProfile;
import com.platformda.iv.api.Unit;
import com.platformda.iv.instrument.bus.socket.SocketBus;
import com.platformda.utility.common.Console;
import com.platformda.utility.ui.SwingUtil;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class ManualMeasurer extends AbstractMeasurer {

//    RoutineTupleFilter routineTupleFilter;
    //
    Routine routine;
    private MeasurerContext context;

    public ManualMeasurer(Console console, Routine routine) {
        super(console);
        this.routine = routine;
    }

    @Override
    public void doMeasurement(MeasurerContext context, ProgressHandle handle) throws IOException {
        // TODO should make use of probe if it exists
        this.context = context;

        boolean shouldZero = context.shouldZero();
        MeaCallback callback = context.getCallback();

        Set<EntityInstrument> matrices = new LinkedHashSet();
        Set<EntityInstrument> meters = new LinkedHashSet();
        routine.fetchInstruments(meters, context);

        Bus bus =  new SocketBus();
        // TODO: determine cv/av
        boolean hasZeroAnalysis = false;

        for (EntityInstrument ei : meters) {
            if (ei.getType() == Instrument.TYPE_METER) {
//                Instrument meter = ei.getInstrument();
                MeterProfile meterProfile = (MeterProfile) ei.getProfile();
                int unitNumber = meterProfile.getUnitNumber();
                for (int i = 0; i < unitNumber; i++) {
                    Unit unit = meterProfile.getUnit(i);
                    if (unit.getUnitType() == Unit.TYPE_CMU) {
                        hasZeroAnalysis = true;
                        break;
                    }
                }
            }
        }

        if (hasZeroAnalysis) {
            if (!hasCV(routine, context)) {
                hasZeroAnalysis = false;
            }
        }

        if (hasZeroAnalysis && shouldZero) {
            SwingUtil.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    String msg = "Going to zero, please separate the probe.";
                    NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            });
        }

        MeasurerHelper measurerHelper = new MeasurerHelper(meters);

        try {
          
            callback.start();
            measurerHelper.connect(bus, hasZeroAnalysis && shouldZero);
            if (hasZeroAnalysis && shouldZero) {
                SwingUtil.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        String msg = "Zero done, please contact the probe.";
                        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                });
            }           
            measureDevice(context, routine, context);
        } finally {
            callback.finish();
            //after pause,console when to finish
//            if(pause){
//                 console.info("Measurment is cancelled, new measurement could be started");
//            }
            measurerHelper.disconnect();
            if (bus != null) {
                bus.close();
            }
        }
    }

    @Override
    public MeasurerContext getContext() {
        return context;
    }
}
