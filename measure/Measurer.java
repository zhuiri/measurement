/**
 *
 */
package com.platformda.iv.measure;

import java.io.IOException;
import org.netbeans.api.progress.ProgressHandle;

/**
 * Responsible for doing a whole round of measurement
 *
 */
public interface Measurer extends Stoppable {

    public void doMeasurement(MeasurerContext context, ProgressHandle handle) throws IOException;
}
