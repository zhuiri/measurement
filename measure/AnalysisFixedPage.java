package com.platformda.iv.measure;

import com.platformda.datacore.EntityDevice;
import com.platformda.datacore.EntityPageType;
import com.platformda.datacore.PXYPage;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.analysis.AdvMeaOutput;
import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.analysis.MeaOutput;
import com.platformda.utility.common.StringUtil;
import java.util.Arrays;

/**
 * A fixed Page is a wrapper page for another page, it will have the output
 * "resolved", that is, will actually compute the AdvMeaOutput from the
 * MeaOutput values
 *
 */
public class AnalysisFixedPage implements AnalysisPage {

    AnalysisPage page;
    EntityDevice device;
    int stressTime = 0;
    //
    private double[] pValues = new double[0];
    private double[] xValues = new double[0];
    private double[] yValues = new double[0];
    private int yNumber = 0;
    private String[] constNames = new String[0];
    private double[] constValues = new double[0];

    public AnalysisFixedPage(AnalysisPage page, EntityDevice device, int stressTime) {
        this.page = page;
        this.device = device;
        this.stressTime = stressTime;
    }

    @Override
    public MeaAnalysis getAnalysis() {
        return page.getAnalysis();
    }

    public AnalysisPage getPage() {
        return page;
    }
    EntityPageType pageType = null;
    PXYPage pxyPage = null;

    // TODO: adapter
    public EntityPageType toPageType() {
        if (pageType != null) {
            return pageType;
        }
        MeaAnalysis analysis = getAnalysis();
        EntityPagePattern pagePattern = analysis.getPagePattern();
        MeaBias[] biases = analysis.getBiases();

        pageType = new EntityPageType();
        pageType.setGroupName(pagePattern.getGroupName());
        pageType.setDeviceType(device.getDeviceType());
        pageType.setDevicePolarity(device.getDevicePolarity());

        pageType.setYNames(pagePattern.getYNames());
        pageType.setXName(pagePattern.getXName());
        String pName = pagePattern.getPName();
        pageType.setPName(pName);
        if (pName.equalsIgnoreCase(EntityPageType.P_PLACE_HOLDER) && biases.length > 1) {
            pName = biases[1].getName();
            pageType.setPName(pName);
        }

        int condNumber = getConstNumber();
        for (int i = 0; i < condNumber; i++) {
            pageType.setCondition(getConstName(i), getConstValue(i));
        }
        if (stressTime > 0) {
            pageType.setCondition(EntityPageType.CONDITION_STRESS, stressTime);
        }
        

        return pageType;
    }

    public PXYPage toPage(EntityPageType pageType) {
        if (pxyPage != null) {
            this.pageType = pageType;
            pxyPage.setPageType(pageType);
            return pxyPage;
        }

        pxyPage = new PXYPage(device, pageType);
//        double[] xValues = new double[getXNumber()];
//        for (int i = 0; i < xValues.length; i++) {
//            xValues[i] = getX(i);
//        }
//        double[] pValues = new double[getPNumber()];
//        for (int i = 0; i < pValues.length; i++) {
//            pValues[i] = getP(i);
//        }
        pxyPage.setPValues(Arrays.copyOf(pValues, pValues.length));
        pxyPage.setXValues(Arrays.copyOf(xValues, xValues.length));

        int yNumber = getYNumber();
        for (int yIndex = 0; yIndex < yNumber; yIndex++) {
            for (int pIndex = 0; pIndex < pValues.length; pIndex++) {
                double[] yValues = new double[xValues.length];
                for (int xIndex = 0; xIndex < yValues.length; xIndex++) {
                    yValues[xIndex] = getY(xIndex, pIndex, yIndex);
                }
                pxyPage.setYValues(yIndex, pIndex, yValues);
            }
        }
        return pxyPage;
    }

