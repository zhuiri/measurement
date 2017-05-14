package com.platformda.iv.measure;

import com.platformda.iv.MeaDAO;
import com.platformda.iv.MeaData;
import com.platformda.iv.admin.RoutineSelector;
import com.platformda.utility.common.Console;

/**
 *
 *
 */
public interface MeasurerContext extends RoutineSelector {

    public boolean shouldZero();

    public MeaCallback getCallback();

    public PageCallback getPageCallback();

    public Console getConsole();

    public MeaData getMeaData();

    public MeaDAO getDao();
}
