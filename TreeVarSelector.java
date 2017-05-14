/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.measure.DeviceBondProvider;
import com.platformda.spec.SpecPattern;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.tree.CheckableTreeNode;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Junyi
 */
public class TreeVarSelector implements VarProviderEditorImpl.VarSelector {

    DeviceBondProvider deviceBondProvider;
    CheckableTreeNode pageNode;
    CheckableTreeNode specNode;
    JTree tree;
    //
    String lastVar = null;
    Map<SpecPattern, List<String>> specVars = new HashMap<SpecPattern, List<String>>();
    Map<EntityPagePattern, List<String>> pageVars = new HashMap<EntityPagePattern, List<String>>();
    public static final Image pageImage = ImageUtilities.loadImage("com/platformda/iv/resources/page.png");
    public static final Image specImage = ImageUtilities.loadImage("com/platformda/iv/resources/spec.png");
    public static final Image validBadge = ImageUtilities.loadImage("com/platformda/iv/resources/existing-badge.png");
    public static final Image badge = ImageUtilities.loadImage("com/platformda/iv/resources/infoBadge.gif");
    public static final Image pageValidImage = ImageUtilities.mergeImages(pageImage, validBadge, 18, 0);
    public static final Image specValidImage = ImageUtilities.mergeImages(specImage, validBadge, 18, 0);
    public static final Image pageBadgeImage = ImageUtilities.mergeImages(pageImage, badge, 0, 0);
    public static final Image specBadgeImage = ImageUtilities.mergeImages(specImage, badge, 0, 0);
    public static final Image pageValidBadgeImage = ImageUtilities.mergeImages(pageBadgeImage, validBadge, 18, 0);
    public static final Image specValidBadgeImage = ImageUtilities.mergeImages(specBadgeImage, validBadge, 18, 0);
    public static final ImageIcon pageIcon = new ImageIcon(pageImage);
    public static final ImageIcon specIcon = new ImageIcon(specImage);
    public static final ImageIcon pageBadgeIcon = new ImageIcon(pageBadgeImage);
    public static final ImageIcon specBadgeIcon = new ImageIcon(specBadgeImage);
    public static final ImageIcon pageValidIcon = new ImageIcon(pageValidImage);
    public static final ImageIcon specValidIcon = new ImageIcon(specValidImage);
    public static final ImageIcon pageValidBadgeIcon = new ImageIcon(pageValidBadgeImage);
    public static final ImageIcon specValidBadgeIcon = new ImageIcon(specValidBadgeImage);

    public TreeVarSelector(DeviceBondProvider deviceBondProvider, CheckableTreeNode pageNode, CheckableTreeNode specNode, JTree tree) {
        this.deviceBondProvider = deviceBondProvider;
        this.pageNode = pageNode;
        this.specNode = specNode;
        this.tree = tree;
    }

    public TreeVarSelector(DeviceBondProvider deviceBondProvider, CheckableTreeNode pageNode, CheckableTreeNode specNode, JTree tree, List<EntityPagePattern> pagePatterns, List<SpecPattern> specPatterns) {
        this.deviceBondProvider = deviceBondProvider;
        this.pageNode = pageNode;
        this.specNode = specNode;
        this.tree = tree;

        updateVars(pagePatterns, specPatterns);
    }

    public void setDeviceBondProvider(DeviceBondProvider deviceBondProvider) {
        this.deviceBondProvider = deviceBondProvider;
    }

    public void updateVars(List<EntityPagePattern> pagePatterns, List<SpecPattern> specPatterns) {
        specVars.clear();
        for (SpecPattern pattern : specPatterns) {
            List<String> vars1 = new ArrayList<String>();
            pattern.fetchSimulationVarNames(vars1);
            specVars.put(pattern, vars1);
        }
        pageVars.clear();
        for (EntityPagePattern pattern : pagePatterns) {
            List<String> vars1 = new ArrayList<String>();
            pattern.fetchVars(vars1);
            pageVars.put(pattern, vars1);
        }
    }

    public void updateIcons() {
        onVarSelected(lastVar);
    }

    @Override
    public void onVarSelected(String var) {
        lastVar = var;
        if (var == null) {
            int number = pageNode.getChildCount();
            for (int i = 0; i < number; i++) {
                CheckableTreeNode node = (CheckableTreeNode) pageNode.getChildAt(i);
                EntityPagePattern pattern = (EntityPagePattern) node.getAssociatedObject();
                if (deviceBondProvider.hasDeviceBond(pattern)) {
                    node.setIcon(pageValidIcon);
                } else {
                    node.setIcon(pageIcon);
                }
            }
            number = specNode.getChildCount();
            for (int i = 0; i < number; i++) {
                CheckableTreeNode node = (CheckableTreeNode) specNode.getChildAt(i);
                SpecPattern pattern = (SpecPattern) node.getAssociatedObject();
                if (deviceBondProvider.hasDeviceBond(pattern)) {
                    node.setIcon(specValidIcon);
                } else {
                    node.setIcon(specIcon);
                }
            }

        } else {
            int number = pageNode.getChildCount();
            for (int i = 0; i < number; i++) {
                CheckableTreeNode node = (CheckableTreeNode) pageNode.getChildAt(i);
                EntityPagePattern pagePattern = (EntityPagePattern) node.getAssociatedObject();
                boolean hasBond = deviceBondProvider.hasDeviceBond(pagePattern);

                if (StringUtil.contains(pageVars.get(pagePattern), var)) {
                    if (hasBond) {
                        node.setIcon(pageValidBadgeIcon);
                    } else {
                        node.setIcon(pageBadgeIcon);
                    }
                } else {
                    if (hasBond) {
                        node.setIcon(pageValidIcon);
                    } else {
                        node.setIcon(pageIcon);
                    }
                }
            }
            number = specNode.getChildCount();
            for (int i = 0; i < number; i++) {
                CheckableTreeNode node = (CheckableTreeNode) specNode.getChildAt(i);
                SpecPattern pattern = (SpecPattern) node.getAssociatedObject();
                boolean hasBond = deviceBondProvider.hasDeviceBond(pattern);
                if (StringUtil.contains(specVars.get(pattern), var)) {
                    if (hasBond) {
                        node.setIcon(specValidBadgeIcon);
                    } else {
                        node.setIcon(specBadgeIcon);
                    }
                } else {
                    if (hasBond) {
                        node.setIcon(specValidIcon);
                    } else {
                        node.setIcon(specIcon);
                    }
                }
            }
        }
//        tree.repaint();
        tree.updateUI();
    }
}
