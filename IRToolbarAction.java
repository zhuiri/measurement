/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
    category = "IR",
id = "com.platformda.mea.admin.IRToolbarAction")
@ActionRegistration(
    iconBase = "com/platformda/mea/resources/console24.png",
iconInMenu = false,
displayName = "#CTL_IRToolbarAction")
@ActionReferences({
    @ActionReference(path = "Toolbars/IR", position = 1000),
    @ActionReference(path = "Shortcuts", name = "D-T")
})
@Messages("CTL_IRToolbarAction=Instrument and Routine")
public final class IRToolbarAction extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        IRAction action = new IRAction();
        action.actionPerformed(e);
    }
}