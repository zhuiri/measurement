/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.api.MeaEditor;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Junyi
 */
public class GPIBBusPanel extends JPanel implements MeaEditor {

    private static final String[] GPIB_NAMES = new String[31];

    static {
        for (int i = 0; i < GPIB_NAMES.length; i++) {
            GPIB_NAMES[i] = "GPIB" + i;
        }
    }
    private JComboBox gpibBoardCombo = new JComboBox(GPIB_NAMES);

    public GPIBBusPanel() {
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("GPIB Board: "), BorderLayout.WEST);
        panel.add(gpibBoardCombo, BorderLayout.CENTER);


        gpibBoardCombo.setSelectedIndex(InstrumentManager.getInstance().getGpibBoardId());
        gpibBoardCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    InstrumentManager.getInstance().setGpibBoardId(gpibBoardCombo.getSelectedIndex());
                }
            }
        });
        add(panel, BorderLayout.WEST);
    }

    @Override
    public void stopEditing() {
    }
}
