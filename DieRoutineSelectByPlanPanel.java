/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.MeaData;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.MeaSpace;
import com.platformda.utility.tree.CheckableTreeNode;
import com.platformda.utility.tree.CheckableTreeUtil;
import com.platformda.utility.ui.JTreeUtil;
import com.platformda.utility.ui.TreePanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.tree.TreePath;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Junyi
 */
public class DieRoutineSelectByPlanPanel extends TreePanel implements ActionListener {

    MeaSpace meaSpace;
    List<MeaJob> jobs = new ArrayList<MeaJob>();
    //
    final JButton selectAllPlansButton = new JButton("");
    final JButton deselectAllPlansButton = new JButton("");
    //
    DieRoutineSelectionPanel selectionPanel;
    MeaJob currentJob = null;

    public DieRoutineSelectByPlanPanel(MeaSpace meaSpace) {
        this.meaSpace = meaSpace;
        initComponents(150);
        initLeftToolBar();
//        setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE);
        setPreferredSize(MeaOptions.COMPONENT_BROAD_SIZE);

        JTreeUtil.selectFirstPath(tree);
    }

    private void initLeftToolBar() {
//        Image badgeImage = ImageUtilities.loadImage("com/platformda/mea/resources/badge_animate.png");
//        selectAllButton.setIcon(GUIUtil.getIcon(ImageUtilities.loadImage("com/platformda/mea/resources/checked.png"), badgeImage, 4, 4));
//        deselectAllButton.setIcon(GUIUtil.getIcon(ImageUtilities.loadImage("com/platformda/mea/resources/unchecked.png"), badgeImage, 4, 4));
        selectAllPlansButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/checked.png")));
        deselectAllPlansButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/mea/resources/unchecked.png")));
        selectAllPlansButton.setToolTipText("Select all plans");
        deselectAllPlansButton.setToolTipText("Unselect all plans");
        selectAllPlansButton.addActionListener(this);
        deselectAllPlansButton.addActionListener(this);

        JToolBar toolbar = new JToolBar();
        toolbar.setLayout(new FlowLayout(FlowLayout.TRAILING, 0, 0));

        toolbar.add(selectAllPlansButton);
        toolbar.add(deselectAllPlansButton);

        leftPanel.add(toolbar, BorderLayout.NORTH);
    }

    public void selectPlans(List<MeaPlan> plans) {
        int jobNumber = rootNode.getChildCount();

        CheckableTreeNode firstSelectedNode = null;

        // from 1 to ingore first default job
        for (int i = 1; i < jobNumber; i++) {
            CheckableTreeNode jobNode = (CheckableTreeNode) rootNode.getChildAt(i);
            MeaJob job = (MeaJob) jobNode.getAssociatedObject();
            MeaPlan plan = job.getPlan();
            if (plans.contains(plan)) {
                jobNode.setChecked(true);
                if (firstSelectedNode == null) {
                    firstSelectedNode = jobNode;
                }
            }
        }
        if (firstSelectedNode != null) {
            TreePath path = new TreePath(firstSelectedNode.getPath());
            tree.setSelectionPath(path);
        }
    }

    public void selectDie(MeaPlan plan, WaferDieInfo dieInfo) {
        int jobNumber = rootNode.getChildCount();
        for (int i = 0; i < jobNumber; i++) {
            CheckableTreeNode jobNode = (CheckableTreeNode) rootNode.getChildAt(i);
            MeaJob job = (MeaJob) jobNode.getAssociatedObject();
            MeaPlan jobPlan = job.getPlan();
            boolean matching = false;
            if (plan == null && i == 0) {
                matching = true;
                plan = jobPlan;
            } else {
                if (plan == jobPlan) {
                    matching = true;
                }
            }
            if (matching) {
                jobNode.setChecked(true);

                TreePath path = new TreePath(jobNode.getPath());
                tree.setSelectionPath(path);
                List<WaferDieInfo> dieInfos = job.getDieInfos();
                dieInfos.clear();
                dieInfos.add(dieInfo);
                selectionPanel.updateSelectedDies(dieInfos);
                break;
            }
        }

    }

