package com.platformda.iv.measure;

import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.analysis.MeaBundle;
import com.platformda.iv.analysis.MeaOutput;
import com.platformda.iv.analysis.Sweep;
import com.platformda.iv.analysis.SweepMeaBundle;
import com.platformda.iv.analysis.SyncMeaBundle;
import com.platformda.iv.api.Capacity;
import com.platformda.iv.api.Compliance;
import com.platformda.iv.api.InputType;
import com.platformda.iv.api.Instrument;
import com.platformda.iv.api.MeaContext;
import com.platformda.iv.api.MeaResponse;
import com.platformda.iv.api.MeaType;
import com.platformda.iv.api.Meter;
import com.platformda.iv.api.MeterProfile;
import com.platformda.iv.api.NodeBond;
import com.platformda.iv.api.OutputType;
import com.platformda.iv.api.Unit;
import com.platformda.iv.api.UnitConstImpl;
import com.platformda.iv.api.UnitOutput;
import com.platformda.iv.api.UnitVar;
import com.platformda.utility.common.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * keypoints to understand this class:
 *
 * 1. handle relative bias
 *
 * 2. split big measurement to smaller ones
 *
 * split logic 1. sub-sweep 2. output * 3. total point
 *
 * 3. AnalysisResponse: data measured in page
 *
 */
public class AnalysisGauge extends StoppableImpl implements Gauge {

    private static final Logger logger = Logger.getLogger(AnalysisGauge.class.getName());
//    private GaugeContext context;
    
    private int singlePointIndex = 0;

    public AnalysisGauge(Console console) {
        super(console);
    }

    /**
     * measure the specified analysis. assumptions: 1. meters have been added to
     * the bus and properly initialized. 2. matrix, if any, has been correctly
     * set up. 3. probe, if any, has moved to the right position.
     *
     * @param anlysis
     * @return AnalysisResponse
     */
    @Override
    public MeaResponse doGauge(GaugeContext context) throws IOException {
//        this.context = context;

        MeaAnalysis analysis = (MeaAnalysis) context.getRequest();
        Instrument[] meters = context.getInstruments();
        NodeBond[] bonds = context.getBonds();
        // find the main meter , set the first as default
        Meter mainMeter = (Meter) MeaUtil.getInstrument(meters, MeaUtil.getUnit(analysis.getOutput(0).getNode(), bonds));
        if (mainMeter == null) {
            throw new IOException("No meter found to measure " + analysis.getName());
        }

        MeasuringContext measuringContext = new MeasuringContext();
        EntityPagePattern pattern = analysis.getPagePattern();
        if(pattern!=null){
            measuringContext.pagePattern = pattern;
        }
        EntityDevice device = context.getDevice();
//        instContext.analysisName = analysis.getName();
        if (device != null) {
            measuringContext.deviceType = device.getDeviceType();
            measuringContext.devicePolarity = device.getDevicePolarity();
        }
        measuringContext.bonds = context.getBonds();
        measuringContext.meaType = analysis.getMeaType();
        // since the limitation object may depend on this information, should set it here.
        boolean splitToPoint = analysis.getMeaType().equals(MeaType.CV);//MeaUtil.isCVNotReferenceOnLow(analysis, bonds);
        measuringContext.bundleType = !splitToPoint && analysis.getBiases()[0].getBundle(0) instanceof Sweep ? MeaContext.TYPE_SWEEP : MeaContext.TYPE_LIST;
        measuringContext.console = context.getConsole();
        mainMeter.setMeaContext(measuringContext);

        // parses the relative correlation between biases 
        MeaBias[] meaBiases = analysis.getMeasureBiases();
        Capacity capacity = mainMeter.getCapacity();
        OrderedBias[] orderedBiases = MeaUtil.getOrderedBiases(meaBiases, capacity, measuringContext, analysis.getRefNode(), analysis.getMeaType().equals(MeaType.CV), splitToPoint);

        AnalysisResponse response = new AnalysisResponse(analysis, context.getPageCallback(), context.getDevice(), context.getStressTime());

        AnalysisTuple tuple = new AnalysisTuple();
        tuple.initMeters(mainMeter, meters);
        tuple.table = response.getTable();
        tuple.analysis = analysis;
        tuple.orderedBiases = orderedBiases;
        tuple.outputs = analysis.getOutputsInArray();
        tuple.bonds = bonds;
        tuple.capacity = capacity;
        tuple.bases = tuple.buildBases();
        tuple.startIndice = new int[orderedBiases.length];

        measureByBias(tuple, orderedBiases.length - 1);
        return response;
    }

