/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.view;


import javax.swing.*;
import org.openide.util.ImageUtilities;

/**
 *
 * @author renjing
 */
public class ThreeStateCheckBox extends JCheckBox {

    private static final Icon selected = ImageUtilities.loadImageIcon("com/platformda/mea/resources/run.png", true);
    private static final Icon unSelected = ImageUtilities.loadImageIcon("com/platformda/mea/resources/run.png", true);
    private static final Icon partSelect = ImageUtilities.loadImageIcon("com/platformda/mea/resources/run.png", true);
            //IconUtil.loadIcon("/image/checkbox_part.png", ThreeStateCheckBox.class);

    public ThreeStateCheckBox(String name) {
        super(name);
        setModel(new ThreeStateButtonModel());
    }

    public void setCheckState(SelectState state) {
        ((ThreeStateButtonModel)getModel()).setCheckState(state);
    }

    public SelectState getState() {
        return ((ThreeStateButtonModel)getModel()).getState();
    }
    
    @Override
    public Icon getIcon() {
        SelectState s=getState();
        switch(s){
            case SELECT:
                return selected;
            case PART_SELECT:
                return partSelect;
            default:
                return unSelected;
        }
    }

    private static class ThreeStateButtonModel extends ToggleButtonModel {

        private SelectState state = SelectState.SELECT;

        public void setCheckState(SelectState state) {
            if (this.state != state) {
                this.state = state;
                fireStateChanged();
            }
        }

        public SelectState getState() {
            return state;
        }

        @Override
        public boolean isSelected() {
            return state== SelectState.SELECT;
        }

        @Override
        public void setSelected(boolean b) {
            setCheckState(b? SelectState.SELECT: SelectState.UN_SELECT);
        }
    }

    public enum SelectState {
        SELECT,
        PART_SELECT,
        UN_SELECT
    }

}
