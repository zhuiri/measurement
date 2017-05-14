/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;

/**
 *
 * @author renjing
 */
public interface SelectAble {
    ThreeStateCheckBox.SelectState getSelectSate();
    void setSelected(ThreeStateCheckBox.SelectState state);
    String getShowName();
}
