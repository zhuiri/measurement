/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.utility.common.Console;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Junyi
 */
public class LogMeaCallback implements MeaCallback, Console {

    private static final Logger logger = Logger.getLogger(LogMeaCallback.class.getName());
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    Console console;
    long started = 0;
    long lastDieStarted = 0;
    long lastSubDieStarted = 0;

    public LogMeaCallback(Console console) {
        this.console = console;
    }

    @Override
    public void start() {
        started = System.currentTimeMillis();
    }

    @Override
    public void finish() {
        long time = System.currentTimeMillis() - started;
        logger.log(Level.INFO, "Done all measurements at {0}, time consumed: {1}s", new Object[]{dateFormat.format(new Date()), time / 1000.0});
        //console.info("Done all measurements at " + dateFormat.format(new Date()) + ", time consumed: " + time / 1000.0 + "s");
    }

    @Override
    public void warn(String message) {
        logger.warning(message);
        console.warn(message);
    }

    @Override
    public void err(String message) {
        logger.warning(message);
        console.err(message);
    }

    @Override
    public void info(String message) {
        logger.info(message);
        console.info(message);
    }

    @Override
    public void clear() {
        console.clear();
    }
}
