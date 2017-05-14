/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.datacore;

/**
 *
 * @author renjing
 */
import com.platformda.datacore.*;
import com.platformda.iv.MeaSpace;
import com.platformda.utility.common.PathUtil;
import com.platformda.utility.common.StringUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author junyi
 */
public class DeviceTypeManager {

    public static String DEVICE_FILE_NAME = "device.ini";
    public static final String FILE_DEVICE_OPTIONS = "deviceOptions.ini";
    static DeviceOptions deviceOptions;
    final static CompositeConfiguration config;
    
    public static final String MOSFET = "Mosfet";
    public static final String MOSSOI = "MosSOI";
    public static final String BJT = "BJT";
    public static final String DIODE = "Diode";
    public static final String RESISTOR = "Resistor";
    public static final String CAPACITOR = "Capacitor";
    

    private static void loadDeviceTypes() {
        File file = new File(PathUtil.getFullPath(PathUtil.getConfigPath(), DEVICE_FILE_NAME));
        if (file.exists()) {
        } else {
            File defaultFile = InstalledFileLocator.getDefault().locate("etc/device.ini", MeaSpace.CODE_BASE, false);
            if (defaultFile != null) {
                file = defaultFile;
            } else {
                List<DeviceType> types = DeviceTypeManager.getAllDeviceTypes();
                for (DeviceType deviceType : types) {
                    if (!deviceType.getName().equalsIgnoreCase("custom")) {
                        deviceTypes.add(deviceType);
                    }
                }
                return;
            }
        }
        try {
            HierarchicalINIConfiguration configuration = new HierarchicalINIConfiguration(file);
            Set<String> allKeys = configuration.getSections();
            for (String key : allKeys) {
                int id = configuration.getInt(key + ".id");
                String[] nodes = configuration.getStringArray(key + ".node");
                String ref = configuration.getString(key + ".ref");
                String[] instances = configuration.getStringArray(key + ".instance");
                String[] dao = configuration.getStringArray(key + ".dao");
                for (int i = 0; i < dao.length; i++) {
                    if (dao[i].equalsIgnoreCase("null")) {
                        dao[i] = null;
                    }
                }           
              
                DevicePolarity[] dpsInArray = null;
                if(key.equalsIgnoreCase(MOSFET)
                        ||key.equalsIgnoreCase(MOSSOI)){
                   dpsInArray = new DevicePolarity[]{DevicePolarity.NMOS,DevicePolarity.PMOS};
                } else if(key.equalsIgnoreCase(BJT)){
                   dpsInArray = new DevicePolarity[]{DevicePolarity.NPN,DevicePolarity.PNP};
                } else if (key.equalsIgnoreCase(DIODE)){
                   dpsInArray = new DevicePolarity[]{DevicePolarity.DIODE};
                } else if (key.equalsIgnoreCase(RESISTOR)){
                   dpsInArray = new DevicePolarity[]{DevicePolarity.RESISTOR};                   
                } else if (key.equalsIgnoreCase(CAPACITOR)){
                   dpsInArray = new DevicePolarity[]{DevicePolarity.CAPACITOR};
                   
                }
              

                DeviceTypeImpl deviceTypeImpl = new DeviceTypeImpl(key, id, dpsInArray, instances, dao, nodes, StringUtil.indexOf(ref, nodes));
                deviceTypes.add(deviceTypeImpl);
            }
        } catch (ConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
    static CompositeConfiguration loadConfiguration(String fileName) {
        CompositeConfiguration config = new CompositeConfiguration();
        // user 
        File userFile = new File(PathUtil.getFullPath(PathUtil.getConfigPath(), fileName));
        if (userFile.exists()) {
            try {
                config.addConfiguration(new HierarchicalINIConfiguration(userFile));
            } catch (ConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // default
        String path = "etc/options/" + fileName;
        File file = InstalledFileLocator.getDefault().locate(path, "com.platformda.datacore", false);
        if (file.exists()) {
            try {
                config.addConfiguration(new HierarchicalINIConfiguration(file));
            } catch (ConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return config;
    }

    static {
        config = loadConfiguration(FILE_DEVICE_OPTIONS);
        deviceOptions = new DeviceOptionsImpl(config);
        //load device types
        deviceTypes = new ArrayList<>();
        loadDeviceTypes();
    }

    public static void addConfiguration(Configuration configuration) {
        int number = config.getNumberOfConfigurations();
        Configuration[] subs = new Configuration[number];
        for (int i = 0; i < subs.length; i++) {
            subs[i] = config.getConfiguration(i);
        }
        for (Configuration sub : subs) {
            config.removeConfiguration(sub);
        }
        config.addConfiguration(configuration);
        for (Configuration sub : subs) {
            config.addConfiguration(sub);
        }
    }

    public static void removeConfiguration(Configuration configuration) {
        config.removeConfiguration(configuration);
    }
    //private static Lookup.Result<DeviceType> deviceTypes = Lookup.getDefault().lookupResult(DeviceType.class);
    private static List<DeviceType> deviceTypes;
    private static List<DeviceTag> allDeviceTags;

    public static List<DeviceTag> getAllDeviceTags() {
        if (allDeviceTags == null) {
            allDeviceTags = new ArrayList<DeviceTag>();
            for (DeviceType deviceType : deviceTypes) {//.allInstances()
                DevicePolarity[] polarities = deviceType.getPolarities();
                for (DevicePolarity devicePolarity : polarities) {
                    DeviceTagImpl deviceTag = new DeviceTagImpl(deviceType, devicePolarity);
                    allDeviceTags.add(deviceTag);
                }
            }
        }
        return allDeviceTags;
    }

    public static List<DeviceType> getAllDeviceTypes() {
        List<DeviceType> all = new ArrayList<DeviceType>();
        all.addAll(deviceTypes);//.allInstances()
        return all;
    }

    public static List<DevicePolarity> getAllPolars() {
        List<DevicePolarity> allPolars = new ArrayList<DevicePolarity>();
        for (DeviceType deviceType : deviceTypes) {//.allInstances()
            DevicePolarity[] polars = deviceType.getPolarities();
            for (int i = 0; i < polars.length; i++) {
                DevicePolarity devicePolarity = polars[i];
                allPolars.add(devicePolarity);
            }
        }
        return allPolars;
    }

    public static DeviceType getDeviceType(String deviceTypeStr) {
        for (DeviceType deviceType : deviceTypes) {//.allInstances()
            if (deviceType.getName().equalsIgnoreCase(deviceTypeStr)) {
                return deviceType;
            }
        }
//        throw new Exception("Could not recognize device type : " + deviceTypeStr);
        return null;
    }

    public static DeviceType getDeviceTypeById(int typeId) {
        for (DeviceType deviceType : deviceTypes) {//.allInstances()
            if (deviceType.getDeviceTypeId() == typeId) {
                return deviceType;
            }
        }
//        throw new Exception("Could not recognize device type : " + deviceTypeStr);
        return null;
    }

    public static DevicePolarity getDevicePolarity(String polar) {
        for (DeviceType deviceType : deviceTypes) {//.allInstances()
            DevicePolarity[] polars = deviceType.getPolarities();
            for (int i = 0; i < polars.length; i++) {
                DevicePolarity devicePolarity = polars[i];
                if (devicePolarity.aliasAccept(polar)) {
                    return devicePolarity;
                }
            }
        }
//        throw new Exception("Could not recognize device polarity: " + polar);
        return null;
    }

    public static DevicePolarity getDevicePolarityById(int polarId) {
        for (DeviceType deviceType : deviceTypes) {//.allInstances()
            DevicePolarity[] polars = deviceType.getPolarities();
            for (int i = 0; i < polars.length; i++) {
                DevicePolarity devicePolarity = polars[i];
                if (devicePolarity.getDevicePolarityId() == polarId) {
                    return devicePolarity;
                }
            }
        }
//        throw new Exception("Could not recognize device polarity: " + polar);
        return null;
    }
//
//    public static DeviceType getDeviceTypeByPolar(String polarStr) {
//        for (DeviceType deviceType : deviceTypes.allInstances()) {
//            DevicePolarity[] polars = deviceType.getPolarities();
//            for (int i = 0; i < polars.length; i++) {
//                DevicePolarity devicePolarity = polars[i];
//                if (devicePolarity.aliasAccept(polarStr)) {
//                    return deviceType;
//                }
//            }
//        }
////        throw new Exception("Could not recognize device type by polarity: " + polar);
//        return null;
//    }
//    public static DeviceType getDeviceType(DevicePolarity polarity) {
//        for (DeviceType deviceType : deviceTypes.allInstances()) {
//            DevicePolarity[] polars = deviceType.getPolarities();
//            for (int i = 0; i < polars.length; i++) {
//                DevicePolarity devicePolarity = polars[i];
//                if (devicePolarity.equals(polarity)) {
//                    return deviceType;
//                }
//            }
//        }
////        throw new Exception("Could not recognize device type by polarity: " + polarity.getName());
//        return null;
//    }

    public static String toString(DeviceType deviceType, DevicePolarity devicePolarity) {
        String type = deviceType.getName();
        String polarity = devicePolarity.getName();
        if (type.equalsIgnoreCase(polarity)) {
            return type;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append(" - ");
            sb.append(polarity);

            return sb.toString();
        }
    }

    public static String toString(DeviceTag deviceTag) {
        String type = deviceTag.getDeviceType().getName();
        String polarity = deviceTag.getDevicePolarity().getName();
        if (type.equalsIgnoreCase(polarity)) {
            return type;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(type);
            sb.append(" - ");
            sb.append(polarity);

            return sb.toString();
        }
    }

    public static DeviceTag getDeviceTagInstance(DeviceTag deviceTag) {
        DeviceTagImpl instance = new DeviceTagImpl(deviceTag.getDeviceType(), deviceTag.getDevicePolarity());
        return instance;
    }

    public static boolean matches(DeviceTag deviceTag0, DeviceTag deviceTag1) {
        if (deviceTag0.getDeviceType() != deviceTag1.getDeviceType() && (deviceTag0.getDeviceType() == null || !deviceTag0.getDeviceType().equals(deviceTag1.getDeviceType()))) {
            return false;
        }
        if (deviceTag0.getDevicePolarity() != deviceTag1.getDevicePolarity() && (deviceTag0.getDevicePolarity() == null || !deviceTag0.getDevicePolarity().equals(deviceTag1.getDevicePolarity()))) {
            return false;
        }
        return true;
    }

    public static void sort(List<DeviceTag> deviceTags) {
        final List<DeviceType> allDeviceTypes = new ArrayList<DeviceType>(deviceTypes);//.allInstances()
        Collections.sort(deviceTags, new Comparator<DeviceTag>() {
            @Override
            public int compare(DeviceTag dt1, DeviceTag dt2) {
                DeviceType type1 = dt1.getDeviceType();
                DeviceType type2 = dt2.getDeviceType();

                int index1 = allDeviceTypes.indexOf(type1);
                int index2 = allDeviceTypes.indexOf(type2);

                if (index1 < index2) {
                    return -1;
                } else if (index1 > index2) {
                    return 1;
                }
                return 0;
            }
        });
    }

    public static String getUnit(String instanceName) {
        return deviceOptions.getUnit(null, instanceName);
    }

    public static double getScale(String instanceName) {
        return deviceOptions.getScale(null, instanceName);
    }

    public static EntityDevice getDefaultDevice(DeviceTag deviceTag) {
        EntityDevice device = new EntityDevice();

        DeviceType deviceType = deviceTag.getDeviceType();
        DevicePolarity devicePolarity = deviceTag.getDevicePolarity();
        device.setDeviceType(deviceType);
        device.setDevicePolarity(devicePolarity);
        String[] instances = deviceType.getPrimaryInstanceNames();
        for (String string : instances) {
            if (string.startsWith("t") || string.startsWith("T")) {
                device.setInstance(string, 25);
            } else {
                device.setInstance(string, 10);
            }
        }

        return device;
    }
    public final static DeviceTagAbbreviation[] abbreviations = new DeviceTagAbbreviation[]{
        new DeviceTagAbbreviation("nmosfet",
        DeviceTypeManager.getDeviceType("Mosfet"), DeviceTypeManager.getDevicePolarity("nmos")),
        new DeviceTagAbbreviation("pmosfet",
        DeviceTypeManager.getDeviceType("Mosfet"), DeviceTypeManager.getDevicePolarity("pmos")),
        new DeviceTagAbbreviation("mosfet",
        DeviceTypeManager.getDeviceType("Mosfet"), DeviceTypeManager.getDevicePolarity("nmos")),
        new DeviceTagAbbreviation("nmossoi",
        DeviceTypeManager.getDeviceType("MosSOI"), DeviceTypeManager.getDevicePolarity("nmos")),
        new DeviceTagAbbreviation("pmossoi",
        DeviceTypeManager.getDeviceType("MosSOI"), DeviceTypeManager.getDevicePolarity("pmos")),
        new DeviceTagAbbreviation("mossoi",
        DeviceTypeManager.getDeviceType("Mosfet"), DeviceTypeManager.getDevicePolarity("nmos")),
        new DeviceTagAbbreviation("npn",
        DeviceTypeManager.getDeviceType("BJT"), DeviceTypeManager.getDevicePolarity("NPN")),
        new DeviceTagAbbreviation("pnp",
        DeviceTypeManager.getDeviceType("BJT"), DeviceTypeManager.getDevicePolarity("PNP")),
        new DeviceTagAbbreviation("bjt",
        DeviceTypeManager.getDeviceType("BJT"), DeviceTypeManager.getDevicePolarity("NPN")),
        new DeviceTagAbbreviation("d",
        DeviceTypeManager.getDeviceType("Diode"), DeviceTypeManager.getDevicePolarity("DIODE")),
        new DeviceTagAbbreviation("diode",
        DeviceTypeManager.getDeviceType("Diode"), DeviceTypeManager.getDevicePolarity("DIODE")),
        new DeviceTagAbbreviation("c",
        DeviceTypeManager.getDeviceType("Capacitor"), DeviceTypeManager.getDevicePolarity("CAPACITOR")),
        new DeviceTagAbbreviation("capacitor",
        DeviceTypeManager.getDeviceType("Capacitor"), DeviceTypeManager.getDevicePolarity("CAPACITOR")),
        new DeviceTagAbbreviation("r",
        DeviceTypeManager.getDeviceType("Resistor"), DeviceTypeManager.getDevicePolarity("RESISTOR")),       
        new DeviceTagAbbreviation("resistor",
        DeviceTypeManager.getDeviceType("Resistor"), DeviceTypeManager.getDevicePolarity("RESISTOR")),
        new DeviceTagAbbreviation("custom",
        DeviceTypeManager.getDeviceType("Custom"), DeviceTypeManager.getDevicePolarity("CUSTOM")),};

    public static DeviceTag getDeviceTagByAbbreviation(String abbreviation) {
        for (DeviceTagAbbreviation dta : abbreviations) {
            if (dta.abbreviation.equalsIgnoreCase(abbreviation)) {
                return dta.deviceTag;
            }
        }
        return null;
    }

    static class DeviceTagAbbreviation {

        String abbreviation;
        DeviceTag deviceTag;

        public DeviceTagAbbreviation(String abbreviation, DeviceType deviceType, DevicePolarity devicePolarity) {
            this.abbreviation = abbreviation;
            deviceTag = new DeviceTagImpl(deviceType, devicePolarity);
        }
    }
}