    // construct the whole bias level by level recursively
    void measureByBias(AnalysisTuple tuple, int biasIndex) throws IOException {
        if (biasIndex == -1) {
            measureByOutput(tuple);
            return;
        }

        OrderedBias currentBias = tuple.orderedBiases[biasIndex];
        List<MeaBundle> currentBundles = currentBias.bias.getMeaBundles();
        List<MeaBundle> backupBundles = new ArrayList<MeaBundle>(currentBundles);

        int start = 0;
        // split bundles
        for (MeaBundle bundle : backupBundles) {
            currentBundles.clear();
            currentBundles.add(bundle);

            // update the start indices
            tuple.startIndice[biasIndex] = start;
            measureByBias(tuple, biasIndex - 1);
            start += bundle.size();
        }

        // restore all bundles
        currentBundles.clear();
        currentBundles.addAll(backupBundles);
    }

    // measure all the outputs as many as possible within the capacity
    void measureByOutput(AnalysisTuple tuple) throws IOException {
        int maxOutput = tuple.capacity.getMaxOutput();
        MeaOutput[] outputs = tuple.outputs;
        int outputNumber = tuple.analysis.getOutputNumber();
        int meaedNumber = 0;
        tuple.outputStartIndex = 0;
        while (meaedNumber < outputNumber) {
            tuple.outputStartIndex = meaedNumber;
            int nextOutput = meaedNumber + maxOutput < outputNumber ? meaedNumber + maxOutput : outputNumber;
            MeaOutput[] subOutputs = new MeaOutput[nextOutput - meaedNumber];
            System.arraycopy(outputs, meaedNumber, subOutputs, 0, subOutputs.length);
            tuple.outputs = subOutputs;
            measureByPoint(tuple);
            meaedNumber += subOutputs.length;
        }

        tuple.outputs = outputs;
    }

    // measure all the points by splitting the 2nd level sweep if total number of points exceeds the capacity of the meter
    void measureByPoint(AnalysisTuple tuple) throws IOException {
        // total number of points
        int total = tuple.size();
        int maxPoint = tuple.capacity.getMaxPoint();
        if (total < maxPoint) {
            measureByTuple(tuple);
        } else {
            OrderedBias[] orderedBiases = tuple.orderedBiases;
            MeaBias secondBias = orderedBiases[1].bias;
            // only one meabundle here
            MeaBundle secondBundle = secondBias.getBundle(0);
            int step = maxPoint / (total / secondBias.size());
            MeaBundle[] splitSecondBundles = secondBundle.split(step);
            int backupStart = tuple.startIndice[1];
            for (int i = 0; i < splitSecondBundles.length; i++) {
                secondBias.clearBundles();
                secondBias.addBundle(splitSecondBundles[i]);
                if (i > 0) {
                    tuple.startIndice[1] += splitSecondBundles[i - 1].size();
                }
                measureByTuple(tuple);
            }
            tuple.startIndice[1] = backupStart;

            // restore
            secondBias.clearBundles();
            secondBias.addBundle(secondBundle);
        }
    }

