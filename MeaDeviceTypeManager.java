/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.DevicePolarity;
import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceTagImpl;
import com.platformda.datacore.DeviceType;
import com.platformda.iv.datacore.DeviceTypeManager;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author Junyi
 */
public class MeaDeviceTypeManager {

    private static MeaDeviceTypeManager instance;

    public static MeaDeviceTypeManager getInstance() {
        if (instance == null) {
            instance = new MeaDeviceTypeManager();
        }
        return instance;
    }

    private MeaDeviceTypeManager() {
    }
    //
//    List<DeviceType> deviceTypes = new ArrayList<DeviceType>();
//

    // TODO:
    public List<DeviceType> getAllDeviceTypes() {
        List<DeviceType> deviceTypes = DeviceTypeManager.getAllDeviceTypes();
        List<DeviceType> all = new ArrayList<>();
        for (DeviceType deviceType : deviceTypes) {
            if (!deviceType.getName().equalsIgnoreCase("custom")
                &&!deviceType.getName().equalsIgnoreCase("mossoi")
                ) {//&&!deviceType.getName().equalsIgnoreCase("capacitor")
                all.add(deviceType);
            }
        }
        return all;
    }
    private static List<DeviceTag> allDeviceTags;

    public static List<DeviceTag> getAllDeviceTags() {
        if (allDeviceTags == null) {
            allDeviceTags = new ArrayList<>();
            List<DeviceType> deviceTypes = DeviceTypeManager.getAllDeviceTypes();
            for (DeviceType deviceType : deviceTypes) {
                if (!deviceType.getName().equalsIgnoreCase("custom")
                    &&!deviceType.getName().equalsIgnoreCase("mossoi")
                    ) {//&&!deviceType.getName().equalsIgnoreCase("capacitor")
                    DevicePolarity[] polarities = deviceType.getPolarities();
                    for (DevicePolarity devicePolarity : polarities) {

                        DeviceTagImpl deviceTag = new DeviceTagImpl(deviceType, devicePolarity);
                        allDeviceTags.add(deviceTag);
                    }
                }
            }
        }
        return allDeviceTags;
    }

    public static JComboBox<DeviceTag> getDeviceTagCombo(DeviceTag deviceTag) {
        List<DeviceTag> deviceTags = MeaDeviceTypeManager.getAllDeviceTags();
        DefaultComboBoxModel<DeviceTag> deviceTagComboModel = new DefaultComboBoxModel<>(deviceTags.toArray(new DeviceTag[0]));
        JComboBox<DeviceTag> deviceCombo = new JComboBox<>(deviceTagComboModel);

        return deviceCombo;
    }

    public static DeviceType getDeviceType(String deviceTypeStr) {
        for (DeviceType deviceType : DeviceTypeManager.getAllDeviceTypes()) {
            if (deviceType.getName().equalsIgnoreCase(deviceTypeStr)) {
                return deviceType;
            }
        }
//        throw new Exception("Could not recognize device type : " + deviceTypeStr);
        return null;
    }

    public static DevicePolarity getDevicePolarity(String polar) {
        for (DeviceType deviceType : DeviceTypeManager.getAllDeviceTypes()) {
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
}
