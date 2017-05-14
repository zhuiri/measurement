/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

import com.platformda.utility.common.Console;

/**
 *
 * @author Junyi
 */
public class StoppableImpl implements Stoppable {

    protected volatile Object pauseObj = null;
    protected volatile boolean pause = false;
    Console console;

    public StoppableImpl(Console console) {
        this.console = console;
    }

    @Override
    public synchronized void pause(Object obj) {
        if (pause) {// do nothing when already paused
            return;
        }

        pauseObj = obj;
        this.pause = true;
    }

    @Override
    public synchronized void resume() {
        if (pauseObj == null) {
            return;
        }

        synchronized (pauseObj) {
            pause = false;
            pauseObj.notifyAll();
        }
    }

    protected void pauseOrStop() {
        if (pauseObj != null) {
            synchronized (pauseObj) {
                if (pause) {
                    try {
                        //console.info("Measurment is running, please wait for its done");
                        //wait the obj of measureContext,which is implement in the manualMeasurer
                        //to get the measpace ,meadao etc
                        //to actually, measurement will get no data
                        pauseObj.wait();
                    } catch (InterruptedException e) {
                        throw new MeaInterruptedException();
                    }
                }
            }
        }
        if (Thread.currentThread().isInterrupted()) {
            // user has tried to stop the measure process
            throw new MeaInterruptedException();
        }
    }
}