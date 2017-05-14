/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.utility.common.Console;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Junyi
 */
public class ConsoleImpl implements Console {

    InputOutput io;

    public ConsoleImpl(String title) {
        io = IOProvider.getDefault().getIO(title, false); // false means reuse
        io.select();
//        io.getOut().println("Hello from standard out");
//        io.getErr().println("Hello from standard err");  //this text should appear in red
//        try {
//            io.getOut().reset(); // clear all messages (out and err)
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        io.getOut().close();
//        io.getErr().close();
//        io.closeInputOutput(); // close the tab
    }
    
    public void select(){
        if(io != null){
            io.select();
        }
    }

    @Override
    public void warn(String message) {
        io.getOut().println(message);
    }

    @Override
    public void err(String message) {
        io.getErr().println(message);
    }

    @Override
    public void info(String message) {
        io.getOut().println(message);
    }

    @Override
    public void clear() {
    }

    public void close() {
        io.getOut().close();
        io.getErr().close();
        io = null;
    }
}
