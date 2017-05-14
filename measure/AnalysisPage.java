package com.platformda.iv.measure;

import com.platformda.iv.analysis.MeaAnalysis;

public interface AnalysisPage {

    public MeaAnalysis getAnalysis();

    public int getPNumber();

    public int getXNumber();

    public int getYNumber();

    public double getP(int index);

    public double getX(int index);

    public double getY(int xIndex, int pIndex, int yIndex);

    public int getConstNumber();

    public String getConstName(int index);

    public double getConstValue(int index);
}
