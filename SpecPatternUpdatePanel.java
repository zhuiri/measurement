/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.iv.MeaOptions;
import com.platformda.iv.tools.auto.SyntaxEditor;
import com.platformda.spec.BaseSpecPattern;
import com.platformda.spec.SpecPattern;
import com.platformda.spec.SpecPatternGroup;
import com.platformda.iv.spec.SpecPatternLoader;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.common.StringBuilderAppender;
import com.platformda.utility.common.VarProvider;
import com.platformda.utility.ui.DocumentListenerStub;
import com.platformda.utility.ui.JTableUtil;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.openide.util.Exceptions;

/**
 *
 * @author Junyi
 */
public class SpecPatternUpdatePanel extends JPanel implements ActionListener {

    SpecPattern specPattern;
    VarProvider rawVarProvider;
    BaseVarProvider varProvider = new BaseVarProvider();
    SyntaxEditor textArea = new SyntaxEditor("text/specpattern");
    JButton updateButton = new JButton("Update");
    VarProviderEditorImpl varEditor;
     //all updated patterns
    Map<String,SpecPattern> updatedPatterns  = new LinkedHashMap<>();

    public SpecPatternUpdatePanel(VarProvider rawVarProvider) {
        this.rawVarProvider = rawVarProvider;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        if(rawVarProvider instanceof IndSpecPattern){
             IndSpecPattern isp = (IndSpecPattern)rawVarProvider;
             varEditor = new VarProviderEditorImpl(isp.specPattern.getName(),varProvider, null);
        }else if(rawVarProvider instanceof RoutinePattern){
              RoutinePattern routinePattern = (RoutinePattern)rawVarProvider;
              if(routinePattern.getSpecPatternNumber()!=0){
                  varEditor = new VarProviderEditorImpl(routinePattern.getSpecPattern(0).getName(),varProvider, null);
              }else{
                  varEditor = new VarProviderEditorImpl(varProvider, null);
              }
              
        }
        
        //varEditor = new VarProviderEditorImpl(varProvider, null);
       
        JTableUtil.setVisibleRowCount(varEditor.getTable(), 5);

        textArea.getEditorPane().setEditable(false);
        textArea.hideToolBar();
        textArea.getEditorPane().setSize(80, 15);
        
        Document doc = textArea.getEditorPane().getDocument();
        DocumentListenerStub stub = new DocumentListenerStub() {
            @Override
            public void validate() {
                updateButton.setEnabled(true);
            }
        };
        doc.addDocumentListener(stub);
        updateButton.addActionListener(this);
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(updateButton, BorderLayout.EAST);
     
        updateButton.setEnabled(false);
        //centerPanel.add(buttonPanel,BorderLayout.SOUTH);
        centerPanel.add(textArea, BorderLayout.CENTER);        
        add(varEditor, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        updateContent();
        //updateVars();
        setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE4);
    }

    protected void updateContent() {
        if (specPattern != null) {
            BaseSpecPattern bsp = (BaseSpecPattern) specPattern;
            StringBuilder builder = new StringBuilder();
            StringBuilderAppender appender = new StringBuilderAppender(builder);
            try {
                SpecPatternLoader.dump(bsp, appender);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            builder.append("\n\n");
            List<SpecPattern> allDepdens = bsp.getAllDepends();
            if (allDepdens != null) {
                for (SpecPattern sp : allDepdens) {
                    BaseSpecPattern dbsp = (BaseSpecPattern) sp;
                    try {
                        SpecPatternLoader.dump(dbsp, appender);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    builder.append("\n\n");
                }
            }
            textArea.setContent(builder.toString());
        }
    }

   protected void updateVars(SpecPattern specPattern) {
//        if (specPattern != null) {
//            varProvider.clearVars();
//            List<String> vars = new ArrayList();
//            specPattern.fetchSimulationVarNames(vars);
//            for (String var : vars) {
//                varProvider.setVar(var, rawVarProvider.getVar(var));
//            }
//            varEditor.updateView();
//        }
     if (specPattern != null) {
            List<String> vars = new ArrayList();
            specPattern.fetchSimulationVarNames(vars);
            
            //get old varProvider   
            varEditor.getTableModel().setPatternName(specPattern.getName());
            Map<String,VarProvider> updatedPatternVarsMap = varEditor.getTableModel().getUpdatedPatternVarsMap();
            VarProvider oldVarProvider = updatedPatternVarsMap.get(specPattern.getName());
            if(oldVarProvider == null){
                varProvider.clearVars();               
            }else{
                BaseVarProvider.copyVars(oldVarProvider, varProvider);
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

    public void setSpecPattern(SpecPattern specPattern) {
        this.specPattern = specPattern;
        updateContent();
        updateVars(specPattern);
        updateButton.setEnabled(false);
    }

    public void stopEditting() {
        // TODO:
       //SpecPattern epp = getSpecPattern();
       //specPattern = epp;
    }

    public boolean updateVar() {
        if (BaseVarProvider.haveSameVars(varProvider, rawVarProvider)) {
            return false;
        }
        BaseVarProvider.copyVars(varProvider, rawVarProvider);
        return true;
    }

    public IndSpecPattern getIndSpecPattern() {
        if (specPattern == null) {
            return null;
        }

        IndSpecPattern indSpecPattern = new IndSpecPattern();
        indSpecPattern.specPattern = getSpecPattern();//specPattern;
        updateVars(indSpecPattern.specPattern);
        List<String> varNames = varProvider.getVarNames();
        for (String var : varNames) {
            indSpecPattern.setVar(var, varProvider.getVar(var));
        }
        return indSpecPattern;
    }
    
      public Map<String,SpecPattern> getUpdatedPatterns(){
        return this.updatedPatterns;
    }
    
     public SpecPattern getSpecPattern() {
        if (specPattern == null) {
            return null;
        }
        String text = textArea.getContent();
        String[] lines = text.split("\n");
        SpecPattern epp = null;
        try{
        SpecPatternLoader loader = new SpecPatternLoader();
        SpecPatternGroup patternGroup = loader.load(specPattern.getDeviceType(), Arrays.asList(lines));
        epp = patternGroup.getSpecPattern(0);
        epp.setDeviceType(specPattern.getDeviceType());
        //epp.setDevicePolarity(specPattern.getDevicePolarity());
        }catch(Exception e){
            e.printStackTrace();
        }
       
        return epp;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == updateButton) {
            SpecPattern specPattern = getSpecPattern();
            updateVars(specPattern);
            //add to updated patterns
            updatedPatterns.put(specPattern.getName(),specPattern);
            updateButton.setEnabled(specPattern == null);
        }
    }
}
