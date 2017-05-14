package com.platformda.iv.measure;

import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.ep.Node;
import com.platformda.ep.PEP;
import com.platformda.ep.PEPUtil;
import com.platformda.iv.MeaDAO;
import com.platformda.iv.MeaData;
import com.platformda.iv.admin.EntityInstrument;
import com.platformda.iv.admin.Routine;
import com.platformda.iv.admin.RoutineSelector;
import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.MeaRequest;
import com.platformda.iv.api.MeaResponse;
import com.platformda.iv.api.NodeBond;
import com.platformda.iv.spec.SpecGauge;
import com.platformda.iv.spec.SpecRequest;
import com.platformda.iv.spec.SpecResponse;
import com.platformda.iv.tools.LogHandlerManager;
import com.platformda.spec.SpecData;
import com.platformda.spec.SpecPattern;
import com.platformda.utility.common.Console;
import com.platformda.utility.common.LoaderUtil;
import com.platformda.utility.ui.SwingUtil;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public abstract class AbstractMeasurer extends StoppableImpl implements Measurer {

    private static final Logger logger = Logger.getLogger(AbstractMeasurer.class.getName());
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Gauge gauge;

    public AbstractMeasurer(Console console) {
        super(console);
         if(logger.getHandlers().length == 0){
             logger.addHandler(LogHandlerManager.getInstance().getDefault());
         }
        
    }

    protected boolean hasCV(Routine routine, RoutineSelector routineTupleFilter) {
        List<EntityPagePattern> pagePatterns = routine.getPagePatterns();
        for (EntityPagePattern pagePattern : pagePatterns) {
            if (routineTupleFilter.isSelected(routine, pagePattern)) {
                String[] yNames = pagePattern.getYNames();
                for (String yName : yNames) {
                    if (yName.toLowerCase().startsWith("c")) {
                        return true;
                    } else if (yName.contains("(")) {
//                                int index = yName.indexOf("(");
                        String exp = LoaderUtil.getWrappedContent(yName, '(', ')');
                        PEP pep = PEP.getInstance();
                        pep.parseExpression(exp);
                        Node node = pep.getTopNode();
                        List<String> vars = new ArrayList<String>();
                        PEPUtil.fetchVarNames(node, vars);
                        for (String var : vars) {
                            if (var.toLowerCase().startsWith("c")) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        List<SpecPattern> specPatterns = routine.getSpecPatterns();
        for (SpecPattern specPattern : specPatterns) {
            if (routineTupleFilter.isSelected(routine, specPattern)) {
                // XXX
                if (specPattern.getName().toLowerCase().startsWith("c")) {
                    return true;
                }
            }
        }
        return false;
    }

    // called by ManualMeasurer
    protected void measureDevice(MeasurerContext context, Routine routine, RoutineSelector routineTupleFilter) throws IOException {
       // long start2 = System.currentTimeMillis();
        MeaCallback callback = context.getCallback();
        MeaData meaData = context.getMeaData();
        MeaDAO dao = context.getDao();

        // measure each device
//        MeaDevice meaDevice = deviceGroup.getDevice(deviceIndex);
//                EntityDevice device = meaDevice.getDevice();
        EntityDevice meaedDevice = routine.device;

        Map<DeviceBond, List<SpecRequest>> specMap = new LinkedHashMap();
        routine.groupSpecByBond(specMap, meaData, routineTupleFilter);

        Set<EntityInstrument> meters = new LinkedHashSet();

        NodeBond[] preBonds = null;
        for (Map.Entry<DeviceBond, List<SpecRequest>> entry : specMap.entrySet()) {
            DeviceBond deviceBond = entry.getKey();
            List<SpecRequest> specRequests = entry.getValue();
            NodeBond[] nodeBonds = deviceBond.getBonds();

            meters.clear();
            deviceBond.fetchInstruments(meters);

            MeasurerHelper helper = new MeasurerHelper(meters);
            try {
                helper.open();

                verifyBond(preBonds, nodeBonds, deviceBond);
                for (SpecRequest specRequest : specRequests) {
                    SpecData specData = meaSpec(meaData, meaedDevice, specRequest, helper.getInstruments(), nodeBonds);
                    meaData.saveSpecData(dao, specData);
                }
            } finally {
                helper.close();
            }
            preBonds = nodeBonds;
        }
        
        // logger.log(Level.INFO, "test time: {0}ms", new Object[]{ (System.currentTimeMillis() - start2)});

        // non-stress
        Map<DeviceBond, List<MeaAnalysis>> analysisMap = new LinkedHashMap();
        long start1 = System.currentTimeMillis();
        routine.groupPageByBond(analysisMap, meaedDevice.getDevicePolarity(), routineTupleFilter);
        long end = System.currentTimeMillis();
        
         logger.log(Level.INFO, "Parsing page time: {0}ms", new Object[]{ (end - start1)});

        for (Map.Entry<DeviceBond, List<MeaAnalysis>> entry : analysisMap.entrySet()) {               
            DeviceBond deviceBond = entry.getKey();
            List<MeaAnalysis> meaAnalyses = entry.getValue();
            NodeBond[] nodeBonds = deviceBond.getBonds();
            meters.clear();          
            deviceBond.fetchInstruments(meters);
            MeasurerHelper helper = new MeasurerHelper(meters);
           
            try {
                 long start3 = System.currentTimeMillis();
                helper.open();   
                 logger.log(Level.INFO, "open.... time: {0}ms", new Object[]{ (System.currentTimeMillis() - start3)});
                verifyBond(preBonds, nodeBonds, deviceBond);
               
                for (MeaAnalysis analysis : meaAnalyses) {
//                    callback.startAnalysis(deviceIndex + 1, analysis.getName());                    
                    AnalysisPage[] pages = measureAnalysis(analysis, helper.getInstruments(), nodeBonds, meaedDevice, 0);

//                    Routine routine = analysis.getRoutine();
                    double start = System.currentTimeMillis();  
                    if (routine != null) {
                        List<AnalysisFixedPage> resolvedPages = new ArrayList<AnalysisFixedPage>();
                        for (AnalysisPage analysisPage : pages) {
                            resolvedPages.add((AnalysisFixedPage) analysisPage);
                        }
                        
                        meaData.savePages(meaedDevice, analysis, resolvedPages);
                        
                    }
                    logger.log(Level.INFO, " savePages consumed {0}ms", new Object[]{System.currentTimeMillis()-start});
                }
              
            } finally {
                helper.close();
            }
            preBonds = nodeBonds;
        }
    }

    protected void verifyBond(NodeBond[] preBonds, NodeBond[] nodeBonds, DeviceBond deviceBond) {
        if (preBonds != null && !nodeBonds.equals(preBonds)) {
            // connection changed, enable user to change the connection
            final StringBuffer message = new StringBuffer("<html>Please change connection to ");
            message.append(deviceBond.getName());
            message.append(" as following:<br>");
            for (int p = 0; p < nodeBonds.length; p++) {
                if (p > 0) {
                    message.append(';');
                }
                message.append(nodeBonds[p].getNodeName());
                message.append(':');
                message.append(nodeBonds[p].getUnit().getName());
            }
            message.append("</html>");
            SwingUtil.invokeAndWait(new Runnable() {
                @Override
                public void run() {
//                            JOptionPane.showMessageDialog(optionParent, message.toString(), "Connection Change", JOptionPane.INFORMATION_MESSAGE);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(message.toString(), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            });
        }
    }

    protected SpecData meaSpec(MeaData meaData, final EntityDevice device, final SpecRequest specRequest, final Instrument[] meters, final NodeBond[] bonds) throws IOException {
        pauseOrStop();
        GaugeContext gc = new GaugeContext() {
            @Override
            public Console getConsole() {
                return getContext().getConsole();
            }

            @Override
            public Instrument[] getInstruments() {
                return meters;
            }

            @Override
            public NodeBond[] getBonds() {
                return bonds;
            }

            @Override
            public MeaRequest getRequest() {
                return specRequest;
            }

            @Override
            public PageCallback getPageCallback() {
                return getContext().getPageCallback();
            }

            @Override
            public EntityDevice getDevice() {
                return device;
            }

            @Override
            public int getStressTime() {
                return 0;
            }
        };

        long start = System.currentTimeMillis();
        logger.log(Level.INFO, "Start measuring spec: {0} at {1}", new Object[]{specRequest.getName(), dateFormat.format(new Date())});
        StringBuilder builder = new StringBuilder();
        builder.append(specRequest.getName()).append("\n");
        builder.append("    ").append("Start measuring spec: " ).append(" at " ).append(dateFormat.format(new Date()));                
        console.info(builder.toString());
        gauge = new SpecGauge(meaData, getContext().getConsole(), device);
        MeaResponse response = gauge.doGauge(gc);

        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Done measuring spec: {0} at {1}, time consumed: {2}s", new Object[]{specRequest.getName(), dateFormat.format(new Date()), (end - start) / 1000.0});
        // specRequest.getName() + 
        console.info("    Done  measuring spec: " +" at " + dateFormat.format(new Date()) + ", time consumed: " + (end - start) / 1000.0 + "s");

        if (pause) {
            console.info("Measurment is cancelled, new measurement could be started");
        }
        if (response == null) {
            return null;
        }
        return ((SpecResponse) response).getSpecData();
    }

    /**
     * measure the specified analysis. assumptions: 1. meters have been added to
     * the bus and properly initialized. 2. matrix, if any, has been correctly
     * set up. 3. probe, if any, has moved to the right position.
     *
     * @param analysis
     * @return the resolved pages
     */
    protected AnalysisPage[] measureAnalysis(final MeaAnalysis analysis, final Instrument[] meters, final NodeBond[] bonds, final EntityDevice device, final int stressTime) throws IOException {
        pauseOrStop();

        GaugeContext gc = new GaugeContext() {
            @Override
            public Console getConsole() {
                return getContext().getConsole();
            }

            @Override
            public Instrument[] getInstruments() {
                return meters;
            }

            @Override
            public NodeBond[] getBonds() {
                return bonds;
            }

            @Override
            public MeaRequest getRequest() {
                return analysis;
            }

            @Override
            public PageCallback getPageCallback() {
                return getContext().getPageCallback();
            }

            @Override
            public EntityDevice getDevice() {
                return device;
            }

            @Override
            public int getStressTime() {
                return stressTime;
            }
        };
        long start = System.currentTimeMillis();

        logger.log(Level.INFO, "Start measuring page: {0} at {1}", new Object[]{analysis.getName(), dateFormat.format(new Date())});
        StringBuilder builder = new StringBuilder();
        builder.append(analysis.getName()).append("\n");
        builder.append("    ").append("Start measuring page: ").append(" at ").append(dateFormat.format(new Date()));
        console.info(builder.toString());
        gauge = new AnalysisGauge(getContext().getConsole());
        MeaResponse response = gauge.doGauge(gc);

        long end = System.currentTimeMillis();
        logger.log(Level.INFO, "Done measuring page: {0} at {1}, time consumed: {2}s", new Object[]{analysis.getName(), dateFormat.format(new Date()), (end - start) / 1000.0});
        //+ analysis.getName()
        console.info("    Done  measuring page: "  + " at " + dateFormat.format(new Date()) + ", time consumed: " + (end - start) / 1000.0 + "s");
        if (pause) {
            console.info("Measurment is cancelled, new measurement could be started");
        }
        if (response == null) {
            return null;
        }
        return ((AnalysisResponse) response).getPages();
    }

    @Override
    public synchronized void pause(Object obj) {
        if (pause) {
            // do nothing when already paused
            return;
        }
        pauseObj = obj;
        this.pause = true;
        if (gauge != null) {
            gauge.pause(obj);
        }
    }

    @Override
    public synchronized void resume() {
        if (gauge != null) {
            gauge.resume();
        }
        synchronized (pauseObj) {
            pause = false;
            pauseObj.notifyAll();
        }
    }

    public abstract MeasurerContext getContext();
}
