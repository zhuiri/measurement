/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DevicePolarity;
import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceType;
import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.EntityPageType;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.datacore.pattern.EntityPagePatternGroup;
import com.platformda.datacore.pattern.FuncField;
import com.platformda.iv.datacore.pattern.PagePatternLoader;
import com.platformda.ep.Node;
import com.platformda.ep.PEP;
import com.platformda.ep.PEPUtil;
import com.platformda.ep.VarContext;
import com.platformda.iv.MeaData;
import com.platformda.iv.analysis.AdvMeaOutput;
import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.analysis.MeaBundle;
import com.platformda.iv.analysis.MeaOutput;
import com.platformda.iv.analysis.SweepMeaBundle;
import com.platformda.iv.analysis.SyncMeaBundle;
import com.platformda.iv.api.Compliance;
import com.platformda.iv.api.InputType;
import com.platformda.iv.api.MeaType;
import com.platformda.iv.api.OutputType;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.iv.spec.SpecRequest;
import com.platformda.spec.Spec;
import com.platformda.spec.SpecData;
import com.platformda.spec.SpecPattern;
import com.platformda.spec.SpecPatternGroup;
import com.platformda.iv.spec.SpecPatternLoader;
import com.platformda.spec.SpecPatternProvider;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.common.Console;
import com.platformda.utility.common.LoaderUtil;
import com.platformda.utility.common.PathUtil;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.common.VarProvider;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jdom2.Element;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * like MeaSource
 *
 * @author Junyi
 */
public class Routine extends BaseVarProvider implements SpecPatternProvider, VarContext {

    public static final Image image = ImageUtilities.loadImage("com/platformda/iv/resources/device.gif");
    public static final Image partBadge = ImageUtilities.loadImage("com/platformda/iv/resources/partMeaed.png");
    public static final Image fullBadge = ImageUtilities.loadImage("com/platformda/iv/resources/fullMeaed.png");
    public static final Image waitingBadge = ImageUtilities.loadImage("com/platformda/iv/resources/waiting.png");
    //
    public static final Image partImage = ImageUtilities.mergeImages(image, partBadge, 8, 8);
    public static final Image fullImage = ImageUtilities.mergeImages(image, fullBadge, 8, 8);
    public static final Image waitingImage = ImageUtilities.mergeImages(image, waitingBadge, 8, 8);
 
    public static final Image stressBadgeImage = ImageUtilities.loadImage("com/platformda/mea/resources/blue-ball-mid.gif");
    public static final Image samplingBadgeImage = ImageUtilities.loadImage("com/platformda/mea/resources/badgeNeedsCheckout.gif");

//    public static final Image stressImage = ImageUtilities.mergeImages(image, stressBadgeImage, 10, 2);
    public static final Image stressImage = ImageUtilities.mergeImages(image, stressBadgeImage, 8, 1);
    public static final Image stressPartImage = ImageUtilities.mergeImages(stressImage, WaferDieInfo.partBadge, 8, 8);
    public static final Image stresFullImage = ImageUtilities.mergeImages(stressImage, WaferDieInfo.fullBadge, 8, 8);
    public static final Image samplingImage = ImageUtilities.mergeImages(image, samplingBadgeImage, 8, 0);
    public static final Image samplingPartImage = ImageUtilities.mergeImages(samplingImage, WaferDieInfo.partBadge, 8, 8);
    public static final Image samplingFullImage = ImageUtilities.mergeImages(samplingImage, WaferDieInfo.fullBadge, 8, 8);
    //
    public static final Icon partIcon = new ImageIcon(partImage);
    public static final Icon fullIcon = new ImageIcon(fullImage);
    public static final Icon icon = new ImageIcon(image);
    public static final Icon stressPartIcon = new ImageIcon(stressPartImage);
    public static final Icon stressFullIcon = new ImageIcon(stresFullImage);
    public static final Icon stressIcon = new ImageIcon(stressImage);
    public static final Icon samplingPartIcon = new ImageIcon(samplingPartImage);
    public static final Icon samplingFullIcon = new ImageIcon(samplingFullImage);
    public static final Icon samplingIcon = new ImageIcon(samplingImage);
    //
    public static final int STATUS_NOT_MEAED = 0;
    public static final int STATUS_PART_MEAED = 1;
    public static final int STATUS_MEAED = 2;
    public static Color[] COLORS = {null, new Color(243, 152, 117), new Color(127, 177, 227)};
    public static String[] STATUS_STRINGS = {"Not", "Part", "Full"};
    //
    public static final int TYPE_PAGE = 0;
    public static final int TYPE_SPEC = 1;
    protected DeviceType deviceType;
    public EntityDevice device;
    String name;
    String group;
    long id = 0;// source id, 0
    Compliance[] compliances = null;
    List<EntityPagePattern> pagePatterns = new ArrayList<EntityPagePattern>();
    List<SpecPattern> specPatterns = new ArrayList<SpecPattern>();
    List<IndPagePattern> indPagePatterns = new ArrayList<IndPagePattern>();
    List<IndSpecPattern> indSpecPatterns = new ArrayList<IndSpecPattern>();
    //
    SpecRequest[] specRequests;
    MeaAnalysis[] meaAnalyses;
//    Map<EntityPagePattern, int[]> pageTypeCache; //= new HashMap<EntityPagePattern, int[]>();
//    Map<EntityPagePattern, List<EntityPageType>> pageTypeMap = new HashMap<EntityPagePattern, List<EntityPageType>>();
    Map<String, DeviceBond> bondMap = new HashMap<String, DeviceBond>();
    //
//    protected List<PXYPage> pages = createPages();
//    protected List<SpecData> specDatas = createSpecDatas();

