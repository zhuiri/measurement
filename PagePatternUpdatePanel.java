/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.datacore.pattern.FuncField;
//import com.platformda.datacore.pattern.PagePatternLoader;
import com.platformda.iv.datacore.pattern.PagePatternLoader;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.analysis.ArrayMeaBundle;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.analysis.MeaBundle;
import com.platformda.iv.analysis.Sweep;
import com.platformda.iv.analysis.SweepMeaBundle;
import com.platformda.iv.tools.PatternUtil;
import com.platformda.iv.tools.TableModelInformer;
import com.platformda.iv.tools.TableModelInformerManager;
//import com.platformda.syntax.SyntaxEditor;
import com.platformda.iv.tools.auto.SyntaxEditor;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.common.StringBuilderAppender;
import com.platformda.utility.common.VarProvider;
import com.platformda.utility.ui.DocumentListenerStub;
import com.platformda.utility.ui.GUIUtil;
import com.platformda.utility.ui.JTableUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.Document;
import net.miginfocom.swing.MigLayout;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Junyi
 */
public class PagePatternUpdatePanel extends AbstractPagePanel implements ActionListener ,TableModelInformer{

    String rawPatternName;
    EntityPagePattern pagePattern;
    VarProvider rawVarProvider;
    BaseVarProvider varProvider = new BaseVarProvider();
    SyntaxEditor textArea = new SyntaxEditor("text/page");
    JButton updateButton = new JButton("Update");
    JButton helpButton = new JButton("");
    VarProviderEditorImpl varEditor;
    
    //quick show 
    Routine routine;
    StringBuilder xQuickShow = new StringBuilder();
    StringBuilder yQuickShow = new StringBuilder();
    StringBuilder pQuickShow = new StringBuilder();
    
    JTextArea quickShowText = new JTextArea();
    
    //all updated patterns
    Map<String,EntityPagePattern> updatedPatterns  = new LinkedHashMap<>();

