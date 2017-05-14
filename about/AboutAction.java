/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.about;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

@ActionID(
    category = "Help",
id = "com.platformda.iv.about.AboutAction")
@ActionRegistration(
    displayName = "About")
@ActionReference(path = "Menu/Help", position = 10000)
public final class AboutAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        DialogDescriptor desc = new DialogDescriptor(new AboutPanel(), "About");
        desc.setOptions(new Object[]{DialogDescriptor.CLOSED_OPTION});
        DialogDisplayer.getDefault().notify(desc); // displays the dialog
    }

    /**
     * displays the product information, including the product name, version
     * number, etc.
     */
    class AboutPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        public AboutPanel() {
            super(new BorderLayout(5, 0));
            JLabel imageLabel = new JLabel(new ImageIcon(ImageUtilities.loadImage("com/platformda/system/resources/logo.jpg")));
            add(imageLabel, BorderLayout.WEST);
            StringBuilder buffer = new StringBuilder("<html>");
            buffer.append("<font style=\"font-size:11px\">");
            buffer.append("LabExpress");
            buffer.append("<br>");

            String year = org.openide.util.NbBundle.getMessage(AboutAction.class, "year");
            String version = org.openide.util.NbBundle.getMessage(AboutAction.class, "version");
            String build = org.openide.util.NbBundle.getMessage(AboutAction.class, "build");

            buffer.append("Version ");
            //add version
            //buffer.append("1.0.3 ");
            buffer.append(version);
            buffer.append(", Build ");
            buffer.append(build);
//		buffer.append("<br>License Expired Date : ").append(Constants.getLicenseExpiredDate());
            // buffer.append("<br>Build time:" + Version.getBuildTimeString());
            buffer.append("<br>Copyright &copy; ");
            buffer.append(year);
            buffer.append(" by Platform Design Automation, Inc.");
            buffer.append("</font></html>");
            add(new JLabel(buffer.toString()));
        }
    }
}