     public Routine(DeviceType deviceType) {
        this.deviceType = deviceType;

        String[] nodes = deviceType.getNodeNames();
        compliances = new Compliance[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            Compliance compliance = new Compliance(nodes[i]);
            compliances[i] = compliance;
        }
    }
    
    
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 43 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Routine other = (Routine) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        if (!Arrays.deepEquals(this.compliances, other.compliances)) {
            return false;
        }
        return true;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;

        if (compliances == null) {

            String[] nodes = deviceType.getNodeNames();
            compliances = new Compliance[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                Compliance compliance = new Compliance(nodes[i]);
                compliances[i] = compliance;
            }
        }
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public EntityDevice getDevice() {
        return device;
    }

    public void setDevice(EntityDevice device) {
        this.device = device;
        this.id = device.getId();
        setDeviceType(device.getDeviceType());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCompliances(Compliance[] compliances) {
        this.compliances = compliances;
    }

    public Compliance[] getCompliances() {
        return compliances;
    }
    
    
    public Icon getIcon() {
//        return icon;
        return getIcon(WaferDieInfo.STATUS_NOT_MEAED);
    }

    public Icon getIcon(int status) {
//        if (stressPattern != null) {
//            if (stressPattern.isSamplingEnabled()) {
//                if (status == WaferDieInfo.STATUS_NOT_MEAED) {
//                    return samplingIcon;
//                } else if (status == WaferDieInfo.STATUS_PART_MEAED) {
//                    return samplingPartIcon;
//                } else {
//                    return samplingFullIcon;
//                }
//            } else {
//                if (status == WaferDieInfo.STATUS_NOT_MEAED) {
//                    return stressIcon;
//                } else if (status == WaferDieInfo.STATUS_PART_MEAED) {
//                    return stressPartIcon;
//                } else {
//                    return stressFullIcon;
//                }
//            }
//        } else {
            if (status == WaferDieInfo.STATUS_NOT_MEAED) {
                return icon;
            } else if (status == WaferDieInfo.STATUS_PART_MEAED) {
                return partIcon;
            } else {
                return fullIcon;
            }
//        }
    }

    public void addIndPagePattern(IndPagePattern indPagePattern) {
        indPagePatterns.add(indPagePattern);
        pagePatterns.add(indPagePattern.pagePattern);
    }

    private boolean checkRepeatedSpecPattern(SpecPattern newPattern){
        for(int index = 0;index<specPatterns.size();index++){
            SpecPattern curPattern = specPatterns.get(index);
            if(newPattern.getName().equals(curPattern.getName())){
                 return true;    
            }                       
        }
        
        return false;
    }
    
    //check repeated
    public boolean addIndSpecPattern(IndSpecPattern indSpecPattern) {
        if(checkRepeatedSpecPattern(indSpecPattern.specPattern)){
            //repeated ,not add
            return false;
        }else{
            indSpecPatterns.add(indSpecPattern);
            specPatterns.add(indSpecPattern.specPattern);
            return true;
        }         
        
      
    }

    public List<EntityPageType> getPageTypes(EntityPagePattern pagePattern) {
        IndPagePattern ipp = getIndPagePattern(pagePattern);
        if (ipp != null) {
            return ipp.getPageTypes();
        }
        return null;
    }

    public void updatePageTypeMap(MeaData meaData) {
        for (IndPagePattern ipp : indPagePatterns) {
            ipp.updatePageTypes(meaData);
        }
    }

    public void removePagesByPagePattern(MeaData meaData, EntityPagePattern pagePattern) {
        List<EntityPageType> pageTypes = getPageTypes(pagePattern);
        if (pageTypes == null || pageTypes.isEmpty()) {
            return;
        }
        meaData.removePageTypes(pageTypes);
        pageTypes.clear();
    }

    public void updatePagePattern(MeaData meaData, EntityPagePattern pagePattern) {
        removePagesByPagePattern(meaData, pagePattern);
    }

    public void removePagePattern(MeaData meaData, EntityPagePattern pagePattern) {
        removePagesByPagePattern(meaData, pagePattern);
        for (int index = pagePatterns.size() - 1; index >= 0; index--) {
            if (pagePatterns.get(index) == pagePattern) {
                pagePatterns.remove(index);
                break;
            }
        }
//        pagePatterns.remove(pagePattern);

        for (Iterator<IndPagePattern> it = indPagePatterns.iterator(); it.hasNext();) {
            IndPagePattern ipp = it.next();
            if (ipp.pagePattern == pagePattern) {
                it.remove();
                break;
            }
        }
    }

    public EntityPagePattern getPagePattern(EntityPageType pageType) {
        for (IndPagePattern ipp : indPagePatterns) {
            List<EntityPageType> pageTypes = ipp.getPageTypes();
            if (pageTypes.contains(pageType)) {
                return ipp.pagePattern;
            }
        }
        return null;
    }

    public void removeSpecPattern(MeaData meaData, SpecPattern specPattern) {
        meaData.removeSpecData(device, specPattern);
        specPatterns.remove(specPattern);

        for (Iterator<IndSpecPattern> it = indSpecPatterns.iterator(); it.hasNext();) {
            IndSpecPattern ipp = it.next();
            if (ipp.specPattern == specPattern) {
                it.remove();
                break;
            }
        }
    }

    public int getMeaStatus(MeaData meaData, EntityDevice meaedDevice) {
        boolean hasMea = false;
        boolean hasNotMea = false;

        // check page
        for (EntityPagePattern pagePattern : pagePatterns) {
            if (meaData.hasPage(meaedDevice, pagePattern)) {
                hasMea = true;
            } else {
                hasNotMea = true;
            }
            if (hasNotMea && hasMea) {
                return STATUS_PART_MEAED;
            }
        }

        for (SpecPattern specPattern : specPatterns) {
            Spec spec = meaData.getSpecByName(specPattern.getName());
            SpecData specData = meaData.getSpecData(meaedDevice, spec);
            if (specData != null) {
                hasMea = true;
            } else {
                hasNotMea = true;
            }
            if (hasNotMea && hasMea) {
                return STATUS_PART_MEAED;
            }
        }
        if (hasNotMea) {
            if (hasMea) {
                return STATUS_PART_MEAED;
            }
            return STATUS_NOT_MEAED;
        } else {
            return STATUS_MEAED;
        }
    }

    public void clearPageTypeMap() {
        for (IndPagePattern ipp : indPagePatterns) {
            List<EntityPageType> pageTypes = ipp.getPageTypes();
            pageTypes.clear();
        }
    }

    ////////////////////////////////////////////////////////////
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    protected void resolveSpec(MeaData meaData) {
//        if (specRequests != null) {
//            return;
//        }
        // TODO: order depends
        RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(deviceType);
        specRequests = new SpecRequest[specPatterns.size()];
        for (int i = 0; i < specRequests.length; i++) {
            IndSpecPattern isp = indSpecPatterns.get(i);
            SpecPattern specPattern = specPatterns.get(i);
            // NOTE: by name not SpecPattern
//            Spec spec = meaData.getSpecByPattern(specPattern);
            Spec spec = meaData.getSpecByName(specPattern.getName());
            if (spec == null) {
                spec = new Spec();
                spec.setName(specPattern.getName());
//                spec.setPattern(specPattern);
                meaData.addSpec(spec);
            }

            SpecRequest specRequest = new SpecRequest();
            specRequest.routine = this;
            specRequest.spec = spec;
            specRequest.specPattern = specPattern;
            specRequest.deviceBond = routinePattern.getDeviceBond(specPattern);
            specRequest.varProvider = isp;
            specRequests[i] = specRequest;
        }
        for (SpecRequest specRequest : specRequests) {
            SpecPattern specPattern = specRequest.specPattern;
            List<SpecPattern> dependPatterns = specPattern.getAllDepends();
            if (dependPatterns != null) {
                Spec[] depends = new Spec[dependPatterns.size()];
                for (int i = 0; i < depends.length; i++) {
                    depends[i] = meaData.getSpecByName(dependPatterns.get(i).getName());
                }
                specRequest.depends = depends;
            }
        }
    }

    public MeaAnalysis resolvePage(final IndPagePattern ipp, final EntityDevice device, MeaData meaData) {
        final List<SpecData> specDatasByDevice = meaData.getSpecDatas(device);

        VarContext varContext = new VarContext() {
            @Override
            public Object getVarValue(String key) {
                for (SpecData specData : specDatasByDevice) {
                    if (specData.getSpec().getName().equalsIgnoreCase(key)) {
                        return specData.getValue();
                    }
                }
//                return Routine.this.getVarValue(key);
                return ipp.getVarValue(key);
            }
        };
        return doResolve(ipp.pagePattern, device.getDevicePolarity(), varContext);
    }

    protected void resolvePage(DevicePolarity devicePolarity,int cycleIndex) {

        meaAnalyses = new MeaAnalysis[indPagePatterns.size()];
        for (int i = 0; i < meaAnalyses.length; i++) {
//            EntityPagePattern pagePattern = pagePatterns.get(i);
            IndPagePattern ipp = indPagePatterns.get(i);
            if (CYCLE_TEST > 1) {
                String name = ipp.getPagePattern().getName();
                if(name.contains("_CYCLE")){
                    int index = name.indexOf("_CYCLE");
                    name = name.substring(0,index);
                }
                ipp.getPagePattern().setName(name + "_CYCLE"+cycleIndex);
            }           
            meaAnalyses[i] = doResolve(ipp.pagePattern, devicePolarity, ipp);
        }
    }
    
     protected void resolvePage(DevicePolarity devicePolarity) {
//        if (meaAnalyses != null) {
//            return;
//        }
        meaAnalyses = new MeaAnalysis[indPagePatterns.size()];
        for (int i = 0; i < meaAnalyses.length; i++) {
//            EntityPagePattern pagePattern = pagePatterns.get(i);
            IndPagePattern ipp = indPagePatterns.get(i);          
            meaAnalyses[i] = doResolve(ipp.pagePattern, devicePolarity, ipp);
        }
    }

    protected MeaAnalysis doResolve(EntityPagePattern pagePattern, DevicePolarity devicePolarity, VarContext varContext) {
//        EntityPagePattern pagePattern = ipp.pagePattern;

        MeaType meaType = MeaType.DC;
        // TODO: cv?
        MeaAnalysis analysis = new MeaAnalysis(deviceType, meaType, pagePattern.getName());
        analysis.setRoutine(this);
        analysis.setPagePattern(pagePattern);
        analysis.setCompliances(compliances);

        String[] conditionNames = pagePattern.getConditionNames();
        String xName = pagePattern.getXName();
        String pName = pagePattern.getPName();
        String ref = getRef(deviceType, conditionNames, xName, pName);
        analysis.setRefNode(ref);
        // output
        String[] yNames = pagePattern.getYNames();
        for (String yName : yNames) {
            addOutput(deviceType, analysis, yName, ref, true);
        }
        // biases
        FuncField xValuesPattern = pagePattern.getXValuesPattern();
        MeaBias xMeaBias = getMeaBias(deviceType, xName, ref);
        resolveBundles(xMeaBias, xValuesPattern, varContext, devicePolarity);
        analysis.addBias(xMeaBias);

        if (!pName.equalsIgnoreCase(EntityPageType.P_PLACE_HOLDER)) {
            FuncField pValuesPattern = pagePattern.getPValuesPattern();
            if (pName.equalsIgnoreCase("beta") || pName.equalsIgnoreCase("betar")) {
                InputType inputType = InputType.CURRENT;
                String from = "";
                String to = ref;
                boolean multiply = true;
                if (pName.equalsIgnoreCase("beta")) {
                    if (xName.equalsIgnoreCase("ib")) {
                        from = "C";
                    } else {
                        from = "B";
                        multiply = false;
                    }
                } else {
                    if (xName.equalsIgnoreCase("ib")) {
                        from = "E";
                    } else {
                        from = "B";
                        multiply = false;
                    }
                }
                MeaBias bias = new MeaBias();
                bias.setName(pName);
                bias.setType(inputType);
                bias.setFrom(from);
                bias.setTo(to);

                String[] params = pValuesPattern.getParams();
                double scale = devicePolarity.getScale();
                List<MeaBundle> meaBundles = new ArrayList<MeaBundle>();
                MeaBundleUtil.fetchBundles(params, varContext, scale, meaBundles);
                List<MeaBundle> points = new ArrayList<MeaBundle>();
                for (MeaBundle meaBundle : meaBundles) {
                    MeaBundle[] splits = meaBundle.split(1);
                    for (MeaBundle split : splits) {
                        points.add(split);
                    }
                }

                List<MeaBundle> xBundles = xMeaBias.getMeaBundles();
                // XXX
                for (Iterator<MeaBundle> it = xBundles.iterator(); it.hasNext();) {
                    MeaBundle xBundle = it.next();
                    if (!(xBundle instanceof SweepMeaBundle)) {
                        it.remove();
                    }
                }
                double[] pValues = new double[points.size()];
                for (int j = 0; j < points.size(); j++) {
                    MeaBundle point = points.get(j);
                    double ratio = point.get(0);
                    pValues[j] = ratio;
                    if (!multiply) {
                        ratio = 1 / ratio;
                    }
                    for (MeaBundle xBundle : xBundles) {
                        SyncMeaBundle syncMeaBundle = new SyncMeaBundle((SweepMeaBundle) xBundle);
                        syncMeaBundle.setRatio(ratio);
                        syncMeaBundle.setOffset(0);
                        bias.addBundle(syncMeaBundle);
                    }
                }
                analysis.setPValues(pValues);
            } else {
                xMeaBias = getMeaBias(deviceType, pName, ref);
                resolveBundles(xMeaBias, pValuesPattern, varContext, devicePolarity);
                analysis.addBias(xMeaBias);
            }
        }

        for (String condition : conditionNames) {
            if(condition.startsWith("limit")||
                   condition.equalsIgnoreCase(PagePatternUpdateUI.EXPRESSION) ){
                continue;
            }
            FuncField funcField = pagePattern.getCondition(condition);
            xMeaBias = getMeaBias(deviceType, condition, ref);
            resolveBundles(xMeaBias, funcField, varContext, devicePolarity);
            analysis.addBias(xMeaBias);
        }

        int outputNumber = analysis.getOutputNumber();
        for (int outputIndex = 0; outputIndex < outputNumber; outputIndex++) {
            MeaOutput output = analysis.getOutput(outputIndex);
            if (output.getType().isCapacitance()) {
                meaType = MeaType.CV;
                analysis.setMeaType(meaType);
                break;
            }
        }

        return analysis;
    }

    public static String getRef(DeviceType deviceType, String[] biasNames, String... extras) {
        for (String bias : biasNames) {
            bias = bias.toLowerCase();
            if (bias.startsWith("ref")) {
                bias = bias.substring(3);
                if (bias.startsWith("_")) {
                    bias = bias.substring(1);
                }
                if (bias.startsWith("v")) {
                    bias = bias.substring(1);
                }
                //bias input error
                if(bias == null||bias.isEmpty()){
                    return null;
                }
                return getRealNode(deviceType, bias.substring(0, 1));
            }
        }
        for (String bias : extras) {
            bias = bias.toLowerCase();
            if (bias.startsWith("ref")) {
                bias = bias.substring(3);
                if (bias.startsWith("_")) {
                    bias = bias.substring(1);
                }
                if (bias.startsWith("v")) {
                    bias = bias.substring(1);
                }
                if(bias == null||bias.isEmpty()){
                    return null;
                }
                return getRealNode(deviceType, bias.substring(0, 1));
            }
        }
        String ref = deviceType.getDefaultRefNode();
        if (StringUtil.isValid(ref)) {
            return ref;
        }
        return MeaAnalysis.GND;
    }

    public static MeaBias getMeaBias(DeviceType deviceType, String name, String ref) {
        MeaBias bias = new MeaBias();
        // ref_vs to refvs!!!
        bias.setName(name.replace("_", ""));

        InputType inputType = InputType.VOLTAGE;
        String from = "";
        String to = ref;

        String lowerName = name.toLowerCase();
        String trim = lowerName;
        if (trim.startsWith("ref")) {
            trim = trim.substring(3);
        }
        if (trim.startsWith("_")) {
            trim = trim.substring(1);
        }
        if (trim.startsWith("i")) {
            inputType = InputType.CURRENT;
            from = getRealNode(deviceType, trim.substring(1, 2));
            // TODO: to?
        } else if (trim.startsWith("v")) {
            inputType = InputType.VOLTAGE;
            from = getRealNode(deviceType, trim.substring(1, 2));
            if (lowerName.startsWith("ref") || from.equalsIgnoreCase(ref)) {
                to = MeaAnalysis.GND;
            } else if (trim.length() <= 2) {
                to = ref;
            } else {
                to = getRealNode(deviceType, trim.substring(2, 3));
            }
        } else if (trim.startsWith("freq")) {
            inputType = InputType.FREQUENCY;
            // TODO: from? to?
        }

        bias.setType(inputType);
        bias.setFrom(from);
        bias.setTo(to);
        return bias;
    }

    public static void addOutput(DeviceType deviceType, MeaAnalysis analysis, String yName, String ref, boolean asOutput) {
        if (analysis.hasOutput(yName)) {
            // TODO: check asOutput
            return;
        }
        yName = yName.toLowerCase();
        if (yName.contains("(")) {
            // expression
            int index = yName.indexOf("(");
            String key = yName.substring(0, index);
            String exp = LoaderUtil.getWrappedContent(yName, '(', ')');

            PEP pep = PEP.getInstance();
            pep.parseExpression(exp);
            Node node = pep.getTopNode();
            List<String> vars = new ArrayList<String>();
            PEPUtil.fetchVarNames(node, vars);

            for (String var : vars) {
                addOutput(deviceType, analysis, var, ref, false);
            }
            AdvMeaOutput advMeaOutput = new AdvMeaOutput(key);
            advMeaOutput.setExp(exp);
            advMeaOutput.setAsOutput(asOutput);
            analysis.addAdvOutput(advMeaOutput);
        } else if (yName.startsWith("i")) {
            if (yName.equalsIgnoreCase("isub")) {
                MeaOutput output = new MeaOutput(yName, getRealNode(deviceType, "b"), OutputType.CURRENT);
                output.setAsOutput(asOutput);
                analysis.addOutput(output);
            } else if (yName.equalsIgnoreCase("ijd")) {
                MeaOutput output = new MeaOutput(yName, getRealNode(deviceType, "d"), OutputType.CURRENT);
                output.setAsOutput(asOutput);
                analysis.addOutput(output);
            } else {
                // 
                MeaOutput output = new MeaOutput(yName, getRealNode(deviceType, yName.substring(1, 2)), OutputType.CURRENT);
                output.setAsOutput(asOutput);
                analysis.addOutput(output);
            }
        } else if (yName.startsWith("c")) {
            MeaOutput output = new MeaOutput(yName, getRealNode(deviceType, yName.substring(1, 2)), OutputType.CAPACITANCE);
            output.setAsOutput(asOutput);
            analysis.addOutput(output);
        } else if (yName.startsWith("v")) {
            String node1 = getRealNode(deviceType, yName.substring(1, 2));
            String node2 = ref;
            if (yName.length() > 2) {
                node2 = getRealNode(deviceType, yName.substring(2, 3));
            }

            MeaOutput output1 = new MeaOutput("v" + node1, node1, OutputType.VOLTAGE);
//            MeaOutput output2 = new MeaOutput("v" + node2, node2, OutputType.VOLTAGE);
            output1.setAsOutput(asOutput);
            analysis.addOutput(output1);
//            output2.setAsOutput(false);
//            analysis.addOutput(output2);

//            addOutput(deviceType, analysis, "v" + node1, ref, false);
//            addOutput(deviceType, analysis, "v" + node2, ref, false);
//
//            AdvMeaOutput advMeaOutput = new AdvMeaOutput(yName);
//            advMeaOutput.setExp(String.format("v%s-v%s", node1, node2));
//            advMeaOutput.setAsOutput(asOutput);
//            analysis.addAdvOutput(advMeaOutput);
        } else {
            // TODO: beta, betar, r
        }
    }

    public static String getRealNode(DeviceType deviceType, String node) {
        String[] nodes = deviceType.getNodeNames();
        if (StringUtil.contains(nodes, node)) {
            return node.toUpperCase();
        }
        return deviceType.getDefaultRefNode();
    }

    public static void resolveBundles(MeaBias bias, FuncField funcField, VarContext varContext, DevicePolarity devicePolarity) {
        String[] params = funcField.getParams();
        double scale = devicePolarity.getScale();
        List<MeaBundle> meaBundles = new ArrayList<MeaBundle>();
        MeaBundleUtil.fetchBundles(params, varContext, scale, meaBundles);
        for (MeaBundle meaBundle : meaBundles) {
            bias.addBundle(meaBundle);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public void fetchInstruments(Set<EntityInstrument> instruments, RoutineSelector routineTupleFilter) {
        for (SpecPattern specPattern : specPatterns) {
            if (!routineTupleFilter.isSelected(this, specPattern)) {
                continue;
            }
            DeviceBond deviceBond = getDeviceBond(specPattern);
            if (deviceBond != null) {
                deviceBond.fetchInstruments(instruments);
            }
        }
        for (EntityPagePattern pagePattern : pagePatterns) {
            if (!routineTupleFilter.isSelected(this, pagePattern)) {
                continue;
            }
            DeviceBond deviceBond = getDeviceBond(pagePattern);
            if (deviceBond != null) {
                deviceBond.fetchInstruments(instruments);
            }
        }
    }

    public void groupSpecByBond(Map<DeviceBond, List<SpecRequest>> specMap, MeaData meaData, RoutineSelector routineTupleFilter) {
        resolveSpec(meaData);
        for (int i = 0; i < specRequests.length; i++) {
            SpecPattern specPattern = specPatterns.get(i);

            if (!routineTupleFilter.isSelected(this, specPattern)) {
                continue;
            }

            DeviceBond deviceBond = getDeviceBond(specPattern);
            if (deviceBond != null) {
                List<SpecRequest> requests = specMap.get(deviceBond);
                if (requests == null) {
                    requests = new ArrayList<SpecRequest>();
                    specMap.put(deviceBond, requests);
                }
                requests.add(specRequests[i]);
            }
        }
    }

    public static int CYCLE_TEST = 1;
    public void groupPageByBond(Map<DeviceBond, List<MeaAnalysis>> analysisMap, DevicePolarity devicePolarity, RoutineSelector routineTupleFilter) {
        resolvePage(devicePolarity);
         //for cycle test
        for (int index = 0; index < CYCLE_TEST; index++) {
            //resolvePage(devicePolarity,index);
            for (int i = 0; i < meaAnalyses.length; i++) {
                EntityPagePattern pagePattern = pagePatterns.get(i);
                if (!routineTupleFilter.isSelected(this, pagePattern)) {
                    continue;
                }
                DeviceBond deviceBond = getDeviceBond(pagePattern);
                if (deviceBond != null) {
                    List<MeaAnalysis> requests = analysisMap.get(deviceBond);
                    if (requests == null) {
                        requests = new ArrayList<MeaAnalysis>();
                        analysisMap.put(deviceBond, requests);
                    }
                    requests.add(meaAnalyses[i]);
                }
            }
        }
    }

    public boolean verifyBond(Console console) {
        for (EntityPagePattern pagePattern : pagePatterns) {
            DeviceBond deviceBond = getDeviceBond(pagePattern);
            if (deviceBond == null) {
                console.err("No connection set for page " + pagePattern.getName());
                return false;
            }
        }
        for (SpecPattern specPattern : specPatterns) {
            DeviceBond deviceBond = getDeviceBond(specPattern);
            if (deviceBond == null) {
                console.err("No connection set for spec " + specPattern.getName());
                return false;
            }
        }
        return true;
    }

    public boolean verifyBond(Console console, RoutineSelector routineTupleFilter) {
        for (EntityPagePattern pagePattern : pagePatterns) {
            if (routineTupleFilter != null && !routineTupleFilter.isSelected(this, pagePattern)) {
                continue;
            }
            DeviceBond deviceBond = getDeviceBond(pagePattern);
            if (deviceBond == null) {
                console.err("No connection set for page " + pagePattern.getName());
                return false;
            }
        }
        for (SpecPattern specPattern : specPatterns) {
            if (routineTupleFilter != null && !routineTupleFilter.isSelected(this, specPattern)) {
                continue;
            }
            DeviceBond deviceBond = getDeviceBond(specPattern);
            if (deviceBond == null) {
                console.err("No connection set for spec " + specPattern.getName());
                return false;
            }
        }
        return true;
    }

    public boolean hasDeviceBond(EntityPagePattern pagePattern) {
        String key = pagePattern.getName().toLowerCase();
        if (bondMap.containsKey(key)) {
            return true;
        }
        RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(deviceType);
        return routinePattern.hasDeviceBond(key);
    }

    public DeviceBond getDeviceBond(EntityPagePattern pagePattern) {
        String key = pagePattern.getName().toLowerCase();
        DeviceBond deviceBond = bondMap.get(key);
        if (deviceBond != null) {
            return deviceBond;

        }
        RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(deviceType);
        return routinePattern.getDeviceBond(key);
    }

    public boolean hasDeviceBond(SpecPattern specPattern) {
        String key = specPattern.getName().toLowerCase();
        if (bondMap.containsKey(key)) {
            return true;
        }
        RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(deviceType);
        return routinePattern.hasDeviceBond(key);
    }

    public DeviceBond getDeviceBond(SpecPattern specPattern) {
        String key = specPattern.getName().toLowerCase();
        DeviceBond deviceBond = bondMap.get(key);
        if (deviceBond != null) {
            return deviceBond;
        }
        RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(deviceType);
        return routinePattern.getDeviceBond(key);
    }

    public List<IndPagePattern> getIndPagePatterns() {
        return indPagePatterns;
    }

    public List<IndSpecPattern> getIndSpecPatterns() {
        return indSpecPatterns;
    }

    public IndPagePattern getIndPagePattern(EntityPagePattern pagePattern) {
        for (IndPagePattern ipp : indPagePatterns) {
            if (ipp.pagePattern == pagePattern) {
                return ipp;
            }
        }

        return null;
    }
    
    private int getSignature(EntityPagePattern pagePattern){
        String result ;
        StringBuilder source = new StringBuilder();
        source.append(pagePattern.getName());
        if (pagePattern.getDeviceType() != null) {
            source.append(pagePattern.getDeviceType());
        }

        if (pagePattern.getPValuesPattern() != null) {
            String[] params = pagePattern.getPValuesPattern().getParams();
            for (int index = 0; index < params.length; index++) {
                source.append(params[index]);
            }
        }

        if (pagePattern.getXValuesPattern() != null) {
            String[] params = pagePattern.getXValuesPattern().getParams();
            for (int index = 0; index < params.length; index++) {
                source.append(params[index]);
            }
        }

        if (pagePattern.getYNames() != null) {
            String[] params = pagePattern.getYNames();
            for (int index = 0; index < params.length; index++) {
                source.append(params[index]);
            }
        }
        
        result = source.toString();
        return result == null ? 0 : result.hashCode();
                 
    }
    
    public IndPagePattern getIndPagePatternBySignature(EntityPagePattern pagePattern) {
        int sourceSignature = getSignature(pagePattern);
        for (IndPagePattern ipp : indPagePatterns) {
            //get signature 
            int desSignature = getSignature(ipp.pagePattern);
            if (sourceSignature == desSignature) {
                return ipp;
            }
        }

        return null;
    }

    public IndSpecPattern getIndSpecPattern(SpecPattern specPattern) {
        for (IndSpecPattern ipp : indSpecPatterns) {
            if (ipp.specPattern == specPattern) {
                return ipp;
            }
        }

        return null;
    }
    
    //get the spec pattern index from all spec patterns
    public int getSpecPatternIndex(SpecPattern specPattern){
        int result = -1;
         for(int index =0;index<specPatterns.size();index++){
            SpecPattern pattern = specPatterns.get(index);
            if(pattern.equals(specPattern)){
                result = index;
                break;
            }
        }
        
        return result;
    }

    // get the page pattern index from all the routine pattern
    public int getPagePatternIndex(EntityPagePattern pagePattern){
        int result = -1;
        for(int index =0;index<pagePatterns.size();index++){
            EntityPagePattern pattern = pagePatterns.get(index);
            if(pattern == pagePattern){
                result = index;
                break;
            }
        }
        
        return result;
        
    }
    
    public List<EntityPagePattern> getPagePatterns() {
        return pagePatterns;
    }

    public EntityPagePattern getPagePattern(String pageName) {
        for (EntityPagePattern pagePattern : pagePatterns) {
            if (pagePattern.getName().equalsIgnoreCase(pageName)) {
                return pagePattern;
            }
        }
        return null;
    }

    public List<SpecPattern> getSpecPatterns() {
        return specPatterns;
    }

    @Override
    public int getSpecPatternNumber() {
        return specPatterns.size();
    }

    @Override
    public SpecPattern getSpecPattern(int index) {
        return specPatterns.get(index);
    }

    @Override
    public SpecPattern getSpecPatternByName(String name) {
        for (SpecPattern specPattern : specPatterns) {
            if (specPattern.getName().equalsIgnoreCase(name)) {
                return specPattern;
            }
        }
        return null;
    }

    @Override
    public Object getVarValue(String key) {
        return getVar(key);
    }

    public void syncVars(VarProvider other) {
        List<String> existingVarNames = this.getVarNames();
        List<String> otherVarNames = other.getVarNames();

        for (String varName : otherVarNames) {
            if (existingVarNames.contains(varName)) {
                other.setVar(varName, this.getVar(varName));
            } else {
                this.setVar(varName, other.getVar(varName));
            }
        }
    }

    public void tearup(String dir) {
        tearupPage(dir);
        tearupSpec(dir);
    }

    public void tearupPage(String dir) {
        String path = PathUtil.getFullPath(dir, id + "_page.ini");
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public void tearupSpec(String dir) {
        String path = PathUtil.getFullPath(dir, id + "_spec.ini");
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public Element asElement(String dir, String name) throws IOException {
        Element routineElem = new Element(name);
        routineElem.setAttribute("name", name);
        routineElem.setAttribute("id", String.valueOf(id));
//        routineElem.setAttribute("type", deviceType.getName());
        for (Compliance comp : compliances) {
            Element compElem = comp.asElement("compliance");
            routineElem.addContent(compElem);
        }
        for (Map.Entry<String, DeviceBond> entry1 : bondMap.entrySet()) {
            String key = entry1.getKey();
            DeviceBond deviceBond = entry1.getValue();
            Element bondElem = new Element("bond");
            routineElem.addContent(bondElem);
            bondElem.setAttribute("key", key);
            bondElem.setAttribute("bond", deviceBond.getName());
        }

//        Element pagepatternsElem = new Element("pagepatterns");
//        routineElem.addContent(pagepatternsElem);
        for (IndPagePattern ipp : indPagePatterns) {
            Element ippElem = ipp.asElement("pagepattern");
//            pagepatternsElem.addContent(ippElem);
            routineElem.addContent(ippElem);
        }

//        Element specpatternsElem = new Element("specpatterns");
//        routineElem.addContent(specpatternsElem);
        for (IndSpecPattern ipp : indSpecPatterns) {
            Element ippElem = ipp.asElement("specpattern");
//            specpatternsElem.addContent(ippElem);
            routineElem.addContent(ippElem);
        }

        if (!pagePatterns.isEmpty()) {
            String path = PathUtil.getFullPath(dir, id + "_page.ini");
            PagePatternLoader.save(pagePatterns, path, this);
        } else {
            tearupPage(dir);
        }
        if (getSpecPatternNumber() > 0) {
            String path = PathUtil.getFullPath(dir, id + "_spec.ini");
            SpecPatternLoader.save(deviceType, this, this, path);
        } else {
            tearupSpec(dir);
        }

        return routineElem;
    }

    public void fromElement(String dir, Element routineElem) throws Exception {
//        DeviceType deviceType = MeaDeviceTypeManager.getDeviceType(routineElem.getAttributeValue("type"));
        this.setName(routineElem.getAttributeValue("name"));
        String idStr = routineElem.getAttributeValue("id");
        this.setId(Long.parseLong(idStr));

        List<Element> complianceElems = routineElem.getChildren("compliance");
        Compliance[] comps = new Compliance[complianceElems.size()];
        for (int i = 0; i < comps.length; i++) {
            Element complianceElem = complianceElems.get(i);
            comps[i] = Compliance.load(complianceElem);
        }
        // TODO:
        this.compliances = comps;

        String path = PathUtil.getFullPath(dir, idStr + "_page.ini");
        if ((new File(path).exists())) {
            PagePatternLoader loader = new PagePatternLoader();
            DeviceTag deviceTag = null;
            EntityPagePatternGroup pagePatternGroup = loader.load(path, deviceTag);
            this.pagePatterns.addAll(pagePatternGroup.getPatterns());
            this.syncVars(pagePatternGroup);
        }
        path = PathUtil.getFullPath(dir, idStr + "_spec.ini");
        if ((new File(path).exists())) {
            SpecPatternGroup patternGroup = null;
            try {
                patternGroup = SpecPatternLoader.load(deviceType, path);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
//            StatusDisplayer.getDefault().setStatusText("Failed to load spec patterns.");
            }
            if (patternGroup == null) {
                return;
            }
            this.specPatterns.addAll(patternGroup.getPatterns());
            this.syncVars(patternGroup);
        }

        Map<String, DeviceBond> bondMap = this.bondMap;
        List<Element> bondElems = routineElem.getChildren("bond");
        if (!bondElems.isEmpty()) {
            RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(deviceType);
            for (Element bondElem : bondElems) {
                String key = bondElem.getAttributeValue("key");
                String bondName = bondElem.getAttributeValue("bond");
                DeviceBond deviceBond = routinePattern.getDeviceBondByName(bondName);
                if (deviceBond != null) {
                    bondMap.put(key, deviceBond);
                }
            }
        }

        List<Element> pagepatternElems = routineElem.getChildren("pagepattern");
        if (!pagepatternElems.isEmpty() && pagepatternElems.size() == pagePatterns.size()) {
            for (int i = 0; i < pagepatternElems.size(); i++) {
                Element pagepatternElem = pagepatternElems.get(i);
                IndPagePattern ipp = new IndPagePattern();
                ipp.fromElement(pagepatternElem);
                ipp.pagePattern = pagePatterns.get(i);
                indPagePatterns.add(ipp);
            }
        }

        List<Element> specpatternElems = routineElem.getChildren("specpattern");
        if (!specpatternElems.isEmpty() && specpatternElems.size() == specPatterns.size()) {
            for (int i = 0; i < specpatternElems.size(); i++) {
                Element specpatternElem = specpatternElems.get(i);
                IndSpecPattern ipp = new IndSpecPattern();
                ipp.fromElement(specpatternElem);
                ipp.specPattern = specPatterns.get(i);
                indSpecPatterns.add(ipp);
            }
        }

    }
}