    void measureByTuple(AnalysisTuple tuple) throws IOException {
        pauseOrStop();

        OrderedBias[] orderedBiases = tuple.orderedBiases;
        MeaBias[] biases = tuple.getMeaBiases();
        double[] shifts = MeaUtil.getShifts(orderedBiases, biases);

        MeaBundle syncBundle = null;
        for (int biasIndex = 0; biasIndex < orderedBiases.length; biasIndex++) {
            if (biasIndex == 1 && orderedBiases[biasIndex].type == OrderedBias.TYPE_SYNC) {
                // SyncMeaBundle already                
                syncBundle = orderedBiases[biasIndex].bias.getBundle(0);
                if (syncBundle instanceof SyncMeaBundle) {
                    continue;
                }
                biases[biasIndex].clearBundles();
                SyncMeaBundle syncMeaBundle = new SyncMeaBundle((SweepMeaBundle) biases[0].getBundle(0));
                syncMeaBundle.setRatio(1);
                syncMeaBundle.setOffset(syncBundle.get(0));
                biases[biasIndex].addBundle(syncMeaBundle);
                continue;
            }
            biases[biasIndex].getBundle(0).shift(shifts[biasIndex]);
        }
        //shift CMU-L as zero V
        MeaUtil.shiftOnCVLowPot(biases, shifts, tuple.bonds);

        double[] data = doMeasure(tuple.mainMeter, tuple.extraMeters, biases, tuple.outputs, tuple.bonds, tuple.analysis.getCompliances());

        // restore biases
        for (int i = 0; i < orderedBiases.length; i++) {
            if (i == 1 && orderedBiases[i].type == OrderedBias.TYPE_SYNC) {
                biases[i].clearBundles();
                // restore bundle
                biases[i].addBundle(syncBundle);
                continue;
            }
            // shift back
            biases[i].getBundle(0).shift(-shifts[i]);
        }

        int outputNumber = tuple.outputs.length;      
        int pNumber = tuple.analysis.getPNumber();
        int pointNumber = data.length / outputNumber / pNumber;
        //int pointNumber = data.length / outputNumber;
        int outColumnIndex = 0;

        int[] innerBases = tuple.buildBases();
        int[] indice = new int[innerBases.length];

        // output base
        MeaAnalysis analysis = tuple.analysis;
        for (int i = 0; i < tuple.outputStartIndex; i++) {
            MeaOutput output = analysis.getOutput(i);
            outColumnIndex += output.getType().getDataSize();
        }

        RowExpandedTable table = tuple.table;
        //CV support-single point
        if (data.length == 1) {
            table.setValue(singlePointIndex, 0, data[0]);
            singlePointIndex++;
        } else {
            for (int outputIndex = 0; outputIndex < outputNumber; outputIndex++) {
                MeaOutput output = tuple.outputs[outputIndex];
                OutputType outputType = output.getType();
                for (int pIndex = 0; pIndex < pNumber; pIndex++) {
                    for (int pointIndex = 0; pointIndex < pointNumber; pointIndex++) {
//                    queryIndice(pointIndex, innerBases, indice);
//                    for (int biasIndex = 0; biasIndex < indice.length; biasIndex++) {
//                        indice[biasIndex] += tuple.startIndice[biasIndex];
//                    }
//                    int row = 0;
//                    for (int biasIndex = 0; biasIndex < indice.length; biasIndex++) {
//                        row += indice[biasIndex] * tuple.bases[biasIndex];
//                    }
                        int row = 0;
                        //modify the row and index
                        row = pointNumber * pIndex + pointIndex;
                        int index = pointNumber * outputNumber * pIndex + pointNumber * outputIndex + pointIndex;
                        // feed the value for each output
                        for (int dataIndex = 0; dataIndex < outputType.getDataSize(); dataIndex++) {
                            // TODO, there must be a bug here, k should be used to index data in data array, probably because we
                            // currently don't support multi-number data, such as impedance. we ignore this currently
                            table.setValue(row, outColumnIndex + dataIndex, data[index]);
                        }
                    }
                }
                outColumnIndex += outputType.getDataSize();
            }
        }
      
        int a = 0;
    }

    double[] doMeasureSingle(Meter mainMeter, Meter[] extraMeters, MeaBias[] biases, MeaOutput[] outputs, NodeBond[] bonds, Compliance[] comps) throws IOException {
//        // use apply and read to do single point measurement, but it doesn't work for CV
//        Meter[] meters = new Meter[extraMeters.length + 1];
//        meters[0] = mainMeter;
//        System.arraycopy(extraMeters, 0, meters, 1, extraMeters.length);
//        //apply biases
//        for (MeaBias meaBias : biases) {
//            String node = meaBias.getFrom();
//            double value = meaBias.get(0);
//            Unit unit = MeaUtil.getUnit(node, bonds);
//            if (InputType.VOLTAGE.equals(meaBias.getType()) && value == 0 && unit == null) {
//                //ignore zero volts, assume it connects to the GND
//                continue;
//            }
//            if (unit == null) {
//                throw new IOException("Can't find unit for node " + node);
//            }
//            Meter meter = MeaUtil.getMeter(meters, unit);
//            double comp = MeaUtil.getCompliance(node, meaBias.getType(), comps);
//            meter.apply(new UnitConstImpl(unit, meaBias.getType(), comp, value));
//        }
//        //read outputs
//        double[] data = new double[outputs.length];
//        for (int i = 0; i < outputs.length; i++) {
//            String node = outputs[i].getNode();
//            Unit unit = MeaUtil.getUnit(node, bonds);
//            if (unit == null) {
//                throw new IOException("Can't find unit for node " + node);
//            }
//            Meter meter = MeaUtil.getMeter(meters, unit);
//            data[i] = meter.read(unit, outputs[i].getType());
//        }
//        return data;
        return null;
    }

