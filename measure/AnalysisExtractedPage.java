package com.platformda.iv.measure;

import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.analysis.MeaBias;

/**
 * extracts page from big table
 *
 * @author Junyi
 */
public class AnalysisExtractedPage implements AnalysisPage {

    private MeaAnalysis analysis;
    private int[] constIndices;
    private AnalysisResponse.AnalysisTable analysisTable;
    //
    private int[] bases;

    //	/** the base offset of the page */
    //	int offset = 0;
    public AnalysisExtractedPage(MeaAnalysis analysis, int[] constIndices, AnalysisResponse.AnalysisTable analysisTable) {
        super();
        this.analysis = analysis;
        if (constIndices != null) {
            this.constIndices = constIndices;
        } else {
            this.constIndices = new int[0];
        }
        this.analysisTable = analysisTable;

        MeaBias[] biases = analysis.getBiases();
        bases = new int[biases.length];
        bases[0] = 1;
        for (int i = 1; i < biases.length; i++) {
            bases[i] = bases[i - 1] * biases[i - 1].size();
        }
    }

    @Override
    public MeaAnalysis getAnalysis() {
        return analysis;
    }

    @Override
    public int getYNumber() {
        return analysis.getOutputNumber();
    }

    @Override
    public int getXNumber() {
//        MeaBias[] biases = analysis.getBiases();
//        if (biases.length == 0) {
//            return 0;
//        }
//        return biases[0].size();
        return analysis.getXNumber();
    }

    @Override
    public int getPNumber() {
//        MeaBias[] biases = analysis.getBiases();
//        if (biases.length < 2) {
//            return 1;
//        }
//        return biases[1].size();
        return analysis.getPNumber();
    }

    @Override
    public double getX(int index) {
//        return analysis.getBiases()[0].get(index);
        return analysis.getX(index);
    }

    @Override
    public double getP(int index) {
//        MeaBias[] biases = analysis.getBiases();
//        if (biases.length > 1) {
//            return biases[1].get(index);
//        }
//        return 0;
        return analysis.getP(index);
    }

    @Override
    public double getY(int xindex, int pindex, int outindex) {
        if (analysis.getBiases().length == 1) {
            pindex = 0;
        }
        //		int row = pindex * analysis.getSweeps()[0].size() + xindex + offset;
        //
        int row = xindex;
        if (bases.length > 1) {
            row += pindex * bases[1];
        }
        //TODO, this is can be pre-calculated
        for (int i = 2; i < bases.length; i++) {
            row += bases[i] * constIndices[i - 2];
        }

        return analysisTable.getValue(row, outindex);
    }

    @Override
    public int getConstNumber() {
        return constIndices.length;
    }

    @Override
    public String getConstName(int index) {
        MeaBias[] biases = analysis.getBiases();
        return biases[index + 2].getName();
    }

    @Override
    public double getConstValue(int index) {
        MeaBias[] biases = analysis.getBiases();
        return biases[index + 2].get(constIndices[index]);
    }
//    @Override
//    public String toString() {
//        StringBuilder builder = new StringBuilder(analysis.getName());
//        if (constIndices.length > 0) {
//            builder.append("@");
//            for (int i = 0; i < constIndices.length; i++) {
//                if (i > 0) {
//                    builder.append(",");
//                }
//                builder.append(getConstName(i));
//                builder.append("=");
//                builder.append(StringUtil.getConciseString(getConstValue(i)));
//            }
//        }
//        return builder.toString();
//    }
}
