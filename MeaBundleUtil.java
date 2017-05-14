/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.ep.EvaluatorVisitor;
import com.platformda.ep.PEP;
import com.platformda.ep.VarContext;
import com.platformda.iv.analysis.ArrayMeaBundle;
import com.platformda.iv.analysis.MeaBundle;
import com.platformda.iv.analysis.SweepMeaBundle;
import com.platformda.utility.common.MathUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author junyi
 */
public class MeaBundleUtil {

    public static void fetchBundles(String[] parts, VarContext varContext, double scale, List<MeaBundle> meaBundles) {
        List<Double> values = new ArrayList<Double>();
        for (String part : parts) {
            if (!getFuncValues(part, values, meaBundles, varContext, scale)) {
//                double value = getValue(part, varContext, scale);
//                values.add(value);
                Object obj = getObjectValue(part, varContext, scale);
                if (obj instanceof Number) {
                    values.add(((Number) obj).doubleValue());
                } else if (obj instanceof List) {
                    List list = (List) obj;
                    for (Object object : list) {
                        if (object instanceof Number) {
                            values.add(((Number) object).doubleValue());
                        }
                    }
                }
            }
        }
        if (!values.isEmpty()) {
            Collections.sort(values);
            double[] valuesInArray = new double[values.size()];
            for (int i = 0; i < valuesInArray.length; i++) {
                valuesInArray[i] = values.get(i);
            }
            ArrayMeaBundle arrayMeaBundle = new ArrayMeaBundle(valuesInArray);
            meaBundles.add(arrayMeaBundle);
        }
    }

    public static double getValue(String valueStr, VarContext varContext, double scale) {
        valueStr = valueStr.trim();
        double value = Double.NaN;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            Object obj = varContext.getVarValue(valueStr);
            if (obj == null) {
                PEP pep = PEP.getInstance();
                pep.parseExpression(valueStr);
                EvaluatorVisitor ev = new EvaluatorVisitor();
                try {
                    obj = ev.getValue(pep.getTopNode(), null, varContext, scale);
                    // TODO: obj instanceof List


                } catch (Exception ex) {
                }
            }
            if (obj instanceof Number) {
                value = ((Number) obj).doubleValue();
            }
        }
        return value;
    }

    static Object getObjectValue(String valueStr, VarContext varContext, double scale) {
        valueStr = valueStr.trim();
        Object obj = null;
        try {
            double value = Double.NaN;
            value = Double.parseDouble(valueStr);
            obj = value;
        } catch (NumberFormatException e) {
            obj = varContext.getVarValue(valueStr);
            if (obj == null) {
                PEP pep = PEP.getInstance();
                pep.parseExpression(valueStr);
                EvaluatorVisitor ev = new EvaluatorVisitor();
                try {
                    obj = ev.getValue(pep.getTopNode(), null, varContext, scale);
                    // TODO: obj instanceof List
                } catch (Exception ex) {
                }
            }
//            if (obj instanceof Number) {
//                value = ((Number) obj).doubleValue();
//            }
        }
        return obj;
    }

    static boolean getFuncValues(String str, List<Double> values, List<MeaBundle> meaBundles, VarContext varContext, double scale) {
        int index = str.indexOf("(");
        if (index >= 0) {
            String funcName = str.substring(0, index);
            String content = str.substring(index + 1);
            if (content.endsWith(")")) {
                content = content.substring(0, content.length() - 1);
            }
            String[] params = content.split(",");
            if (funcName.equalsIgnoreCase("poivector") || funcName.equalsIgnoreCase("poisweep") || funcName.equalsIgnoreCase("range")) {
                for (int i = 0; i < params.length; i++) {
                    Object obj = getObjectValue(params[i], varContext, scale);
                    if (obj instanceof Number) {
                        values.add(((Number) obj).doubleValue());
                    } else if (obj instanceof List) {
                        List list = (List) obj;
                        for (Object object : list) {
                            if (object instanceof Number) {
                                values.add(((Number) object).doubleValue());
                            }
                        }
                    }
                }
            } else {
                double[] paramValues = new double[params.length];
                for (int i = 0; i < paramValues.length; i++) {
                    paramValues[i] = getValue(params[i], varContext, scale);
                }
                if (funcName.equalsIgnoreCase("linvector") || funcName.equalsIgnoreCase("linsweep") || funcName.equalsIgnoreCase("steprange")) {
                    if (paramValues.length == 3) {
//                        double[] vs = MathUtil.getSweepValues(paramValues[0], paramValues[1], paramValues[2]);
//                        int point = vs.length;
                        int point = (int) Math.round((paramValues[1] - paramValues[0]) / paramValues[2]) + 1;
                        SweepMeaBundle sweepMeaBundle = new SweepMeaBundle(paramValues[0], paramValues[1], paramValues[2], point);
                        meaBundles.add(sweepMeaBundle);
                    }
                } else if (funcName.equalsIgnoreCase("linrange")) {
                    if (paramValues.length == 3) {
                        // !!! FIX STEP BUG, devided by point-1, not point!!!
                        int point = (int) paramValues[2];
                        if (point == 1) {
                            SweepMeaBundle sweepMeaBundle = new SweepMeaBundle(paramValues[0], paramValues[1], 0, point);
                            meaBundles.add(sweepMeaBundle);
                        } else {
                            double step = (paramValues[1] - paramValues[0]) / (point - 1);
                            SweepMeaBundle sweepMeaBundle = new SweepMeaBundle(paramValues[0], paramValues[1], step, point);
                            meaBundles.add(sweepMeaBundle);
                        }
                    }
                } else if (funcName.equalsIgnoreCase("logvector") || funcName.equalsIgnoreCase("logsweep") || funcName.equalsIgnoreCase("logrange")) {
                    if (paramValues.length == 3) {
                        double step = (paramValues[1] - paramValues[0]) / paramValues[2];
                        SweepMeaBundle sweepMeaBundle = new SweepMeaBundle(paramValues[0], paramValues[1], step, (int) paramValues[2]);
                        sweepMeaBundle.setType(SweepMeaBundle.TYPE_LOG10);
                        meaBundles.add(sweepMeaBundle);
                    }
                } else if (funcName.equalsIgnoreCase("perdecade") || funcName.equalsIgnoreCase("perdecaderange")) {
                    if (paramValues.length == 3) {
                        values.addAll(MathUtil.getPerDecadeValuesAsList(paramValues[0], paramValues[1], (int) paramValues[2]));
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
