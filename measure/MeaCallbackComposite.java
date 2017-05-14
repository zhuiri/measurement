/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.measure;

/**
 *
 * @author Junyi
 */
public class MeaCallbackComposite implements MeaCallback {

//    MeaCallback m0;
//    MeaCallback m1;
    MeaCallback[] callbacks;

    public MeaCallbackComposite(MeaCallback[] callbacks) {
//        this.m0 = m0;
//        this.m1 = m1;
        this.callbacks = callbacks;
    }

    @Override
    public void start() {
//        m0.start();
//        m1.start();
        for (MeaCallback callback : callbacks) {
            callback.start();
        }
    }

    @Override
    public void finish() {
//        m0.finish();
//        m1.finish();
        for (MeaCallback callback : callbacks) {
            callback.finish();
        }
    }
}
