/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceType;
import com.platformda.iv.datacore.DeviceTypeManager;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.datacore.pattern.EntityPagePatternGroup;
import com.platformda.iv.datacore.pattern.PagePatternLoader;
import com.platformda.iv.api.MeterProfile;
import com.platformda.iv.api.NodeBond;
import com.platformda.iv.api.Unit;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.spec.SpecPatternGroup;
import com.platformda.iv.spec.SpecPatternLoader;
import com.platformda.utility.common.FileUtil;
import com.platformda.utility.common.PathUtil;
import com.platformda.utility.common.XMLUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Junyi
 */
public class RoutinePatternManager {

    public static final String CODE_BASE = "com.platformda.iv";
    private static RoutinePatternManager instance;

    public static RoutinePatternManager getInstance() {
        if (instance == null) {
            instance = new RoutinePatternManager();
        }
        return instance;
    }
    List<RoutinePattern> patterns = new ArrayList();

    private RoutinePatternManager() {
        MeaDeviceTypeManager deviceTypeManager = MeaDeviceTypeManager.getInstance();
        List<DeviceType> deviceTypes = deviceTypeManager.getAllDeviceTypes();
        for (DeviceType deviceType : deviceTypes) {
            RoutinePattern pattern = new RoutinePattern(deviceType);
            pattern.setDeviceType(deviceType);
            patterns.add(pattern);
        }

        // load default
        //        String path = "etc/pagepattern.ini";
        //        File file = InstalledFileLocator.getDefault().locate(path, CODE_BASE, false);
        //        if (file != null) {
        //            loadPagePattern(file.getAbsolutePath());
        //        }
        //        for (DeviceType deviceType : deviceTypes) {
        //            path = "etc/spec/" + deviceType.getName() + "/Spec.ini";
        //            file = InstalledFileLocator.getDefault().locate(path, CODE_BASE, false);
        //            if (file != null) {
        //                loadSpecPattern(deviceType, file.getAbsolutePath());
        //            }
        //        }

        String configDir = PathUtil.getConfigPath();
        File file = new File(PathUtil.getFullPath(configDir, FILE_ROUTINE_PATTERN));
        if (!file.exists()) {
            // copy default
            File etcFile = InstalledFileLocator.getDefault().locate("etc", CODE_BASE, false);
            if (etcFile != null) {
                try {
                    String etcDir = etcFile.getAbsolutePath();
                    String srcPath = PathUtil.getFullPath(etcDir, FILE_ROUTINE_PATTERN);
                    String destPath = PathUtil.getFullPath(configDir, FILE_ROUTINE_PATTERN);

                    FileUtil.copy(srcPath, destPath);
                    for (RoutinePattern rp : patterns) {
                        DeviceType deviceType = rp.getDeviceType();
                        srcPath = PathUtil.getFullPath(etcDir, deviceType.getName() + "_page.ini");
                        destPath = PathUtil.getFullPath(configDir, deviceType.getName() + "_page.ini");

                        FileUtil.copy(srcPath, destPath);
                        srcPath = PathUtil.getFullPath(etcDir, deviceType.getName() + "_spec.ini");
                        destPath = PathUtil.getFullPath(configDir, deviceType.getName() + "_spec.ini");

                        FileUtil.copy(srcPath, destPath);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        InstrumentManager instrumentManager = InstrumentManager.getInstance();
        try {
            load(configDir);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public RoutinePattern getRoutinePattern(DeviceType deviceType) {
        for (RoutinePattern patternBondManagerByDeviceType : patterns) {
            if (patternBondManagerByDeviceType.getDeviceType() == deviceType) {
                return patternBondManagerByDeviceType;
            }
        }
        return null;
    }
    DeviceTag deviceTag = DeviceTypeManager.getDeviceTagByAbbreviation("nmosfet");

    public void loadPagePattern(String path) {
        EntityPagePatternGroup patternGroup = null;
        try {
            PagePatternLoader loader = new PagePatternLoader();
            patternGroup = loader.load(path, deviceTag);

            for (RoutinePattern routinePattern : patterns) {
                routinePattern.loadPagePattern(patternGroup);
            }
        } catch (Exception ex) {
        }
    }

    public void loadSpecPattern(DeviceType deviceType, String path) {
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

        for (RoutinePattern routinePattern : patterns) {
            routinePattern.loadSpecPattern(patternGroup);
        }
    }
    public static String FILE_ROUTINE_PATTERN = "routinepattern.xml";

    public void save(String dir) throws IOException {
        Element root = new Element("pattern");
        for (RoutinePattern rp : patterns) {
            DeviceType deviceType = rp.getDeviceType();
            List<EntityPagePattern> pagePatterns = rp.pagePatterns;

            String path = PathUtil.getFullPath(dir, deviceType.getName() + "_page.ini");
            PagePatternLoader.save(pagePatterns, path, rp);
            path = PathUtil.getFullPath(dir, deviceType.getName() + "_spec.ini");
            SpecPatternLoader.save(deviceType, rp, rp, path);

            Element typeElem = new Element("device");
            typeElem.setAttribute("type", deviceType.getName());
            root.addContent(typeElem);
            // TODO: List<DeviceBond> deviceBonds
            List<DeviceBond> deviceBonds = rp.deviceBonds;
            Map<String, DeviceBond> bondMap = rp.bondMap;

            DeviceBond defaultBond = rp.getDefaultBond();
            for (DeviceBond deviceBond : deviceBonds) {
                Element bondElem = new Element("bond");
                typeElem.addContent(bondElem);
                bondElem.setAttribute("name", deviceBond.getName());
                if (deviceBond == defaultBond) {
                    bondElem.setAttribute("asdefault", "true");
                }
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<String, DeviceBond> entry : bondMap.entrySet()) {
                    String string = entry.getKey();
                    DeviceBond db = entry.getValue();
                    if (db == deviceBond) {
                        if (builder.length() > 0) {
                            builder.append("|");
                        }
                        builder.append(string);
                    }
                }
                bondElem.setAttribute("apply", builder.toString());

                NodeBond[] nodeBonds = deviceBond.getBonds();
                for (NodeBond nodeBond : nodeBonds) {
                    Element nodeElem = new Element("node");
                    bondElem.addContent(nodeElem);
                    nodeElem.setAttribute("nodename", nodeBond.getNodeName());
                    Unit unit = nodeBond.getUnit();
                    if (unit != null) {
                        nodeElem.setAttribute("instname", nodeBond.getInstName());
                        nodeElem.setAttribute("term", unit.getName());
                    }
                }
            }
        }

        String xmlPath = PathUtil.getFullPath(dir, FILE_ROUTINE_PATTERN);
        Format fmt = Format.getPrettyFormat();
        fmt.setIndent("  ");
        fmt.setEncoding("gb2312");
        XMLOutputter outputtter = new XMLOutputter(fmt);
        try {
            FileWriter writer = new FileWriter(xmlPath);
            outputtter.output(root, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
        }
    }

    public void load(String dir) throws Exception {
        File file = new File(PathUtil.getFullPath(dir, FILE_ROUTINE_PATTERN));
        if (!file.exists()) {
            return;
        }
        for (RoutinePattern rp : patterns) {
            DeviceType deviceType = rp.getDeviceType();
            String path = PathUtil.getFullPath(dir, deviceType.getName() + "_page.ini");
            if ((new File(path).exists())) {
//                PagePatternLoader loader = new PagePatternLoader();
//                EntityPagePatternGroup pagePatternGroup = loader.load(path, deviceTag);
//                rp.pagePatterns.addAll(pagePatternGroup.getPatterns());
                loadPagePattern(path);
            }
            path = PathUtil.getFullPath(dir, deviceType.getName() + "_spec.ini");
            if ((new File(path).exists())) {
                loadSpecPattern(deviceType, path);
            }
        }

        InstrumentManager instrumentManager = InstrumentManager.getInstance();

        Element root = XMLUtil.getRoot(PathUtil.getFullPath(dir, FILE_ROUTINE_PATTERN));
        List<Element> deviceElems = root.getChildren("device");
        for (Element deviceElem : deviceElems) {
            String typeName = deviceElem.getAttributeValue("type");
            DeviceType deviceType = MeaDeviceTypeManager.getDeviceType(typeName);
            RoutinePattern rp = getRoutinePattern(deviceType);
            List<DeviceBond> deviceBonds = rp.deviceBonds;
            Map<String, DeviceBond> bondMap = rp.bondMap;

            List<Element> bondElems = deviceElem.getChildren("bond");
            for (Element bondElem : bondElems) {
                DeviceBond deviceBond = new DeviceBond(bondElem.getAttributeValue("name"));
                deviceBond.setDeviceType(deviceType);
                deviceBonds.add(deviceBond);

                String def = bondElem.getAttributeValue("asdefault");
                if (def != null && (def.equalsIgnoreCase("1") || def.equalsIgnoreCase("true"))) {
                    rp.setDefaultBond(deviceBond);
                }

                String[] applies = bondElem.getAttributeValue("apply").split("\\|");
                for (String apply : applies) {
                    if (!apply.isEmpty()) {
                        bondMap.put(apply, deviceBond);
                    }
                }

                List<Element> nodeElems = bondElem.getChildren("node");
                NodeBond[] bonds = new NodeBond[nodeElems.size()];
                deviceBond.setBonds(bonds);

                for (int i = 0; i < nodeElems.size(); i++) {
                    Element nodeElem = nodeElems.get(i);
                    String node = nodeElem.getAttributeValue("nodename");

                    String instName = nodeElem.getAttributeValue("instname");
                    Unit term = null;
                    if (instName != null) {
                        String termName = nodeElem.getAttributeValue("term");
                        EntityInstrument inst = instrumentManager.getInstrument(instName);
                        if (inst == null) {
                            instName = null;
                        } else {
                            MeterProfile meterConfig = (MeterProfile) inst.getProfile();
                            int termNumber = meterConfig.getUnitNumber();
                            for (int termIndex = 0; termIndex < termNumber; termIndex++) {
                                Unit t = meterConfig.getUnit(termIndex);
                                if (t.getName().equalsIgnoreCase(termName)) {
                                    term = t;
                                    break;
                                }
                            }
                        }
                    }

                    NodeBond nodeBond = new NodeBond(instName, term, node);
                    bonds[i] = nodeBond;
                }
            }
        }
    }
}
