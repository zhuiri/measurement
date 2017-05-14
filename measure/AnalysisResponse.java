package com.platformda.iv.measure;

import com.platformda.datacore.EntityDevice;
import com.platformda.iv.analysis.MeaAnalysis;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.analysis.MeaOutput;
import com.platformda.iv.api.MeaResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalysisResponse implements MeaResponse {

    private AnalysisTable analysisTable;
    private List<AnalysisPage> pages;
    private List<AnalysisFixedPage> fixedPages;
//    static final int NOTIFY_ON_PAGE = 0;
//    static final int NOTIFY_ON_LINE = 1;
//    private int notifyPolicy = NOTIFY_ON_PAGE;
    private int rowTickOnReady = 0;
    private PageCallback pageCallback;
    EntityDevice device;
    int stressTime = 0;

    public AnalysisResponse(MeaAnalysis analysis, PageCallback pageCallback, EntityDevice device, int stressTime) {
        this.pageCallback = pageCallback;
        this.device = device;
        this.stressTime = stressTime;

        // TODO: if nop, tick= xnumber, modify extractedPage and fixedPage
        int tableRowNumber = 1;
        MeaBias[] biases = analysis.getBiases();
        int pageNumber = 1;
        int[] constNumbers = new int[0];
        if (biases.length > 2) {
            constNumbers = new int[biases.length - 2];
        }
        for (int biasIndex = 0; biasIndex < biases.length; biasIndex++) {
            int size = biases[biasIndex].size();
            if (biasIndex > 1) {
                pageNumber *= size;
                constNumbers[biasIndex - 2] = size;
            }
            tableRowNumber *= size;
        }

        int outputColumnNumber = 0;
        for (int outputIndex = 0; outputIndex < analysis.getOutputNumber(); outputIndex++) {
            MeaOutput output = analysis.getOutput(outputIndex);
            outputColumnNumber += output.getType().getDataSize();
        }
        analysisTable = new AnalysisTable(tableRowNumber, outputColumnNumber);

        pages = new ArrayList(pageNumber);
        fixedPages = new ArrayList(pageNumber);
        if (biases.length < 3) {
            AnalysisPage page = new AnalysisExtractedPage(analysis, new int[0], analysisTable);
            pages.add(page);
            fixedPages.add(new AnalysisFixedPage(page, device, stressTime));
        } else {
            int[] indices = new int[constNumbers.length];
            addPage(analysis, constNumbers, indices, constNumbers.length - 1);
        }
        // determine notify pad
//        switch (notifyPolicy) {
//            case NOTIFY_ON_PAGE:
//                notifyPad = biases[0].size();
//                if (biases.length > 1) {
//                    notifyPad *= biases[1].size();
//                }
//                break;
//            case NOTIFY_ON_LINE:
//                notifyPad = biases[0].size();
//                break;
//            default:
//                break;
//        }

        rowTickOnReady = biases[0].size();
        if (biases.length > 1) {
            rowTickOnReady *= biases[1].size();
        }
    }

    private void addPage(MeaAnalysis analysis, int[] constNumbers, int[] constIndices, int level) {
        if (level == -1) {
            int[] indices = Arrays.copyOf(constIndices, constIndices.length);
            AnalysisPage page = new AnalysisExtractedPage(analysis, indices, analysisTable);
            pages.add(page);
            fixedPages.add(new AnalysisFixedPage(page, device, stressTime));
            return;
        }
        for (int i = 0; i < constNumbers[level]; i++) {
            constIndices[level] = i;
            addPage(analysis, constNumbers, constIndices, level - 1);
        }
    }

    public RowExpandedTable getTable() {
        return analysisTable;
    }

    public class AnalysisTable extends RowExpandedTableImpl {

        public AnalysisTable(int row, int column) {
            super(row, column);
        }

        @Override
        public boolean setValue(int row, int column, double value) {
            boolean rt = super.setValue(row, column, value);
            //merge more than one curve eg.ic,ib
            //fix page when column as the last one
            if (column == (this.getColumnNumber() - 1)) {
                if ((row + 1) % rowTickOnReady == 0) {
                    // find the page
                    int index = row / rowTickOnReady;

                    // TODO there is a exception here
                    if (index >= fixedPages.size()) {
                        return false;
                    }

                    AnalysisFixedPage page = (AnalysisFixedPage) fixedPages.get(index);
                    page.fix();
                    if (pageCallback != null) {
                        pageCallback.onPageReady(page);
                    }
                }
            }
           
            return rt;
        }
    }

    public AnalysisPage[] getPages() {
        return (AnalysisPage[]) fixedPages.toArray(new AnalysisPage[0]);
    }
}
