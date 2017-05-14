package com.platformda.iv.measure;

import com.platformda.iv.api.MeaResponse;
import java.io.IOException;

/**
 * An interface to measure a single request
 *
 */
public interface Gauge extends Stoppable {

    public MeaResponse doGauge(GaugeContext context) throws IOException;
}