    public PagePatternUpdatePanel(VarProvider rawVarProvider) {
        this.rawVarProvider = rawVarProvider;
        initComponents();
        //add TableModelInformer listeners
        TableModelInformerManager.removeAllListeners();
        TableModelInformerManager.addTableModelListener(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
         if(rawVarProvider instanceof IndPagePattern){
             IndPagePattern ipp = (IndPagePattern)rawVarProvider;
             varEditor = new VarProviderEditorImpl(ipp.pagePattern.getName(),varProvider, null);
        }else if(rawVarProvider instanceof RoutinePattern){
              RoutinePattern routinePattern = (RoutinePattern)rawVarProvider;
              if(routinePattern.getPagePatterns().isEmpty()){
                  varEditor = new VarProviderEditorImpl(routinePattern.getPagePattern(0).getName(),varProvider, null);
              }else{
                  varEditor = new VarProviderEditorImpl(varProvider, null);
              }
              
        }
        //varEditor = new VarProviderEditorImpl(varProvider, null);
        JTableUtil.setVisibleRowCount(varEditor.getTable(), 5);

//        textArea.getEditorPane().setEditable(false);
        textArea.hideToolBar();
        textArea.getEditorPane().setSize(80, 15);

        Document doc = textArea.getEditorPane().getDocument();
        DocumentListenerStub stub = new DocumentListenerStub() {
            @Override
            public void validate() {
                updateButton.setEnabled(true);
                doUpdate();
                updateButton.setEnabled(false);
            }
        };
        doc.addDocumentListener(stub);
        
        //help toolbar
        helpButton.setIcon(new ImageIcon(ImageUtilities.loadImage("com/platformda/iv/resources/add.gif")));
        helpButton.setToolTipText("Help");
        helpButton.addActionListener(this);
        JPanel toolBarPanel = GUIUtil.createToolBarPanel(FlowLayout.TRAILING, helpButton);

        updateButton.addActionListener(this);
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(updateButton, BorderLayout.EAST);
        updateButton.setEnabled(false);
        //centerPanel.add(toolBarPanel,BorderLayout.NORTH);
        centerPanel.add(textArea, BorderLayout.CENTER);
        //centerPanel.add(toolBarPanel, BorderLayout.SOUTH);
        //centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(varEditor,BorderLayout.CENTER);     
        //add  quick show for pattern data
        //JPanel displayPanel = new JPanel(new BorderLayout(5,15));
        JPanel displayPanel = new JPanel(new MigLayout("wrap 2"));
        //JLabel label = new JLabel("Quick show:");
        //label.setBackground(Color.GREEN);
        //displayPanel.add(label);
        quickShowText.setBackground(Color.GREEN);
        quickShowText.setEditable(false);
        displayPanel.add(quickShowText);        
//        displayPanel.add(new JLabel("X:"));
//        displayPanel.add(xText);
//        displayPanel.add(new JLabel("Y:"));
//        displayPanel.add(new JLabel(yQuickShow.toString()));
//        displayPanel.add(new JLabel("P:")); 
//        displayPanel.add(new JLabel(pQuickShow.toString()));
        //lock
        southPanel.add(displayPanel,BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        updateContent();
//        updateVars();
        setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE3);
    }

    protected void updateContent() {
        if (pagePattern != null) {
            StringBuilder builder = new StringBuilder();
            StringBuilderAppender appender = new StringBuilderAppender(builder);
            try {
                PagePatternLoader.save(pagePattern, appender);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            textArea.setContent(builder.toString());
        }
    }

    public void updateVars(EntityPagePattern pagePattern) {
        if (pagePattern != null) {
            List<String> vars = new ArrayList();
            pagePattern.fetchVars(vars);
            if (rawVarProvider instanceof IndPagePattern) {
                //nothing to do
            } else if (rawVarProvider instanceof RoutinePattern) {
                //get old varProvider   
                varEditor.getTableModel().setPatternName(pagePattern.getName());
                Map<String, VarProvider> updatedPatternVarsMap = varEditor.getTableModel().getUpdatedPatternVarsMap();
                VarProvider oldVarProvider = updatedPatternVarsMap.get(pagePattern.getName());
                if (oldVarProvider == null) {
                    varProvider.clearVars();
                } else {
                    BaseVarProvider.copyVars(oldVarProvider, varProvider);
                }
            }

            
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            for (String var : vars) {
                Object obj = varProvider.getVar(var);
                if (obj == null) {
                    obj = rawVarProvider.getVar(var);
                }
                if (obj == null) {
                    map.put(var, 0.0);
                } else {
                    map.put(var, obj);
                }
            }
            varProvider.clearVars();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String var = entry.getKey();
                Object object = entry.getValue();
                varProvider.setVar(var, object);
            }
            varEditor.updateView();
        }
    }
    
    public BaseVarProvider getVarProvider(){
        return this.varProvider;
    }

    public void setPagePattern(EntityPagePattern pagePattern) {
        this.pagePattern = pagePattern;
        this.rawPatternName = pagePattern.getName();
        updateContent();
        updateVars(pagePattern);      
        updateButton.setEnabled(false);
    }
    
    private void updateQuickShow(EntityPagePattern pagePattern, Routine routine) {
        //update qucik show content
        xQuickShow = new StringBuilder();
        xQuickShow.append("Quick show:").append("\n");
        String[] yNames = pagePattern.getYNames();
        String xName = pagePattern.getXName();
        String pName = pagePattern.getPName();
        String[] conditionNames = pagePattern.getConditionNames();
        FuncField xValuesPattern = pagePattern.getXValuesPattern();
        FuncField pValuesPattern = pagePattern.getPValuesPattern();
        String ref = Routine.getRef(routine.getDeviceType(), conditionNames, xName, pName);
        if(ref == null){
             xQuickShow.append("some error with ref");
             quickShowText.setText(xQuickShow.toString());
             quickShowText.setBackground(Color.red);
             return;
        }
        //x values
        MeaBias xMeaBias = Routine.getMeaBias(routine.getDeviceType(), xName, ref);
        Routine.resolveBundles(xMeaBias, xValuesPattern, getIndPagePattern(), routine.getDevice().getDevicePolarity());
        //p values
        MeaBias pMeaBias = Routine.getMeaBias(routine.getDeviceType(), pName, ref);
        Routine.resolveBundles(pMeaBias, pValuesPattern, getIndPagePattern(), routine.getDevice().getDevicePolarity());
        //build it
        xQuickShow.append("Y: ");
        for(int index =0;index<yNames.length;index++){
            xQuickShow.append(yNames[index]).append("\t");
        }
        xQuickShow.append("\n");
        boolean result1 = addBuilder("X",xMeaBias);
        boolean result2 = addBuilder("P",pMeaBias);     
        
        quickShowText.setText(xQuickShow.toString());
        if(result1&&result2){
            quickShowText.setBackground(Color.GREEN);
        }else{
            quickShowText.setBackground(Color.red);
        }
    }
    
    private boolean addBuilder(String name,MeaBias meaBias){
        if(meaBias.getBundleNumber() == 0){
            xQuickShow.append(name).append("(").append(meaBias.getName()).append(")");    
            xQuickShow.append(": error!!").append("\n");
            return false;
        }
        MeaBundle meabundle = meaBias.getBundle(0);
        xQuickShow.append(name).append("(").append(meaBias.getName()).append(")").append(": ");       
        if (meabundle instanceof SweepMeaBundle) {
            SweepMeaBundle sweep = (SweepMeaBundle)meabundle ;
//            xQuickShow.append(" start from ");
//            xQuickShow.append(String.format("%.4g", sweep.getStart()));
//            xQuickShow.append(" to ");
//            xQuickShow.append(String.format("%.4g", sweep.getStop()));
//            xQuickShow.append(" and step is  ");
//            xQuickShow.append(String.format("%.4g", sweep.getStep())).append("\n");
            double start = sweep.getStart();
            double stop = sweep.getStop();
            double step = sweep.getStep();
            int point = sweep.getPoint();
            if(Double.isNaN(start)|| Double.isNaN(stop)
                    ||Double.isNaN(step)){              
                xQuickShow.append("error!!").append("\n");
                return false;
            }else{
                //int point = (int) Math.abs((stop-start)/step)+1;
                if(point<1){
                     xQuickShow.append("point error!!").append("\n");
                    return false;
                }
                for(int index = 0;index<point;index++){
                    if(index == 3){
                        if(index!= point-1){
                             xQuickShow.append(" ...");  
                        }else{
                            String str = String.format("%.4g", start+step*index);
                            xQuickShow.append(str).append(" ");
                        }                       
                    }else if (index<3 || index == point -1){
                         String str = String.format("%.4g", start+step*index);
                         xQuickShow.append(str).append(" ");
                    }else{
                        continue;
                    }
                }
                xQuickShow.append("(").append(point).append(")");
                xQuickShow.append("\n");
                
                //duplicated data
                if (start == stop && point>1) {
                    xQuickShow.append("maybe duplicated data error!!").append("\n");
                    return false;
                }
                //limit point 
                if ((name.equalsIgnoreCase("X") && point > 5000)
                        || (name.equalsIgnoreCase("P") && point > 20)) {
                    xQuickShow.append("maybe too much point for ").append(name).append("\n");
                    return false;

                }
                
                  //FS360/4139
                if(meaBias.getName().toUpperCase().startsWith("V")){
                    if(start<-60){
                        xQuickShow.append("start limit error!").append("\n");
                        return false;
                    }
                    if(stop>60){
                        xQuickShow.append("stop limit error!").append("\n");
                        return false;
                    }
                    if(Math.abs(step)<1e-6 && point>1){
                        xQuickShow.append("step limit error!").append("\n");
                        return false;
                    }
                    
                }else if (meaBias.getName().toUpperCase().startsWith("I")){
                    if(Math.abs(start)<1e-9){
                        xQuickShow.append("start limit error!").append("\n");
                        return false;
                    }
                    if(Math.abs(stop)>100e-3){
                        xQuickShow.append("stop limit error!").append("\n");
                        return false;
                    }
                    if(Math.abs(step)<1e-9 && point>1){
                        xQuickShow.append("step limit error!").append("\n");
                        return false;
                    }
                }
                //FS360/4139    
                
            }
        }else if (meabundle instanceof ArrayMeaBundle){
            ArrayMeaBundle arrayBundle = (ArrayMeaBundle)meabundle;
            //xQuickShow.append(": ");
            for(int index =0;index< arrayBundle.size();index++){
                if(index == 10){
                   xQuickShow.append(" ...");                   
                }else if(index<10||
                        index == arrayBundle.size()-1){
                   String str = String.format("%.4g", arrayBundle.get(index));
                   xQuickShow.append(str) ;
                   xQuickShow.append(" ");  
                }else{
                   continue;
                }
                
            }
            xQuickShow.append("\n");
        }
        
        return true;
      
    }
    public void setRawPatternName(String rawPatternName){
        this.rawPatternName = rawPatternName;
    }
    
      public void setPagePattern(EntityPagePattern pagePattern,Routine routine) {
        this.pagePattern = pagePattern;
        this.rawPatternName = pagePattern.getName();
        updateContent();
        updateVars(pagePattern);      
        updateButton.setEnabled(false);
        this.routine = routine;
        updateQuickShow(pagePattern,routine);
    }

    public EntityPagePattern getPagePattern() {
        if (pagePattern == null) {
            return null;
        }
        String text = textArea.getContent();
        String[] lines = text.split("\n");
        PagePatternLoader loader = new PagePatternLoader();
        EntityPagePattern epp = loader.loadPagePattern(Arrays.asList(lines));
        epp.setDeviceType(pagePattern.getDeviceType());
        epp.setDevicePolarity(pagePattern.getDevicePolarity());
//        pagePattern.copyValueOf(epp);
        return epp;
    }

    public boolean updateVar() {
        
        if (BaseVarProvider.haveSameVars(varProvider, rawVarProvider)) {
            return false;
        }
        BaseVarProvider.copyVars(varProvider, rawVarProvider);
        return true;
    }

    public IndPagePattern getIndPagePattern() {
        if (pagePattern == null) {
            return null;
        }

        IndPagePattern indPagePattern = new IndPagePattern();
        indPagePattern.pagePattern = getPagePattern();
        updateVars(indPagePattern.pagePattern);
        List<String> varNames = varProvider.getVarNames();
        for (String var : varNames) {
            indPagePattern.setVar(var, varProvider.getVar(var));
        }
        return indPagePattern;
    }
    
    public Map<String,EntityPagePattern> getUpdatedPatterns(){
        return this.updatedPatterns;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == updateButton) {
            doUpdate();
        } else if (source == helpButton){
            //todo
        }
    }
    
    @Override
    public void afterTableModelChanged(){
        doUpdate();
    }
    @Override
    public void doUpdate() {
        EntityPagePattern pattern = getPagePattern();
        if (PatternUtil.checkPatternLegality(pattern)) {
            updateVars(pattern);
            //add to updated patterns        
            updatedPatterns.put(rawPatternName, pattern);//pattern.getName()
            //update quick show
            if (this.routine != null) {
                updateQuickShow(pattern, this.routine);
            }
            updateButton.setEnabled(pattern == null);
        }

    }
}