    /**
     * *
     * This method do the actual measurement work with the given parameters,
     *
     * We may need different measure "algorithms" for different analyses in
     * future.
     *
     * to support this kind of flexibility, we need to define an interface for
     * the
     *
     * "algorithm".
     *
     * the meaning and layout of the data in the returned array vary between
     * different kinds of meters, but should be the same for the same kind of
     * meter IV meter: [data for the first output e.g. ids, data for the second
     * output e.g. igs] for each output, the format is: [first curve, second
     * curve, ...] for each curve, the format is: [first point, second point,
     * ...] for each point, the format is: [first data e.g. real(z), second data
     * e.g. img(z), ...]
     *
     * @param measureMeter
     * @param extraMeters
     * @param biases
     * @param outputs
     * @param bonds
     * @param comps
     * @return
     * @throws IOException
     */
    double[] doMeasure(Meter mainMeter, Meter[] extraMeters, MeaBias[] biases, MeaOutput[] outputs, NodeBond[] bonds, Compliance[] comps) throws IOException {
        // use apply and read to do single point measurement, but it doesn't work for CV
//        if (biases[0].size() == 1 && !outputs[0].getType().isCapacitance()) {
//            return doMeasureSingle(mainMeter, extraMeters, biases, outputs, bonds, comps);
//        }

        MeterProfile profile = null;
        List<MeaBiasCalibration> calibrations = null;
        List<UnitConstAdaptor> constAdaptors = new ArrayList<>();

//        boolean hasVMU = false;
//        TestVMU:
//        for (int i = 0; i < extraMeters.length; i++) {
//            profile = (MeterProfile) extraMeters[i].getProfile();
//            // meterInputs.clear();
//            for (int j = 0; j < profile.getUnitNumber(); j++) {
//                Unit unit = profile.getUnit(j);
//                if (unit.getUnitType() == Unit.TYPE_VMU) {
//                    MeaBias bias = MeaUtil.getUnitBias(unit, bonds, biases);
//                    if (bias != null) {
//                        hasVMU = true;
//                        break TestVMU;
//                    }
//                }
//            }
//        }
//        for (int i = 0; i < extraMeters.length; i++) {
//            profile = (MeterProfile) extraMeters[i].getProfile();
//            // meterInputs.clear();
//            for (int j = 0; j < profile.getUnitNumber(); j++) {
//                Unit unit = profile.getUnit(j);
//                MeaBias bias = MeaUtil.getUnitBias(unit, bonds, biases);
//                if (bias != null) {
//                    //consider the case where two terminals connect to the same node
//                    String nodeName = null;
//                    for (int k = 0; k < bonds.length; k++) {
//                        if (bonds[k].getUnit().equals(unit)) {
//                            nodeName = bonds[k].getNodeName();
//                            break;
//                        }
//                    }
//                    int count = 0;
//                    for (int k = 0; k < bonds.length; k++) {
//                        if (bonds[k].getNodeName().equals(nodeName)) {
//                            count++;
//                        }
//                    }
//                    if (count > 1) {
//                        // TODO: special case, like using vmu to monitor target voltage
////                        if (term.getTermType() == Term.TYPE_VMU || term.getTermType() == Term.TYPE_SMU ) {
//                        if (unit.getUnitType() == Unit.TYPE_VMU) {
//                            List<Unit> unitList = MeaUtil.getUnitsInList(nodeName, bonds);
//                            unitList.remove(unit);
//                            if (unitList.size() == 1) {
//                                Unit mainUnit = unitList.get(0);
//                                if (MeaUtil.hasUnit(mainMeter, mainUnit)) {
//                                    double initialValue = bias.get(0);
////                                    List<MeaBiasCalibration> temps = new ArrayList<MeaBiasCalibration>();
//                                    for (int calTime = 0; calTime < 5; calTime++) {
//                                        double value = bias.get(0);
//                                        UnitConstImpl termConstImpl = new UnitConstImpl(mainUnit, bias.getType(), MeaUtil.getCompliance(bias.getFrom(), bias.getType(), comps), value);
//                                        mainMeter.apply(termConstImpl);
//                                        double real = extraMeters[i].read(unit, OutputType.VOLTAGE);
//                                        double shift = initialValue - real;
//                                        if (Math.abs(shift) < 2e-3) {
//                                            if (calTime > 0) {
//                                                logger.log(Level.WARNING, "Calibrate OK, expected and result:{0} {1}", new Object[]{initialValue, real});
//                                            }
//                                            break;
//                                        }
//                                        if (calTime > 1) {
//                                            logger.log(Level.WARNING, "Calibrate Times: {0}", calTime);
//                                        }
//                                        if (Math.abs(shift) > 5e-3) {
//                                            logger.log(Level.WARNING, "Large shift detected by VMU, expected and result:{0} {1}", new Object[]{initialValue, real});
//                                        } else {
//                                            logger.log(Level.WARNING, "Shift detected by VMU, expected and result:{0} {1}", new Object[]{initialValue, real});
//                                        }
//                                        MeaBiasCalibration calibration = new MeaBiasCalibration(bias, shift);
//                                        calibration.apply();
//                                        if (calibrations == null) {
//                                            calibrations = new ArrayList<MeaBiasCalibration>();
//                                        }
//                                        calibrations.add(calibration);
////                                        temps.add(calibration);
//                                    }
//                                }
//                            }
//                        }
//                    } else {
//                        UnitConstAdaptor constAdaptor = new UnitConstAdaptor(unit, bias, MeaUtil.getCompliance(bias.getFrom(), bias.getType(), comps));
//                        constAdaptors.add(constAdaptor);
//                        extraMeters[i].apply(constAdaptor);
//                        if (hasVMU) {
//                            logger.log(Level.FINE, "Apply {0} {1} on node {2} through {3}", new Object[]{bias.getType().getName(), bias.get(0), bias.getFrom(), unit.getName()});
//                        }
//                    }
//                }
//            }
//        }

        // organize inputs for measure meter
        List<UnitVar> unitVars = new ArrayList(biases.length);
        for (int i = 0; i < biases.length; i++) {
            String node = biases[i].getFrom();
            for (int j = 0; j < bonds.length; j++) {
                if (bonds[j].getNodeName().equals(node)) {
                    if (MeaUtil.hasUnit(mainMeter, bonds[j].getUnit())) {
                        unitVars.add(new MeaBiasAsUnitVarAdaptor(bonds[j].getUnit(), biases[i], MeaUtil.getCompliance(biases[i].getFrom(), biases[i].getType(), comps)));
                    }
                }
            }
        }

        MeaBiasAsUnitVarAdaptor[] unitVarsInArray = new MeaBiasAsUnitVarAdaptor[unitVars.size()];
        unitVars.toArray(unitVarsInArray);

        UnitOutput[] unitOutputs = new UnitOutput[outputs.length];
        for (int i = 0; i < unitOutputs.length; i++) {
            Unit unit = MeaUtil.getUnit(outputs[i].getNode(), bonds);
            unitOutputs[i] = new MeaOutputAsUnitOutputAdaptor(unit, outputs[i]);
        }
        mainMeter.setup(unitVarsInArray, unitOutputs);
        //for algorithm 
        mainMeter.setBiases(biases);
        double[] data = null;
        if (mainMeter.getMeaContext().getMeaType().equals(MeaType.CV)) {
//            for (int i = 0; i < extraMeters.length; i++) {
//                UnitConstAdaptor constAdaptor = new UnitConstAdaptor(unit, bias, MeaUtil.getCompliance(bias.getFrom(), bias.getType(), comps));
////                        constAdaptors.add(constAdaptor);
////                        extraMeters[i].apply(constAdaptor);
//            }
//            
//           
            extraMeters[0].setMeaContext(mainMeter.getMeaContext());
            extraMeters[0].apply(biases);
            //wait to SMU
            try{
//                 logger.log(Level.FINE, "Apply {0} {1} on node {2} through {3}", new Object[]{bias.getType().getName(), bias.get(0), bias.getFrom(), unit.getName()});
                 Thread.sleep(10);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
           
            data = mainMeter.emit();


            //after measure 
            extraMeters[0].afterMeasure(biases);
            
        }else{
             // IV measure
            data = mainMeter.emit();
        }
       
     
             
        if (calibrations != null) {
            for (MeaBiasCalibration calibration : calibrations) {
                calibration.restore();
            }
        }

        return data;
    }

    void queryIndice(int lineIndex, int[] bases, int[] indice) {
        for (int i = indice.length - 1; i >= 0; i--) {
            indice[i] = lineIndex / bases[i];
            lineIndex = lineIndex % bases[i];
        }
    }

    // calibration
    static class MeaBiasCalibration {

        MeaBias meaBias;
        double calibration;

        public MeaBiasCalibration(MeaBias meaBias, double calibration) {
            this.meaBias = meaBias;
            this.calibration = calibration;
        }

        public void apply() {
            meaBias.getBundle(0).shift(calibration);
        }

        public void restore() {
            meaBias.getBundle(0).shift(-calibration);
        }
    }
}
