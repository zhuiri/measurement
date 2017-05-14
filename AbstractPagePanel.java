/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import javax.swing.JPanel;

/**
 *
 * @author renjing
 */
public abstract class AbstractPagePanel extends JPanel implements PagePanel{
    
    public abstract void setRawPatternName(String rawName);
    
    public abstract void doUpdate();
    
    public String getErrorInfo(){
        return null;
    }
    
}
