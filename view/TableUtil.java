/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

import java.awt.Container;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

/**
 *
 * @author renjing
 */
public class TableUtil {
    
    
    /*
     * set table column with ,use netative number indicaing ignore
     */
    public static void configTableColumeWidth(JTable table, int col, int min, int pre, int max) {
        TableColumn c = table.getColumnModel().getColumn(col);
        if (min > -1) {
            c.setMinWidth(min);
        }
        if (pre > -1) {
            c.setPreferredWidth(pre);
        }
        if (max > -1) {
            c.setMaxWidth(max);
        }
    }
}