    @Override
    public void buildTree() {
        MeaData meaData = meaSpace.getMeaData();

        MeaPlan defaultPlan = meaSpace.getDefaultPlan();
        MeaJob defaultJob = new MeaJob(defaultPlan, false);
        defaultJob.setAsDefault(true);
        jobs.add(defaultJob);

        List<MeaPlan> plans = meaData.getPlans();
        for (MeaPlan plan : plans) {
            MeaJob job = new MeaJob(plan, true);
            jobs.add(job);
        }

        for (MeaJob job : jobs) {
            CheckableTreeNode planNode = new CheckableTreeNode(job.getName(), job);
            planNode.setCheckable(true);
            rootNode.add(planNode);
        }
    }

    @Override
    public void updateContent(Object lastPathComponent) {
        stopEditing();
        currentJob = null;
        CheckableTreeNode treeNode;
        if (lastPathComponent != null && lastPathComponent instanceof CheckableTreeNode) {
            treeNode = (CheckableTreeNode) lastPathComponent;
            Object obj = treeNode.getAssociatedObject();
            if (obj != null && obj instanceof MeaJob) {
                MeaJob job = (MeaJob) obj;
                MeaPlan plan = job.getPlan();
                currentJob = job;

                if (selectionPanel == null) {
                    selectionPanel = new DieRoutineSelectionPanel(meaSpace, plan.getDieInfos(), plan.getRoutineTuples());
                    contentPanel.add(selectionPanel, BorderLayout.CENTER);
                    contentPanel.updateUI();
                }
                selectionPanel.update(plan.getDieInfos(), plan.getRoutineTuples(), job.getDieInfos(), job.getRoutineTuples(), plan);
            }
        }
    }

    public void stopEditing() {
        if (currentJob != null && selectionPanel != null) {
            List<WaferDieInfo> dieInfos = currentJob.getDieInfos();
            List<RoutineTuple> routineTuples = currentJob.getRoutineTuples();

            dieInfos.clear();
            dieInfos.addAll(selectionPanel.getSelectedDies());

            routineTuples.clear();
            routineTuples.addAll(selectionPanel.getSelectedRoutineTuples());
        }
    }

    @Override
    public void treeNodeChecked(CheckableTreeNode node, boolean checked) {
        // do nothing
//        Object obj = node.getAssociatedObject();
//        if (obj != null && obj instanceof MeaJob) {
//            
//        }
    }

    public List<MeaJob> getJobs() {
        return jobs;
    }

    public List<MeaJob> getSelectedJobs() {
        List<MeaJob> results = new ArrayList<MeaJob>();
        CheckableTreeUtil.fetchCheckedLeafObjects(tree, results);
        for (Iterator<MeaJob> it = results.iterator(); it.hasNext();) {
            MeaJob meaJob = it.next();
            if (!meaJob.isValid()) {
                it.remove();
            }
        }
        return results;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == selectAllPlansButton) {

            int number = rootNode.getChildCount();
            if (number > 1) {
                for (int i = 1; i < number; i++) {
                    CheckableTreeNode node = (CheckableTreeNode) rootNode.getChildAt(i);
                    CheckableTreeUtil.checkNode(node, true, false, this);
                }
                tree.repaint();

                int[] rows = tree.getSelectionRows();
                if (rows == null || rows.length == 0 || rows[0] == 0) {
                    CheckableTreeNode firstSelectedNode = (CheckableTreeNode) rootNode.getChildAt(1);
                    TreePath path = new TreePath(firstSelectedNode.getPath());
                    tree.setSelectionPath(path);
                }
            }
        } else if (source == deselectAllPlansButton) {
            int number = rootNode.getChildCount();
            if (number > 1) {
                for (int i = 1; i < number; i++) {
                    CheckableTreeNode node = (CheckableTreeNode) rootNode.getChildAt(i);
                    CheckableTreeUtil.checkNode(node, false, false, this);
                }
                tree.repaint();
                int[] rows = tree.getSelectionRows();
                if (rows == null || rows.length == 0 || rows[0] == 0) {
                    CheckableTreeNode firstSelectedNode = (CheckableTreeNode) rootNode.getChildAt(1);
                    TreePath path = new TreePath(firstSelectedNode.getPath());
                    tree.setSelectionPath(path);
                }
            }
        }
    }
}
