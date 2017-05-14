/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import com.platformda.iv.GeneralPanel;
import com.platformda.utility.common.StringUtil;
import java.awt.Color;
import java.awt.Paint;
import org.openide.util.NbPreferences;

/**
 *
 * @author Junyi
 */
public class ChartOptions {

    public static final Color[] DEFAULT_COLORS = new Color[]{
        new Color(30, 30, 180), new Color(232, 0, 16), new Color(0, 108, 0), new Color(255,105,180),new Color(240, 16, 240),
        new Color(85, 0, 85), new Color(255, 200, 0), new Color(0, 255, 0), new Color(0, 255, 255), new Color(64, 64, 64),
        new Color(139,0,139), new Color(106, 90, 205), new Color(30, 144, 255),new Color(0,139,139), new Color(184,134,11),
        new Color(255,192,203),new Color(220,20,60),new Color(218,112,214),new Color(0,0,139),new Color(0,128,128)};

    static {
        // load colors
        String colorsString = NbPreferences.forModule(GeneralPanel.class).get(GeneralPanel.PROP_COLORS, "");
        String[] colorStrings = colorsString.split("\\|");
        if (colorStrings != null && colorStrings.length > 0) {
            for (int i = 0; i < colorStrings.length; i++) {
                setDefaultColor(i, getColor(colorStrings[i]));
            }
        }
    }

    public static Color getColor(String colorString) {
        Color color = null;
        if (colorString.equalsIgnoreCase("black")) {
            color = Color.black;
        } else if (colorString.equalsIgnoreCase("white")) {
            color = Color.white;
        } else if (colorString.equalsIgnoreCase("lightGray")) {
            color = Color.lightGray;
        } else if (colorString.equalsIgnoreCase("gray")) {
            color = Color.gray;
        } else if (colorString.equalsIgnoreCase("darkGray")) {
            color = Color.darkGray;
        } else if (colorString.equalsIgnoreCase("red")) {
            color = Color.red;
        } else if (colorString.equalsIgnoreCase("green")) {
            color = Color.green;
        } else if (colorString.equalsIgnoreCase("blue")) {
            color = Color.blue;
        } else if (colorString.equalsIgnoreCase("yellow")) {
            color = Color.yellow;
        } else if (colorString.equalsIgnoreCase("orange")) {
            color = Color.orange;
        } else if (colorString.equalsIgnoreCase("pink")) {
            color = Color.pink;
        } else if (colorString.equalsIgnoreCase("magenta")) {
            color = Color.magenta;
        } else if (colorString.equalsIgnoreCase("cyan")) {
            color = Color.cyan;
        } else {
            if (!colorString.startsWith("#") && !colorString.startsWith("0X") && !colorString.startsWith("0x")) {
                colorString = "#" + colorString;
            }
            try {
                color = Color.decode(colorString);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }
        return color;
    }

    public static void setDefaultColor(int index, Color color) {
        if (index >= 0 && index < DEFAULT_COLORS.length && color != null) {
            DEFAULT_COLORS[index] = color;
        }
    }

    public static void storeColors() {
        String[] colorStrings = new String[DEFAULT_COLORS.length];
        for (int i = 0; i < colorStrings.length; i++) {
            // encode color
            colorStrings[i] = Integer.toHexString(DEFAULT_COLORS[i].getRGB()).substring(2).toString();
        }
        String colorsString = StringUtil.concatenate(colorStrings, "|");
        NbPreferences.forModule(GeneralPanel.class).put(GeneralPanel.PROP_COLORS, colorsString);
    }

    public static Color getDefaultColor(int index) {
//        return ChartUtil.getDefaultColor(index);
        return DEFAULT_COLORS[index % DEFAULT_COLORS.length];
    }

    public static Paint getDefaultPaint(int index) {
//        return ChartUtil.getDefaultPaint(index);
        return DEFAULT_COLORS[index % DEFAULT_COLORS.length];
    }
}
