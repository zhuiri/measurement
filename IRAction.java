/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

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

@ActionID(
    category = "Settings",
id = "com.platformda.mea.admin.IRAction")
@ActionRegistration(
         iconBase = "com/platformda/mea/resources/console.png",
displayName = "#CTL_IRAction")
@ActionReference(path = "Menu/Settings", position = 100)
@NbBundle.Messages("CTL_IRAction=Instrument and Routine")
public class IRAction extends AbstractAction implements ActionListener {
    
    public IRAction() {
        super("Instrument and Routine");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        IRPanel form = new IRPanel();
//        DialogDescriptor desc = new DialogDescriptor(form,
//                "Instrument and Measurement", true, DialogDescriptor.OK_CANCEL_OPTION,
//                DialogDescriptor.OK_OPTION, null);
//        if (result == NotifyDescriptor.OK_OPTION) {
//            // TODO:
//        }
        DialogDescriptor desc = new DialogDescriptor(form, "Instrument and Routine");
        desc.setOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
        DialogDisplayer.getDefault().notify(desc); // displays the dialog
        String configPath = PathUtil.getConfigPath();
        try {
            form.stopEditing();
            MeaDeviceTypeManager.getInstance().save(configPath);
            InstrumentManager.getInstance().save(configPath);
            RoutinePatternManager.getInstance().save(configPath);
            RoutineManager.getInstance().save(configPath);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