    public void fix() {
        MeaAnalysis analysis = getAnalysis();
//        MeaBias[] biases = getAnalysis().getBiases();
//        int count = biases[0].size();
//        int xNumber = count;
//        int pNumber = 1;
//        if (biases.length > 1) {
//            pNumber = biases[1].size();
//            count *= pNumber;
//        }

        int xNumber = analysis.getXNumber();
        int pNumber = analysis.getPNumber();
        int count = xNumber * pNumber;

        xValues = new double[xNumber];
        for (int i = 0; i < xNumber; i++) {
//            xValues[i] = biases[0].get(i);
            xValues[i] = analysis.getX(i);
        }

        pValues = new double[pNumber];
//        if (biases.length > 1) {
//            for (int i = 0; i < pValues.length; i++) {
//                pValues[i] = biases[1].get(i);
//            }
//        }
        for (int i = 0; i < pNumber; i++) {
            pValues[i] = analysis.getP(i);
        }

        // 
        yNumber = 0;
        for (int i = 0, size = analysis.getOutputNumber(); i < size; i++) {
            MeaOutput output = analysis.getOutput(i);
            if (output.asOutput()) {
                yNumber += output.getType().getDataSize();
            }
        }
        int meaOutputNumber = yNumber;
        for (int i = 0; i < analysis.getAdvOutputNumber(); i++) {
            AdvMeaOutput output = analysis.getAdvOutput(i);
            if (output.asOutput()) {
                yNumber++;
            }
        }

        yValues = new double[count * yNumber];
        int yOffset = 0;

        for (int outputIndex = 0; outputIndex < meaOutputNumber; outputIndex++) {
            for (int xIndex = 0; xIndex < xNumber; xIndex++) {
                for (int pIndex = 0; pIndex < pNumber; pIndex++) {
                    yValues[pIndex * xNumber + xIndex + yOffset * count] = page.getY(xIndex, pIndex, outputIndex);
                }
            }
            yOffset++;
        }
        for (int i = 0; i < getAnalysis().getAdvOutputNumber(); i++) {
            AdvMeaOutput output = getAnalysis().getAdvOutput(i);
            if (!output.asOutput()) {
                continue;
            }
            String[] varNames = output.getVarNames();
            int[] varIndice = new int[varNames.length];
            for (int j = 0; j < varIndice.length; j++) {
                varIndice[j] = getMeaOutIndex(getAnalysis(), varNames[j]);
            }
            double[] varValues = new double[varIndice.length];
            for (int xIndex = 0; xIndex < xNumber; xIndex++) {
                for (int pIndex = 0; pIndex < pNumber; pIndex++) {
                    for (int varIndex = 0; varIndex < varValues.length; varIndex++) {
                        if (varIndice[varIndex] < 0) {
                            String varName = varNames[varIndex];
                            if (varName.equalsIgnoreCase("x")) {
                                varValues[varIndex] = page.getX(xIndex);
                            } else if (varName.equalsIgnoreCase("p")) {
                                varValues[varIndex] = page.getP(pIndex);
                            } else {
                                // bias
                                int constIndex = StringUtil.indexOf(varName, constNames);
                                if (constIndex >= 0) {
                                    varValues[varIndex] = constValues[constIndex];
                                }
                            }
                            continue;
                        }
                        varValues[varIndex] = page.getY(xIndex, pIndex, varIndice[varIndex]);
                    }
                    yValues[pIndex * xNumber + xIndex + yOffset * count] = output.getValue(varValues);
                }
            }
            yOffset++;
        }

        constValues = new double[page.getConstNumber()];
        constNames = new String[constValues.length];
        for (int i = 0; i < constValues.length; i++) {
            constValues[i] = page.getConstValue(i);
            constNames[i] = page.getConstName(i);
        }
    }

    int getMeaOutIndex(MeaAnalysis analysis, String meaOutName) {
        for (int i = 0; i < analysis.getOutputNumber(); i++) {
            if (analysis.getOutput(i).getName().equalsIgnoreCase(meaOutName)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getPNumber() {
        return pValues.length;
    }

    @Override
    public int getXNumber() {
        return xValues.length;
    }

    @Override
    public int getYNumber() {
        return yNumber;
    }

    @Override
    public double getX(int index) {
        return xValues[index];
    }

    @Override
    public double getP(int index) {
        return pValues[index];
    }

    @Override
    public double getY(int xIndex, int pIndex, int yIndex) {
        int xNumber = getXNumber();
        int pNumber = getPNumber();
        int count = xNumber * pNumber;
        return yValues[yIndex * count + pIndex * xNumber + xIndex];
    }

    @Override
    public int getConstNumber() {
        return constValues.length;
    }

    @Override
    public String getConstName(int index) {
        return constNames[index];
    }

    @Override
    public double getConstValue(int index) {
        return constValues[index];
    }

    @Override
    public String toString() {
        return page.toString();
    }
}
