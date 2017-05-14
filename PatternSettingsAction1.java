/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.tools.ToolBarManager;
import com.platformda.utility.common.PathUtil;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;


public class PatternSettingsAction1 extends PatternSettingsAction{
   

    public PatternSettingsAction1() {
       super();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PatternSettingsPanel.HIDDEN_STRATEGY = true;
        super.doAction();
    }
}
