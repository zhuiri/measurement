/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.datacore.pattern.FuncField;
import com.platformda.iv.MeaOptions;
import com.platformda.iv.analysis.ArrayMeaBundle;
import com.platformda.iv.analysis.MeaBias;
import com.platformda.iv.analysis.MeaBundle;
import com.platformda.iv.analysis.SweepMeaBundle;
import com.platformda.iv.api.NodeBond;
import com.platformda.iv.datacore.DeviceTypeManager;
import com.platformda.iv.dialog.TitledSeparator;
import com.platformda.iv.help.StringHelper;
import com.platformda.iv.measure.DeviceBond;
import com.platformda.iv.tools.PatternUtil;
import com.platformda.iv.tools.SettingManager;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.ui.DocumentListenerStub;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author renjing
 */
public class PagePatternUpdateUI extends AbstractPagePanel implements ActionListener{
    
    private static final String FUN = "Sweep";
    private static final String CONSTANT = "Constant";
    private static final String VOLTAGE = "V";
    private static final String CURRENT = "I";
    private static final String AMPERE = "A";
    private static final String X = "X";
    private static final String P = "P ";
    private static final String FROM = "From";
    private static final String TO = "To";
    private static final String REFERENCE = "Ref";
    public static final String EXPRESSION = "expression";
    
    private static final Dimension PREFERRED_SIZE = new Dimension(80,5);
    private static final Dimension HIDDEN_SIZE = new Dimension(0,0);
    private static final int PREDERRED_TEXTSIZE = 13;
    
    public static final String ORIGIN_LIMIT = "0.1";
    
    public static final String VOLTAGE_ORIGIN_LIMIT = "1";
    private static final String CURRENT_LIMIT_360 = "3";
    private static final String CURRENT_LIMIT_380 = "1";
    
    
    String rawPatternName;
    EntityPagePattern pagePattern;
    EntityPagePattern updatePagePattern;
    
    Routine routine;
//    RoutinePattern routinePattern;
    
    LinkedHashMap<String,JPanel> bondsMap;
    LinkedHashMap<String,JComboBox> funMap;
    LinkedHashMap<String,JComboBox> modeMap;
    LinkedHashMap<String,JLabel> termMap;
//    LinkedHashMap<String,JComboBox> nodesMap;
    LinkedHashMap<String,String> limitMap;
    List<String> nodeNames;
    
    JPanel mainPanel;
    JPanel northPanel;
    JPanel southPanel;
    private JComboBox xFromCombo,xToCombo,pFromCombo,pToCombo,refCombo;
    private String refNode;
    private String refBackup;
    private JTextField pageNameText,yText,expressionText;
    private List<String> conditionNodes;
    private String errorInfo;
    private String leftNode,rightNode;
//    private JLabel errorLabel;
    //all updated patterns
    private Map<String, EntityPagePattern> updatedPatterns = new LinkedHashMap<>();
    
    private String fastType;
    private boolean isDoUpdate = false;
    private boolean hasExpression = false;
    JButton updateButton = new JButton("Update");
    
    LinkedHashMap<String,DocumentListenerStub1> stub1Map;
    LinkedHashMap<String,DocumentListenerStub2> stub2Map;
    LinkedHashMap<String,DocumentListenerStub3> stub3Map;
    
    
//    public PagePatternUpdateUI(EntityPagePattern pagePattern,Routine routine){
//        isDoUpdate = false;
//        this.routine = routine;
//        this.pagePattern = pagePattern;
//        this.updatePagePattern = new EntityPagePattern();
//        updatePagePattern.copyValueOf(pagePattern);
//        initComponent();
//        loadPagePattern();
//    }
    
    public PagePatternUpdateUI(){//RoutinePattern routinePattern
//        this.routinePattern = routinePattern;
//        this.routine = routine;     
          fastType = SettingManager.getInsance().readValue("FSType");
    }
    
    private String getFullTermName(String nodeName,NodeBond[] bonds){
        StringBuilder builder = new StringBuilder();
        for(int index =0;index<bonds.length;index++){
           if(bonds[index].getNodeName().equalsIgnoreCase(nodeName)){
               builder.append(bonds[index].getUnit().getName());
               builder.append(" ");
           }
        }
        
        return builder.toString();
    }
    
    private void initComponent() {
        setLayout(new BorderLayout());
//        northPanel = new JPanel(new MigLayout("wrap 4","[][]40[][]")); 
//        southPanel = new JPanel(new MigLayout("wrap 4","[][]25[][]"));
        if(routine.getDeviceType().getName().equals(DeviceTypeManager.BJT)){
            
            mainPanel = new JPanel(new MigLayout("wrap 4 ","[][]25[]25[grow]"));
        }else{
            mainPanel = new JPanel(new MigLayout("wrap 4 ","[][]25[]25[grow]"));
        }  
        
           //pageName and y
         mainPanel.add(new JLabel("PageName :"));
         pageNameText = new JTextField(); 
         pageNameText.setColumns(2*PREDERRED_TEXTSIZE);
         mainPanel.add(pageNameText,"span,grow");
//         northPanel.add(new JLabel("Y :"));
         yText = new JTextField(); 
         yText.setColumns(2*PREDERRED_TEXTSIZE);
//         northPanel.add(yText);     
         DocumentListenerStub stub = new DocumentListenerStub() {
            @Override
            public void validate() {
                doUpdate();
            }
         };
         pageNameText.getDocument().addDocumentListener(stub);
         yText.getDocument().addDocumentListener(stub);
       
       
       
        DeviceBond deviceBond = routine.getDeviceBond(pagePattern);
        NodeBond[] bonds = deviceBond.getBonds();
        bondsMap = new LinkedHashMap<>();
        funMap = new LinkedHashMap<>();
        modeMap = new LinkedHashMap<>();
        termMap = new LinkedHashMap<>();
        nodeNames = new LinkedList<>();
        limitMap = new LinkedHashMap<>();
        for (int index = 0; index < bonds.length; index++) {
            NodeBond bond = bonds[index];
            String nodeName = bond.getNodeName();
            if (!nodeNames.contains(nodeName)) {
                nodeNames.add(nodeName);
                //fun combo
                JComboBox funCombo = getFunComboBox(nodeName);
                //set default = null
                //to allow to invoke the value changed
                funCombo.setSelectedItem(null);
                funMap.put(nodeName, funCombo);
                //mode combo
                JComboBox modeCombo = getModeComboBox(nodeName);
                modeCombo.setSelectedItem(VOLTAGE);
                modeMap.put(nodeName, modeCombo);
                //fun panel
                JPanel panel = getFunPanel(FUN);
                bondsMap.put(nodeName, panel);

                //limit map
                limitMap.put(VOLTAGE + nodeName, VOLTAGE_ORIGIN_LIMIT);
                limitMap.put(CURRENT + nodeName, ORIGIN_LIMIT);
                //add bond panel
                //maybe connectted to more than one terminal
//                String termName = getFullTermName(nodeName,bonds);
//                StringBuilder builder = new StringBuilder();
//                builder.append(nodeName).append("(").append(termName).append(")");
                JLabel label = new JLabel(" :");
                termMap.put(nodeName, label);
                mainPanel.add(label);
                mainPanel.add(modeCombo);
                mainPanel.add(funCombo);
                mainPanel.add(bondsMap.get(nodeName));
            }         
           
        }
        //add expression 
        mainPanel.add(new JLabel("Expression :"));
        expressionText = new JTextField();
        mainPanel.add(expressionText,"span,grow");
        
        
        
        //south 
        mainPanel.add(new TitledSeparator("Setup Y/X/P value"), "span,grow,gaptop 10");
        mainPanel.add(new JLabel("Y :"));
        mainPanel.add(yText,"span,grow");        
        //add X,P 
        mainPanel.add(new JLabel("X :"));
        JComboBox xmodeCombo = getModeComboBox(X);
        xmodeCombo.setSelectedItem(VOLTAGE);
//        xmodeCombo.setEnabled(false);
        modeMap.put(X, xmodeCombo);
        mainPanel.add(xmodeCombo);
        xFromCombo = getNodeComboBox(X+FROM);
        mainPanel.add(xFromCombo);
        xToCombo = getNodeComboBox(X+TO);
        mainPanel.add(xToCombo);
      

        mainPanel.add(new JLabel("P :"));
        JComboBox pmodeCombo = getModeComboBox(P);
        pmodeCombo.setSelectedItem(VOLTAGE);
        modeMap.put(P, pmodeCombo);
        mainPanel.add(pmodeCombo);
        pFromCombo = getNodeComboBox(P+FROM);
        mainPanel.add(pFromCombo);
        pToCombo = getNodeComboBox(P+TO);
        mainPanel.add(pToCombo);
        
        xFromCombo.setSelectedItem(null);
        xToCombo.setSelectedItem(null);
        pFromCombo.setSelectedItem(null);
        pToCombo.setSelectedItem(null);
        

        //ref
//        mainPanel.add(new JLabel("Ref :"));
        refCombo = getNodeComboBox(REFERENCE);
//        mainPanel.add(refCombo);   
        
        updateButton.addActionListener(this);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(updateButton, BorderLayout.EAST);
        updateButton.setEnabled(false);
        
//        add(buttonPanel,BorderLayout.EAST);
//        add(northPanel,BorderLayout.NORTH);
        add(new JScrollPane(mainPanel),BorderLayout.CENTER);
//        add(southPanel,BorderLayout.SOUTH);
//        errorLabel = new JLabel("");
//        add(errorLabel,BorderLayout.SOUTH);
        if(routine.getDeviceType().getName().equalsIgnoreCase(DeviceTypeManager.MOSFET)){
           setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE10); 
        }else if (routine.getDeviceType().getName().equalsIgnoreCase(DeviceTypeManager.BJT)) {
           setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE11);
        }else {
           setPreferredSize(MeaOptions.COMPONENT_PREFERRED_SIZE12);
        }
        

    }
    
    @Override
    public String getErrorInfo() {     
        errorInfo = checkLegality();
        if (errorInfo.isEmpty()) {
            errorInfo = null;
        }
        return errorInfo;
    }
    
    @Override
    public Map<String, EntityPagePattern> getUpdatedPatterns() {
        return this.updatedPatterns;
    }

    
    @Override
    public EntityPagePattern getPagePattern(){
        if(pagePattern == null){
            return null;
        }
//        if(!savePagePattern()){
//            return pagePattern;
//        }    
        savePagePattern();
        return updatePagePattern;
        
    }
    
    private String[] uninstallValue(String nodeName,String type){
        if(type == null || nodeName == null){
            return null;
        }
        JPanel panel = bondsMap.get(nodeName);
        if(panel!=null){
            if(type.equalsIgnoreCase(FUN)){
                String[] values = new String[3];
//                for(int index =0;index< panel.getComponentCount()-2;index++){
//                    int actualIndex = index / 2;
//                    if(index % 2 !=0){
//                         JTextField textArea = (JTextField)panel.getComponent(index);
//                         values[actualIndex] = textArea.getText().trim();
//                    }                   
//                }
                for (int index = panel.getComponentCount()/2; index < panel.getComponentCount()/2+3; index++) {
                    int actualIndex = index - panel.getComponentCount()/2;
                    JTextField textArea = (JTextField) panel.getComponent(index);
                    values[actualIndex] = textArea.getText().trim();
                    
                }
                return values;
                
                
            }else if (type.equalsIgnoreCase(CONSTANT)||type.equalsIgnoreCase(REFERENCE)){
//                JTextField textArea = (JTextField)panel.getComponent(panel.getComponentCount()-1);
                JTextField textArea = (JTextField)panel.getComponent(panel.getComponentCount()-3);
                return new String[]{textArea.getText().trim()};
            }
            
        }
        
        return null;
    }
    
    private boolean savePagePattern(){
        //uninstall page name and y names
        updatePagePattern.setName(pageNameText.getText().trim());
        String[] yNames ;
        String text = yText.getText().trim();
        if(text.contains(",")){
          yNames = text.split(",");
        }else if (text.contains(";")){
          yNames = text.split(";"); 
        }else{
          yNames = text.split(" "); 
        }
        
        updatePagePattern.setYNames(yNames);
        
        //get ref node
       setRefNodeFromPanel();
//        String ref = (String)refCombo.getSelectedItem();       
//        if(ref!=null){
//            this.refNode = ref;           
//        }
        String refValue="0";
        //uninstall ref node 
        int conditionNum = updatePagePattern.getConditionNames().length;
        if (!refNode.equalsIgnoreCase(refBackup)) {
            //remove ref condition
            for (int index = conditionNum - 1; index >=0; index--) {
                String conditionName = updatePagePattern.getConditionNames()[index];
                if (conditionName.startsWith("ref")) {
                    updatePagePattern.setCondition(conditionName, null);
                } 
            }
            
            refValue = uninstallValue(refNode, REFERENCE)[0];
            String refFun = (String) funMap.get(refNode).getSelectedItem();
            String refConditionName = "ref_v" + refNode.toLowerCase();
            assert (refFun.equalsIgnoreCase(REFERENCE));
            FuncField condtionField = new FuncField(refValue);
            updatePagePattern.setCondition(refConditionName, condtionField);
            
        }
        
       
        //remove limit condition
        for (int index = conditionNum - 1; index >= 0; index--) {
            String conditionName = updatePagePattern.getConditionNames()[index];
            if (conditionName.startsWith("limit")) {
                updatePagePattern.setCondition(conditionName, null);
            }
        }
         //uninstall limit condition
        for(int index =0; index<nodeNames.size();index++){
            String nodeName = nodeNames.get(index).toUpperCase();
            JPanel panel = bondsMap.get(nodeName);
            String limitValue = String.format("%.4g", new Double(uninstallLimitValue(nodeName)));
            String limitConditionName = "limit" + nodeName;
            FuncField condtionField = new FuncField(limitValue);
            updatePagePattern.setCondition(limitConditionName, condtionField);
        }

      
      
        
       
        
        //String[] yNames = pagePattern.getYNames();
//        String xName = pagePattern.getXName();
//        String pName = pagePattern.getPName();
//        String[] conditionNames = pagePattern.getConditionNames();
//        FuncField xValuesPattern = pagePattern.getXValuesPattern();
//        FuncField pValuesPattern = pagePattern.getPValuesPattern();
        
        //set xName,PName
        StringBuilder builder = new StringBuilder();
        String xMode = (String)modeMap.get(X).getSelectedItem();
        builder.append(xMode);       
        String xFrom = (String)xFromCombo.getSelectedItem();
        String xTo = (String) xToCombo.getSelectedItem();
        assert(xFrom!=null&&!xFrom.isEmpty());
        builder.append(xFrom.toLowerCase());
        if (xMode.equalsIgnoreCase(VOLTAGE)) {           
            assert (xTo != null && !xTo.isEmpty());
            builder.append(xTo.toLowerCase());
        }       
        String xName = builder.toString();
        updatePagePattern.setXName(xName);
        
        String pFrom = (String)pFromCombo.getSelectedItem();
        String pTo = (String)pToCombo.getSelectedItem();
        String pName = updatePagePattern.getPName();
        
        if(pFrom == null){
             //set nop = 0;
            //acutally the original one is it
        }else if (pFrom != null || pTo != null) {
            builder = new StringBuilder();
            builder.append((String) modeMap.get(P).getSelectedItem());
            assert (pFrom != null && !pFrom.isEmpty());
            builder.append(pFrom.toLowerCase());
            if (pTo != null && !pTo.isEmpty()) {
                builder.append(pTo.toLowerCase());
            }
            pName = builder.toString();
            updatePagePattern.setPName(pName);
        }
        
        
     
        
        //uninstall value
        if(xName.toUpperCase().startsWith("V")){
            if(xTo.equalsIgnoreCase(refNode)){
                //uninstall xFrom node
                String xType = (String)funMap.get(xFrom).getSelectedItem();
                String[] xValuesFrom = uninstallValue(xFrom,xType);
                //assert ref value is constant              
                FuncField xValuesPattern = afterunInstallValue(xValuesFrom,refValue);  
                if(xValuesPattern!=null){
                   updatePagePattern.setXValuesPattern(xValuesPattern);
                }              
                
            }else{
                //todo when stop node is not ref node
            }
        }else if (xName.toUpperCase().startsWith("I")){
            //todo
            //uninstall xFrom node
            String xType = (String) funMap.get(xFrom).getSelectedItem();
            String[] xValuesFrom = uninstallValue(xFrom, xType);
            FuncField xValuesPattern = afterunInstallValue(xValuesFrom, refValue);
            if (xValuesPattern != null) {
                updatePagePattern.setXValuesPattern(xValuesPattern);
            } 
        }
        
        if (pName.toUpperCase().startsWith("V")) {
            if (pTo.equalsIgnoreCase(refNode)) {
                //uninstall pFrom node
                String pType = (String) funMap.get(pFrom).getSelectedItem();
                String[] pValuesFrom = uninstallValue(pFrom, pType);
                FuncField pValuesPattern = afterunInstallValue(pValuesFrom, refValue);
                if (pValuesPattern != null) {
                    updatePagePattern.setPValuesPattern(pValuesPattern);
                }
            } else {
                //todo
                String pType = (String) funMap.get(pFrom).getSelectedItem();
                String[] pValuesFrom = uninstallValue(pFrom, pType);
                pType = (String) funMap.get(pTo).getSelectedItem();
                String[] pValuesTo = uninstallValue(pTo, pType);
                FuncField pValuesPattern = afterunInstallValue(pValuesFrom, pValuesTo);
                if (pValuesPattern != null) {
                    updatePagePattern.setPValuesPattern(pValuesPattern);
                }
                
                
                
                
            }
        } else if (pName.toUpperCase().startsWith("I")) {
            //todo
            String nodeName = pName.substring(1).toUpperCase();
            String mode =(String) modeMap.get(nodeName).getSelectedItem();
            assert(mode.equals(CURRENT));
            String pType = (String) funMap.get(pFrom).getSelectedItem();
            String[] pValuesFrom = uninstallValue(pFrom, pType);
            assert(refValue.equalsIgnoreCase("0"));
            FuncField pValuesPattern = afterunInstallValue(pValuesFrom, refValue);
            if (pValuesPattern != null) {
                updatePagePattern.setPValuesPattern(pValuesPattern);
            }
            
                    
        }
        
        
         //finllay uninstall other conditions       
        //get all the nodes except x,p related ones
        conditionNodes = new ArrayList<>();
        conditionNodes.addAll(nodeNames);
        if (xFrom != null) {
            conditionNodes.remove(xFrom);
        }
        if (xTo != null) {
            conditionNodes.remove(xTo);
        }
        if (pFrom != null) {
            conditionNodes.remove(pFrom);
        }
        if (pTo != null) {
            conditionNodes.remove(pTo);
        }
        //remove unuse condtions
        //should add remove method in EntityPagePattern
        for (int index = conditionNum-1; index >=0; index--) {
            String conditionName = updatePagePattern.getConditionNames()[index];
            if (conditionName.startsWith("ref")||conditionName.startsWith("limit")) {
                continue;
            } else {
                //remove it
                updatePagePattern.setCondition(conditionName, null);
            }
        }
        //assert condition start node
        //stop node is ref node 
        for(int index = 0; index<conditionNodes.size();index++){
            String conditionNode = conditionNodes.get(index).toUpperCase();
            String mode = (String)modeMap.get(conditionNode).getSelectedItem();
            builder = new StringBuilder();   
            if (mode.equalsIgnoreCase(VOLTAGE)) {
                //assert v condition
                builder.append("v").append(conditionNode.toLowerCase()).append(refNode.toLowerCase());
            } else if (mode.equalsIgnoreCase(CURRENT)) {
                //assert v condition
                builder.append("i").append(conditionNode.toLowerCase());
            }           
            String conditionName = builder.toString();
            String fun = (String) funMap.get(conditionNode).getSelectedItem();
            if (fun != null
                    && (fun.equalsIgnoreCase(FUN) 
                    || fun.equalsIgnoreCase(CONSTANT)
                    || fun.equalsIgnoreCase(REFERENCE))) {
                String[] valuesFrom = uninstallValue(conditionNode, fun);
                FuncField condtionField = afterunInstallValue(valuesFrom, refValue);
                updatePagePattern.setCondition(conditionName, condtionField);
            }
            
        }      
        
        //add expression
        if(hasExpression){
            updatePagePattern.setCondition(EXPRESSION, new FuncField(expressionText.getText().trim()));
        }
        
//        for (int index = 0; index < conditionNum; index++) {
//            String conditionName = updatePagePattern.getConditionNames()[index];
//            if (conditionName.startsWith("ref")) {
//                continue;
//            } else {                          
//                
//                if(conditionName.toUpperCase().startsWith("V")){
//                    String nodeName = conditionName.substring(1,2).toUpperCase();
//                    String fun = (String) funMap.get(nodeName).getSelectedItem();
//                    if (fun != null
//                            && (fun.equalsIgnoreCase(FUN) || fun.equalsIgnoreCase(CONSTANT))) {
//                        String[] valuesFrom = uninstallValue(nodeName, fun);
//                        FuncField condtionField = afterunInstallValue(valuesFrom, refValue);
//                        updatePagePattern.setCondition(conditionName, condtionField);
//                    }                  
////                    if (fun.equalsIgnoreCase(CONSTANT)) {
////                        assert (fun.equalsIgnoreCase(CONSTANT));
////                        String[] valuesFrom = uninstallValue(nodeName, CONSTANT);
////                        if (valuesFrom.length == 1) {
////                            FuncField condtionField = afterunInstallValue(valuesFrom, refValue);
////                            updatePagePattern.setCondition(conditionName, condtionField);
////
////                        } else {
////                            //todo
////                        }
////                    } else if (fun.equalsIgnoreCase(FUN)) {
////                        assert (fun.equalsIgnoreCase(FUN));
////                        String[] valuesFrom = uninstallValue(nodeName, FUN);
////                        FuncField condtionField = afterunInstallValue(valuesFrom, refValue);
////                        updatePagePattern.setCondition(conditionName, condtionField);
////                    }
//                   
//                    
//                }else{
//                    //todo start with I
//                }
//               
//
//            }
//        }  
        
        //check legality
        errorInfo = checkLegality();
        if(errorInfo!=null&&!errorInfo.isEmpty()){
//            errorLabel.setForeground(Color.red);
//            errorLabel.setText(errorInfo);
            return false;
        }      
        
        
        return true;
        
    }
    
    
    private String getOutPutType(String[] yNames,String nodeName){
        for(int index =0;index<yNames.length;index++){
            if(yNames[index].toLowerCase().contains(nodeName.toLowerCase())){
                if(yNames[index].startsWith("I")){
                    return CURRENT;
                }else if(yNames[index].startsWith("V")){
                    return VOLTAGE;
                }
            }
        }        
        return null;        
    }
    
    private String checkLegality(){
         //get expression error
        parseExpression();
        if(!errorInfo.trim().isEmpty()){
            return errorInfo;
        }
        
        
        StringBuilder builder = new StringBuilder();
        String[] yNames ;
        String text = yText.getText().trim();
        if(text.contains(",")){
          yNames = text.split(",");
        }else if (text.contains(";")){
          yNames = text.split(";"); 
        }else{
          yNames = text.split(" "); 
        }
        
        //check ynames beta,betar
        for(int index =0;index<yNames.length;index++){
            String yName = yNames[index];
            if(yName.equalsIgnoreCase("beta")||yName.equalsIgnoreCase("betar")){
                if (index != 2) {
                    builder.append("some error with ynames!");
                    builder.append("(eg.Ic,Ib,Beta)");
                    builder.append("\n");
                    return builder.toString();
                }
            }
        }
        
        //builder.append("error:").append("\n");                
        String pFrom = (String)pFromCombo.getSelectedItem();
        String pTo = (String)pToCombo.getSelectedItem();
        String xFrom = (String)xFromCombo.getSelectedItem();
        String xTo = (String)xToCombo.getSelectedItem(); 
        String pMode = (String)modeMap.get(P).getSelectedItem();
        
        int funNum = 0;
        
        
        //check leftNode,rightNode mode same
        if(leftNode!=null&&rightNode!=null){
            if(pMode.equalsIgnoreCase(VOLTAGE)){
                if(!pFrom.equalsIgnoreCase(leftNode)
                        ||!pTo.equalsIgnoreCase(rightNode)){                   
                    builder.append("the nodes in expression should be exactly same as ");
                    builder.append("P's nodes!");
                    builder.append("\n");
                    return builder.toString();
                }
            }
            String leftMode = (String)modeMap.get(leftNode).getSelectedItem();
            String rightMode = (String)modeMap.get(rightNode).getSelectedItem();
            if(!leftMode.equalsIgnoreCase(rightMode)){
                builder.append("node ").append(leftNode);
                builder.append("'s mode should be same as ");
                builder.append("node ").append(rightNode).append("'s mode!");
                builder.append("\n");
                return builder.toString();
            }
        }
        
        //ref fun -combo check for UI
        boolean hasRef = false; 
        int refNum = 0;
        for(int index =0;index<nodeNames.size();index++){
            String nodeName = nodeNames.get(index).toUpperCase();
            String fun = (String)funMap.get(nodeName).getSelectedItem();
            String mode = (String)modeMap.get(nodeName).getSelectedItem();
            //fun combo empty check
            if(fun == null){
                builder.append("node ").append(nodeName);
                builder.append(" should set fun mode!");
                builder.append("\n");
                return builder.toString();
            }
            if(fun!=null&&fun.equalsIgnoreCase(REFERENCE)){
                hasRef = true;
                this.refNode = nodeName;
                refNum++;
                if(!mode.equalsIgnoreCase(VOLTAGE)){                   
                    builder.append("ref node should be defined as voltage mode!");
                    builder.append("\n");
                    return builder.toString();
                }
            }
        }
        if(!hasRef){
            builder.append("should set ref node!");
            builder.append("\n");
            return builder.toString();
        }
        if(refNum>1){
            builder.append("only one ref node can be set!");
            builder.append("\n");
            return builder.toString();
        }
        
        //node panel set double check
        for(int index=0;index<nodeNames.size();index++){
            String nodeName = nodeNames.get(index).toUpperCase();
            String mode = (String) modeMap.get(nodeName).getSelectedItem();
            String fun = (String)funMap.get(nodeName).getSelectedItem();
            //output limit double check
            if (!StringHelper.isDouble(uninstallLimitValue(nodeName))) {
                builder.append("node ").append(nodeName).append(" output limit value set error!");
                builder.append("\n");
                return builder.toString();
            }
            //output limit check
//            String outputType = getOutPutType(yNames,nodeName);
            if (mode != null) {
                Double limitValue = new Double(uninstallLimitValue(nodeName));
                if (mode.equalsIgnoreCase(VOLTAGE)) {
                    if (limitValue < 1e-8) {
                        builder.append("node ").append(nodeName).append(" output current limit value too samll!(limit is 10nA)");
                        builder.append("\n");
                        return builder.toString();
                    }
                } else if (mode.equalsIgnoreCase(CURRENT)) {
                    if (limitValue < 1e-2) {
                        builder.append("node ").append(nodeName).append(" output voltage limit value too samll!(limit is 10mV)");
                        builder.append("\n");
                        return builder.toString();
                    }

                }
            }
            
            if (fun != null) {
                String[] values = uninstallValue(nodeName, fun);
                for (int i = 0; i < values.length; i++) {
                    if (!StringHelper.isDouble(values[i])) {
                        builder.append("node ").append(nodeName).append(" value set error!");
                        builder.append("\n");
                        return builder.toString();
                    }
                }
                
                //check step !=0
                if (fun.equalsIgnoreCase(FUN)) {
                    //ensure fun node is p from or x from node
                    boolean allowFun = false;
                    if((xFrom!=null && xFrom.equalsIgnoreCase(nodeName))
                            ||(pFrom!=null && pFrom.equalsIgnoreCase(nodeName))){
                        allowFun = true;
                    }
                    
                    if(hasExpression){
                        if(pTo!=null&&pTo.equalsIgnoreCase(nodeName)){
                              allowFun = true;
                        }
                    }
                    
                    if (!allowFun) {
                        builder.append("only P/X node can be defined as sweep!");
                        builder.append("\n");
                        return builder.toString();
                    }
                    
                    
                    funNum++;
                    if (StringHelper.isDouble(values[2])) {
                        Double d = new Double(values[2]);
                        if (d == 0) {
                            builder.append("node ").append(nodeName).append("'s step value should not be zero!");
                            builder.append("\n");
                            return builder.toString();
                        }
                    }
                    
                    //check point
                    String point = uninstallPointValue(nodeName);
                    if (point.isEmpty()) {
                        builder.append("node ").append(nodeName).append("'s point value should not be empty!");
                        builder.append("\n");
                        return builder.toString();
                    }else{
                        int pointNum = new Integer(point);
                        if (pointNum <= 0) {
                            builder.append("node ").append(nodeName).append("'s point value should be positive!");
                            builder.append("\n");
                            return builder.toString();
                        }
                    }
                    
                }

                //current/voltage input limit               
                if (fastType == null) {
                    fastType = "FS360";
                }
                if (fastType.equalsIgnoreCase("FS360")) {
                    if (mode != null) {
                        if (mode.equalsIgnoreCase(VOLTAGE)) {
                            Double value = new Double(values[0]);
                            if (Math.abs(value) > 60) {
                                builder.append("node ").append(nodeName).append(" voltage limit error!");
                                builder.append("\n");
                                return builder.toString();
                            }
                            if (values.length > 1) {
                                value = new Double(values[1]);
                                if (Math.abs(value) > 60) {
                                    builder.append("node ").append(nodeName).append(" voltage limit error!");
                                    builder.append("\n");
                                    return builder.toString();
                                }
                            }
                        } else if (mode.equalsIgnoreCase(CURRENT)) {
                            Double value = new Double(values[0]);
                            if (Math.abs(value) > 3) {
                                builder.append("node ").append(nodeName).append(" current limit error!");
                                builder.append("\n");
                                return builder.toString();
                            }
                            if (values.length > 1) {
                                value = new Double(values[1]);
                                if (Math.abs(value) > 3) {
                                    builder.append("node ").append(nodeName).append(" current limit error!");
                                    builder.append("\n");
                                    return builder.toString();
                                }
                            }
                        }
                    }
                } else if (fastType.equalsIgnoreCase("FS380")) {
                    if (mode != null) {
                        if (mode.equalsIgnoreCase(VOLTAGE)) {
                            Double value = new Double(values[0]);
                            if (Math.abs(value) > 20) {
                                builder.append("node ").append(nodeName).append(" voltage limit error!");
                                builder.append("\n");
                                return builder.toString();
                            }
                            if (values.length > 1) {
                                value = new Double(values[1]);
                                if (Math.abs(value) > 20) {
                                    builder.append("node ").append(nodeName).append(" voltage limit error!");
                                    builder.append("\n");
                                    return builder.toString();
                                }
                            }
                        } else if (mode.equalsIgnoreCase(CURRENT)) {
                            Double value = new Double(values[0]);
                            if (Math.abs(value) > 1) {
                                builder.append("node ").append(nodeName).append(" current limit error!");
                                builder.append("\n");
                                return builder.toString();
                            }
                            if (values.length > 1) {
                                value = new Double(values[1]);
                                if (Math.abs(value) > 1) {
                                    builder.append("node ").append(nodeName).append(" current limit error!");
                                    builder.append("\n");
                                    return builder.toString();
                                }
                            }
                        }
                    }
                }

            }           
           
           
        }
        if(funNum>2){
            builder.append("the number of sweep should be no more than two!");
            builder.append("\n");
            return builder.toString();
        }
        
        
        //P from node and to node
        if(pFrom!=null){
            String pmode = (String)modeMap.get(P).getSelectedItem();
            //check pFrom node mode same
            if(!pmode.equalsIgnoreCase((String)modeMap.get(pFrom).getSelectedItem())){
                builder.append("node ").append(pFrom);
                builder.append(" should not have diff modes!");
                builder.append("\n");
                return builder.toString();
            }
            
//            pmode.equalsIgnoreCase(CURRENT)
            if(pFrom.equalsIgnoreCase(refNode)){
                builder.append("p from node should not be same as the ref node!");
                builder.append("\n");
                return builder.toString();
            }
        }
        
        if (pFrom != null && pTo != null) {
            String pFromFun = (String) funMap.get(pFrom).getSelectedItem();
            String pToFun = (String) funMap.get(pTo).getSelectedItem();
            int point1 = new Integer(uninstallPointValue(pFrom));
            int point2 = new Integer(uninstallPointValue(pTo));
            //point limit
            if (!hasExpression&&point1 > 50 ) {//|| point2 > 20
                builder.append("maybe too much point for ").append("P(limit is 50)");
                builder.append("\n");
                return builder.toString();
            }
            if (pFromFun.equalsIgnoreCase(FUN) && pToFun.equalsIgnoreCase(FUN)) {
                JTextField fromStep = (JTextField) bondsMap.get(pFrom).getComponent(8);
                JTextField toStep = (JTextField) bondsMap.get(pTo).getComponent(8);
                Double d1 = new Double(fromStep.getText().trim());
                Double d2 = new Double(toStep.getText().trim());
//                if (Double.compare(d1, d2) != 0) {
//                    builder.append("step of node ").append(pFrom)
//                            .append(" should  be same as the to node ").append(pTo).append(" !");
//                    builder.append("\n");
//                    return builder.toString();
//                }

//                if (point1 != point2) {
//                    builder.append("points of node ").append(pFrom)
//                            .append(" should  be same as the to node ").append(pTo).append(" !");
//                    builder.append("\n");
//                    return builder.toString();
//                }

            }
          
        }
        if (pFrom != null && pTo != null
                &&pFrom.equalsIgnoreCase(pTo)) {
            builder.append("p from node should not be same as the to node!");
            builder.append("\n");
            return builder.toString();
        }
        
        //check xFrom node mode same
        if (xFrom != null) {
            String xmode = (String) modeMap.get(X).getSelectedItem();
            if (!xmode.equalsIgnoreCase((String) modeMap.get(xFrom).getSelectedItem())) {
                builder.append("node ").append(xFrom);
                builder.append(" should not have diff modes!");
                builder.append("\n");
                return builder.toString();
            }
        }
        if (xFrom != null && xTo != null) {
            //point limit
            Integer pointNum = new Integer(uninstallPointValue(xFrom));
            if(pointNum>5000){
                builder.append("maybe too much point for ").append("X(limit is 5000)");
                builder.append("\n");
                return builder.toString();
            }
            
            if (xFrom.equalsIgnoreCase(refNode)) {
                builder.append("x from node should not be same as the ref node!");
                builder.append("\n");
                return builder.toString();
            }
            
            if (xFrom.equalsIgnoreCase(xTo)) {
                builder.append("x from node should not be same as the to node!");
                builder.append("\n");
            }else if (!xTo.equalsIgnoreCase(refNode)){
                builder.append("x to node should  be same as the ref node!");
                builder.append("\n");
            }
           
        } 
        
         
        return builder.toString();
        
    }
    
    private String uninstallPointValue(String nodeName){
        JPanel panel = bondsMap.get(nodeName);
        JTextField text = (JTextField)panel.getComponent(panel.getComponentCount()-4);
        return text.getText().trim();
    }
    
    private String uninstallLimitValue(String nodeName){
        JPanel panel = bondsMap.get(nodeName);
        JTextField text = (JTextField)panel.getComponent(panel.getComponentCount()-2);
        return text.getText().trim();
    }
    
    private void installLimitValue(String nodeName,String value){
        JPanel panel = bondsMap.get(nodeName);
        JTextField text = (JTextField) panel.getComponent(panel.getComponentCount() - 2);
        text.setText(value);
    }
    
    private FuncField afterunInstallValue(String[]valuesFrom,String ...values){
         StringBuilder builder = new StringBuilder();
        if (valuesFrom.length == 1 && values.length == 1) {
//            builder.append("linSweep(");
//            Double value = new Double(valuesFrom[0]) - new Double(refValue);
//            String str = String.format("%.4g", value);
//            builder.append(str).append(",").append(str).append(",").append(str);
//            builder.append(")");
              Double value = new Double(valuesFrom[0]) - new Double(values[0]);
              String str = String.format("%.4g", value);
              builder.append(str);
        } else if(valuesFrom.length>1 && values.length == 1){
            builder.append("linSweep(");
            for (int index = 0; index < valuesFrom.length - 1; index++) {
                Double value = new Double(valuesFrom[index]) - new Double(values[0]);
                String str = String.format("%.4g", value);
                builder.append(str).append(",");
            }
            Double value = new Double(valuesFrom[valuesFrom.length - 1]);
            String str = String.format("%.4g", value);
            builder.append(str).append(")");

        }else if (valuesFrom.length>1 && values.length >1){
//            assert(valuesFrom[2].equals(values[2]));
            Double start= new Double(valuesFrom[0]) - new Double(values[0]);
            Double stop = new Double(valuesFrom[1]) - new Double(values[1]);
            if(Math.abs(start-stop)<1e-6){
              String str = String.format("%.4g", start);
              builder.append(str);
            }else{
                builder.append("linSweep(");
                builder.append(String.format("%.4g", start)).append(",");
                builder.append(String.format("%.4g", stop)).append(",");
                Double value = new Double(valuesFrom[valuesFrom.length - 1]);
                String step = String.format("%.4g", value);
                builder.append(step).append(")");
            }
        }else if(valuesFrom.length == 1 && values.length > 1){
            builder.append("linSweep(");         
            for (int index = 0; index < values.length - 1; index++) {
                Double value = new Double(valuesFrom[0]) - new Double(values[index]);
                String str = String.format("%.4g", value);
                builder.append(str).append(",");
            }
            Double value = (-1)*new Double(values[values.length - 1]);
            String step = String.format("%.4g", value);
            builder.append(step).append(")");
        }
        //todo when input stop is ref node
        
        FuncField valuesPattern = new FuncField(builder.toString());
        return valuesPattern;
        
    }
    
    private void loadPagePattern(){
        //load term names
        for(int index =0;index<nodeNames.size();index++){
            String nodeName = nodeNames.get(index);
              String termName = getFullTermName(nodeName, routine.getDeviceBond(pagePattern).getBonds());
                StringBuilder builder = new StringBuilder();
                builder.append(nodeName).append("(").append(termName).append(")");
                JLabel label = termMap.get(nodeName);
                label.setText(builder.toString() + " :");
        }
        
        conditionNodes = new LinkedList<>();
        String[] yNames = pagePattern.getYNames();
        String xName = pagePattern.getXName();
        String pName = pagePattern.getPName();
        String[] conditionNames = pagePattern.getConditionNames();
        FuncField xValuesPattern = pagePattern.getXValuesPattern();
        FuncField pValuesPattern = pagePattern.getPValuesPattern();
        //install page name and y
        pageNameText.setText(pagePattern.getName());
        StringBuilder builder = new StringBuilder();
        for(int index =0;index<yNames.length-1;index++){
            builder.append(yNames[index]).append(",");
        }
        builder.append(yNames[yNames.length -1]);
        yText.setText(builder.toString());
        
        
        //ref
        String ref = Routine.getRef(routine.getDeviceType(), conditionNames, xName, pName);
        this.refNode = ref;
        this.refBackup = ref;
        if(ref!=null){
            refCombo.setSelectedItem(ref);  
            refCombo.setEnabled(false);
            String conditionName = "ref_v"+ref.toLowerCase();
            FuncField conditionValue = pagePattern.getCondition(conditionName);
            String value = conditionValue.getParams()[0];
//            String value = param.substring(param.indexOf("=")+1);
            installValue(ref,REFERENCE,value);         
            JPanel panel = bondsMap.get(ref);
            for(int index =0;index<panel.getComponentCount()-1;index++){
                panel.getComponent(index).setEnabled(false);
            }
        }
        //condition install including limit condition except ref condition 
        int conditionNum = pagePattern.getConditionNames().length;
        for (int index = 0; index < conditionNum; index++) {
            String conditionName = pagePattern.getConditionNames()[index];
            if (conditionName.startsWith("ref")) {
                continue;
            }else if(conditionName.startsWith("limit")){ 
                continue;
            }else if(conditionName.equalsIgnoreCase(EXPRESSION)){ 
                continue;
            }else {
                FuncField conditionValue = pagePattern.getCondition(conditionName);
                String str = conditionValue.getParams()[0];
                //parse value
//                double scale = routine.getDevice().getDevicePolarity().getScale();
//                RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(routine.getDeviceType());
//                String value = new Double(MeaBundleUtil.getValue(str, routinePattern, scale)).toString();
               
                //include ref node voltage mode
                if (conditionName.contains(ref.toLowerCase())) {
                    //assert("end with ref node");
                    assert(conditionName.substring(2).equalsIgnoreCase(ref));
                    String nodeName = conditionName.substring(1, 2).toUpperCase();
                    //add into condition node
                    //this.conditionNode = nodeName;
                    this.conditionNodes.add(nodeName);
                    String[] values = prepareInstallValue(nodeName, refNode, conditionValue);
                    if(values.length == 1){
                         installValue(nodeName, CONSTANT, values);
                    }else{
                         installValue(nodeName, FUN, values);
                    }
                    //lock the conditio node fun mode
//                    funMap.get(nodeName).setEnabled(false);

                } else {
                    //todo exclued ref node current mode
                    String nodeName = conditionName.substring(1).toUpperCase();
                    modeMap.get(nodeName).setSelectedItem(CURRENT);
                    this.conditionNodes.add(nodeName);
                    String[] values = prepareInstallValue(nodeName, refNode, conditionValue);
                    if (values.length == 1) {
                        installValue(nodeName, CONSTANT, values);
                    } else {
                        installValue(nodeName, FUN, values);
                    }
                }
                
            
                

            }
        }
        
        //X,P load
        if(xName.toUpperCase().startsWith("V")){
            String start = xName.substring(1,2).toUpperCase();
            String stop = xName.substring(2).toUpperCase();
            modeMap.get(X).setSelectedItem(VOLTAGE);
            xFromCombo.setSelectedItem(start);
            xToCombo.setSelectedItem(stop);
            if(stop.equalsIgnoreCase(ref)){
                //install
                String[] values = prepareInstallValue(xName,ref,xValuesPattern); 
                if(values.length == 1){
                   installValue(start, CONSTANT,values);
                }else{
                   installValue(start, FUN,values);
                }
                
            }else{
                //todo
            }
            
        }else if (xName.toUpperCase().startsWith("I")){
            //todo
            String start = xName.substring(1, 2).toUpperCase();
            modeMap.get(X).setSelectedItem(CURRENT);
            modeMap.get(start).setSelectedItem(CURRENT);
            xFromCombo.setSelectedItem(start);
            xToCombo.setSelectedItem(null);
            String[] values = prepareInstallValue(xName, ref, xValuesPattern);
            if (values.length == 1) {
                installValue(start, CONSTANT, values);
            } else {
                installValue(start, FUN, values);
            }
        }
       
        
        if(pName.toUpperCase().startsWith("V")){
            String start = pName.substring(1,2).toUpperCase();
            String stop = pName.substring(2).toUpperCase();
            pFromCombo.setSelectedItem(start);
            pToCombo.setSelectedItem(stop);
            if (stop.equalsIgnoreCase(ref)) {
                //install
                String[] values = prepareInstallValue(pName, ref, pValuesPattern);
                if(values.length == 1){
                   installValue(start, CONSTANT, values);
                }else{
                   installValue(start, FUN, values);
                }
                
                //set expression text              
//                expressionText.setText("");
//                modeMap.get(P).setEnabled(true);
//                pFromCombo.setEnabled(true);
                
            } else {
                //get node value                
                JPanel panel = bondsMap.get(start);
                String[] valuesFrom = new String[3];
                String[] valuesTo = new String[3];
                //assert start node is install value
                if (panel != null) {
                    String fromFun = (String)funMap.get(start).getSelectedItem();
                    valuesFrom = uninstallValue(start,fromFun);                    
                    String[] values = prepareInstallValue(pName, ref, pValuesPattern);
                    //assert fun type
                    String[] actualValues = new String[3];
                    if (valuesFrom!=null&&valuesFrom[0] != null && !valuesFrom[0].isEmpty()) {
                        if (values.length == 1) {
                            //assert constant value
                            double constant = new Double(values[0]);
                            for (int index = 0; index < actualValues.length - 1; index++) {
                                actualValues[index] = String.format("%.4g", new Double(valuesFrom[index]) - constant);
                            }
                            actualValues[actualValues.length - 1] = valuesFrom[actualValues.length - 1];

                        } else {
                            //todo fun values
                            for (int index = 0; index < actualValues.length - 1; index++) {
                                actualValues[index] = String.format("%.4g", new Double(valuesFrom[0]) - new Double(values[index]));
                            }
                            actualValues[actualValues.length - 1] = values[actualValues.length - 1];
                        }                       
                        installValue(stop, FUN, actualValues);
                    } else {
                        //should get stop panel
                        panel = bondsMap.get(stop);
                        if (panel != null) {
                            String toFun = (String) funMap.get(stop).getSelectedItem();
                            valuesTo = uninstallValue(stop, toFun);
                            
                            if (values.length == 1) {
                                //assert constant value
                                double constant = new Double(values[0]);
                                for (int index = 0; index < actualValues.length - 1; index++) {
                                    actualValues[index] = String.format("%.4g", constant + new Double(valuesTo[index]));
                                }

                            } else {
                                //todo fun values
                                for (int index = 0; index < actualValues.length - 1; index++) {
                                    actualValues[index] = String.format("%.4g", new Double(values[index]) + new Double(valuesTo[index]));
                                }
                            }
                            actualValues[actualValues.length - 1] = valuesTo[actualValues.length - 1];
                            installValue(start, FUN, actualValues);
                        }
                    }
                    //set expression text
                    assert(values.length == 1);
                    builder = new StringBuilder();
                    builder.append(start).append(" = ");
                    builder.append(stop).append(" + ");
                    builder.append(values[0]);
                    expressionText.setText(builder.toString());
//                    modeMap.get(P).setEnabled(false);
//                    pFromCombo.setEnabled(false);
                    //lock start,stop fun
                    funMap.get(start).setEnabled(false);
                    funMap.get(stop).setEnabled(false);
                    this.leftNode = start;
                    this.rightNode = stop;
                    //set has expression
                    hasExpression = true;
                    
                    
                }
            }
            modeMap.get(start).setSelectedItem(VOLTAGE);
            modeMap.get(stop).setSelectedItem(VOLTAGE);
            modeMap.get(P).setSelectedItem(VOLTAGE);
            
        }else if (pName.toUpperCase().startsWith("I")){
            //todo
            String node = pName.substring(1).toUpperCase();
            String[] values = prepareInstallValue(pName, ref, pValuesPattern);
            if(values.length == 1){
                installValue(node, CONSTANT, values);
            }else{
                installValue(node, FUN, values);
            }      
            pFromCombo.setSelectedItem(node);
            modeMap.get(node).setSelectedItem(CURRENT);
            modeMap.get(P).setSelectedItem(CURRENT);
            //set expression text              
//            expressionText.setText("");
//            modeMap.get(P).setEnabled(true);
//            pFromCombo.setEnabled(true);
            
            
        }else if (pName.equalsIgnoreCase("NoP")){
           modeMap.get(P).setEnabled(false);
           pFromCombo.setSelectedItem(null); 
           pFromCombo.setEnabled(false);
           pToCombo.setSelectedItem(null);
           pToCombo.setEnabled(false);
           //set expression text              
//           expressionText.setText("");
        }
        
         //install limit value
        for (int index = 0; index < conditionNum; index++) {
            String conditionName = pagePattern.getConditionNames()[index];
            if (conditionName.startsWith("limit")) {
                FuncField conditionValue = pagePattern.getCondition(conditionName);
                String str = conditionValue.getParams()[0];
                String nodeName = conditionName.substring(conditionName.length() - 1).toUpperCase();
                installLimitValue(nodeName, str);
            }           
        }
         
        
        
        //lock ref node panel
        //bondsMap.get(ref).setEnabled(false);
        //set ref limit
        setRefLimitValue(ref);
//        funMap.get(ref).setEnabled(false);
        funMap.get(ref).setSelectedItem(REFERENCE);
        
        //hidden xTo,pTo combo
        xToCombo.setVisible(false);
        pToCombo.setVisible(false);
        
        //lock mode combo
//        for(int index =0;index<nodeNames.size();index++){
//            modeMap.get(nodeNames.get(index).toUpperCase()).setEnabled(false);
//        }
        
         //for current mode to show
        if (expressionText.getText().trim().isEmpty()) {
            FuncField condition = pagePattern.getCondition(EXPRESSION);
            if (condition != null) {
                String str = condition.toString();
                if (str != null && !str.isEmpty()) {
                    expressionText.setText(str);
                    hasExpression = true;
                }
            }

        }
        
        
        //lock expression node related panel
        //lock x mode
        if(hasExpression){
             pToCombo.setVisible(true);
//            modeMap.get(X).setEnabled(false);
            JPanel panel = bondsMap.get(pName.substring(1,2).toUpperCase());
            if (panel != null) {
                setComponentVisible(panel, 0, 5, false);
                setComponentVisible(panel, 5, 6, true);
                setComponentVisible(panel, 7, 12, false);
                setComponentVisible(panel, 12, 13, true);
            }            
        }
        
    }
    
    private String[] prepareInstallValue(String name,String ref,FuncField valuesPattern) {
        MeaBias meaBias = Routine.getMeaBias(routine.getDeviceType(), name, ref);
        RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(routine.getDeviceType());
        Routine.resolveBundles(meaBias, valuesPattern, routinePattern, routine.getDevice().getDevicePolarity());
        MeaBundle meabundle = meaBias.getBundle(0);
        if (meabundle instanceof SweepMeaBundle) {
            SweepMeaBundle sweep = (SweepMeaBundle) meabundle;
//            
            String start = String.format("%.4g", sweep.getStart());
            String stop = String.format("%.4g", sweep.getStop());
            String step = String.format("%.4g", sweep.getStep());
            int point = sweep.getPoint();
            return new String[]{start,stop,step};
//              
        } else if (meabundle instanceof ArrayMeaBundle) {
            //todo
            ArrayMeaBundle arrayBundle = (ArrayMeaBundle)meabundle;            
            return new String[]{String.format("%.4g",arrayBundle.get(0))};
            
        }
        return null;
    }
    
    private void installValue(String nodeName,String type,String... values) {
        if(type == null||nodeName == null||values == null){
            return;
        }
        //todo should allow CONSTANT/FUN
        JComboBox combo = funMap.get(nodeName);
        if (combo != null) {
            if (type.equalsIgnoreCase(CONSTANT)||type.equalsIgnoreCase(REFERENCE)) {
                combo.setSelectedItem(type);
                //install value for node panel
                JPanel panel = bondsMap.get(nodeName);
                if (panel != null) {
                    //constant
                    JTextField text = (JTextField) panel.getComponent(panel.getComponentCount() - 3);
                    text.setText(values[0]);
                }
            }else if (type.equalsIgnoreCase(FUN)){
                 combo.setSelectedItem(FUN);
                //install value for node panel
                JPanel panel = bondsMap.get(nodeName);
                if (panel != null) {
                    //fun
                    for (int index = panel.getComponentCount()/2; index < panel.getComponentCount()/2+3; index++) {
                        int actualIndex = index-panel.getComponentCount()/2;
                        JTextField text = (JTextField) panel.getComponent(index);
                        text.setText(values[actualIndex]);
                    }
                    //point calculate
                    Integer pointNum = 1;
                    Double d = ((new Double(values[1])-new Double(values[0]))/new Double(values[2]));
                    DecimalFormat df = new DecimalFormat("#####0");
                    if(!d.isInfinite()){
                        pointNum= new Integer(df.format(d));
                        pointNum = pointNum +1;
                    }                  
                    if(pointNum == 0 || pointNum == Integer.MAX_VALUE){
                        pointNum =1;
                    }
                    JTextField point = (JTextField) panel.getComponent(panel.getComponentCount() / 2 + 3);
                    point.setText(pointNum.toString());
                    
//                    for(int index =0;index< panel.getComponentCount() - 2;index++){
//                        int actualIndex = index / 2;
//                        if(index % 2 !=0){
//                            JTextField text = (JTextField) panel.getComponent(index);
//                            text.setText(values[actualIndex]);
//                        }
//                    }                   
                }               
            }
           
        }
    }
    
     private JPanel getFunPanel(final String type, JPanel panel) {
         panel = new JPanel(new MigLayout("wrap 7"));
         JLabel lstart = new JLabel("Start:");
         panel.add(lstart);
         JLabel lstop = new JLabel("Stop:");
         panel.add(lstop);
         JLabel lstep = new JLabel("Step:");
         panel.add(lstep);
         JLabel lpoint = new JLabel("Point:");
         panel.add(lpoint);
         JLabel lconstant = new JLabel("Constant:");
         panel.add(lconstant);
         JLabel llimit = new JLabel("Limit:");
         panel.add(llimit);
         panel.add(new JLabel(""),"w 8!");
       

         final JTextField start = new JTextField();
         start.setColumns(PREDERRED_TEXTSIZE);
//         start.setRows(1);
         start.setText("0");         
         panel.add(start);
         final JTextField stop = new JTextField();
         stop.setColumns(PREDERRED_TEXTSIZE);
//         stop.setRows(1);
         stop.setText("0");
         panel.add(stop);
         final JTextField step = new JTextField();
         step.setColumns(PREDERRED_TEXTSIZE);
//         step.setRows(1);
         step.setText("0");
         panel.add(step);
         final JTextField point = new JTextField();
         point.setColumns(PREDERRED_TEXTSIZE);
//         point.setRows(1);
         point.setText("1");
         panel.add(point);

         final JTextField constant = new JTextField();
         constant.setColumns(PREDERRED_TEXTSIZE);
//         constant.setRows(1);
         constant.setText("0");
         panel.add(constant);
         final JTextField limit = new JTextField();
         limit.setColumns(PREDERRED_TEXTSIZE);
//         limit.setRows(1);
         limit.setText(ORIGIN_LIMIT);
         panel.add(limit);
         
         JLabel limitUnit = new JLabel(AMPERE);
         panel.add(limitUnit,"w 8!");

//         final Document startDoc = start.getDocument();
//         final Document stopDoc = stop.getDocument();
//         final Document stepDoc = step.getDocument();
//         final Document pointDoc = point.getDocument();
//         Document constantDoc = constant.getDocument();
//         
//         final DocumentListenerStub stub = new DocumentListenerStub() {
//             @Override
//             public void validate() {
//
//                 JPopupMenu errorPop = new JPopupMenu();
//                 if (type.equalsIgnoreCase(FUN)) {
//                     String sstart = start.getText().trim();
//                     String sstop = stop.getText().trim();
//                     String sstep = step.getText().trim();
////                        if(sstart.isEmpty()||sstop.isEmpty()||sstep.isEmpty()){
////                            return;
////                        }
//                     if (!StringHelper.isDouble(sstart)
//                             || !StringHelper.isDouble(sstop)
//                             || !StringHelper.isDouble(sstep)) {
//                         return;
//                     }
//                     Double dstart = new Double(sstart);
//                     Double dstop = new Double(sstop);
//                     Double dstep = new Double(sstep);
//
//                     StringBuilder builder = new StringBuilder();
////                        if(Double.compare(dstart, dstop)==1){
////                            builder.append("start maybe should not bigger than stop!");
////                            builder.append("\n");
////                        }
//                     if (Double.isNaN(dstep) || Double.isNaN(dstart) || Double.isNaN(dstop)) {
//                         builder.append("should not be NAN!");
//                         builder.append("\n");
//                     }
//
////                        if(Double.compare(dstep, 0) == 0){
////                            builder.append("step should not be zero!");
////                            builder.append("\n");
////                        }
////                        errorPop.setForeground(Color.red);
//                     errorPop.add(builder.toString());
//                     if (!builder.toString().isEmpty()) {
//                         int x = PagePatternUpdateUI.this.getX() + PagePatternUpdateUI.this.getWidth() - 50;
//                         int y = PagePatternUpdateUI.this.getY() + PagePatternUpdateUI.this.getHeight() / 2;
//                         if (x > 0 && y > 0) {
//                             errorPop.show(PagePatternUpdateUI.this, x, y);
//                         }
//                     }
//                     
//                     Integer pointNum = (int)((dstop-dstart)/dstep)+1;
//                     if(pointNum == 0 || pointNum == Integer.MAX_VALUE){
//                         pointNum =1;
//                     }
//                     point.setText(pointNum.toString());
//                     
//
//
//                 } else if (type.equalsIgnoreCase(CONSTANT)) {
////                        String sconstant = constant.getText().trim();
////                        if (!StringHelper.isDouble(sconstant)) {
////                            return;
////                        }
////                        Double dcontant = new Double(sconstant);
//                 }
//                 doUpdate();
//                 try {
//                     Thread.sleep(10);
//                 } catch (Exception e) {
//                     e.printStackTrace();
//                 }
//             }
//         };
//
//         
//        final DocumentListenerStub stub1 = new DocumentListenerStub() {
//             @Override
//             public void validate() {
//                if (type.equalsIgnoreCase(FUN)) {
//                    
//                     String sstart = start.getText().trim();
//                     String sstop = stop.getText().trim();
//                     String spoint = point.getText().trim();
//                     if(spoint == null|| spoint.isEmpty()){
//                         spoint = "1";
//                     }
//                    
//                     Double dstart = new Double(sstart);
//                     Double dstop = new Double(sstop);
//                     Integer pointNum = new Integer(spoint);
//                     
//                    Double dstep = (dstop-dstart)/pointNum; 
//                    startDoc.removeDocumentListener(stub);
//                    stopDoc.removeDocumentListener(stub);
//                    stepDoc.removeDocumentListener(stub);
//                    step.setText(dstep.toString());
//                    stepDoc.addDocumentListener(stub);
//                    stopDoc.addDocumentListener(stub);
//                    stepDoc.addDocumentListener(stub);
//                    
//                }else{
//                    //todo
//                }
//                doUpdate();
//                
//             }
//         };
//         
//         startDoc.addDocumentListener(stub);
//         stopDoc.addDocumentListener(stub);
//         stepDoc.addDocumentListener(stub);
//         pointDoc.addDocumentListener(stub1);
         
//         point.addCaretListener(caret);

//         if (type.equalsIgnoreCase(FUN)) {
//             lconstant.setVisible(false);
//             constant.setVisible(false);
//             lstart.setVisible(true);
//             lstop.setVisible(true);
//             lstep.setVisible(true);
//             start.setVisible(true);
//             stop.setVisible(true);
//             step.setVisible(true);
//         } else if (type.equalsIgnoreCase(CONSTANT)) {
//             lstart.setVisible(false);
//             lstop.setVisible(false);
//             lstep.setVisible(false);
//             start.setVisible(false);
//             stop.setVisible(false);
//             step.setVisible(false);
//             lconstant.setVisible(true);
//             constant.setVisible(true);
//
//
//         }


         return panel;


    }

    private JPanel getFunPanel(String type) {
        JPanel panel = null;
        panel = getFunPanel(type, panel);
        return panel;
    }
    
    private void installLimitUnitValue(String nodeName,String unit){
        JPanel panel = bondsMap.get(nodeName);
        if(panel!=null){
            JLabel text = (JLabel)panel.getComponent(panel.getComponentCount()-1);
            text.setText(unit);
        }
    }
    
      private JComboBox getModeComboBox(final String name){
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel();
        final JComboBox<String> combo;    
        comboModel.addElement(VOLTAGE);
        comboModel.addElement(CURRENT);
        combo = new JComboBox(comboModel);
        combo.setPreferredSize(PREFERRED_SIZE);
        combo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                     //todo invoke start stop combo
                    String selectValue = (String)combo.getSelectedItem();
                    if(selectValue.equalsIgnoreCase(VOLTAGE)){
                        if (name.equalsIgnoreCase(X)) {
//                            String pNodeName = (String)pFromCombo.getSelectedItem();
                            String xNodeName = (String)xFromCombo.getSelectedItem();
                            xToCombo.setSelectedItem(refNode);
                            xToCombo.setEnabled(true);
                             //should set all related node mode
                            setNodeVoltageMode(xNodeName);
                        } else if (name.equalsIgnoreCase(P)) {
//                            String xNodeName = (String)xFromCombo.getSelectedItem();
                            String pNodeName = (String)pFromCombo.getSelectedItem();
//                            String pName = pagePattern.getPName();
//                            pToCombo.setSelectedItem(pName.substring(2).toUpperCase());
//                            pToCombo.setEnabled(true);
                            
                             //should set all related node mode
                            setNodeVoltageMode(pNodeName);
                            
                            if (hasExpression) {
                                parseExpression();
                            } else {
                                pToCombo.setSelectedItem(refNode);
                                pToCombo.setEnabled(true);
                            }                         
                        }else{
                            //node mode to invoke limit uint label
                            installLimitUnitValue(name,AMPERE);
                        }
                        
                    }else if (selectValue.equalsIgnoreCase(CURRENT)){
                        String xNodeName = (String) xFromCombo.getSelectedItem();
                        String pNodeName = (String)pFromCombo.getSelectedItem();
                        if(name.equalsIgnoreCase(X)){
                            //should set related node mode
                          
                            if (xNodeName!=null&&pNodeName!=null
                                    &&!xNodeName.equalsIgnoreCase(refNode)
                                    &&!xNodeName.equalsIgnoreCase(pNodeName)) {
                               setNodeCurrentMode(xNodeName);
                            }                            
                            
                            xToCombo.setSelectedItem(null);
                            xToCombo.setEnabled(false);
                        }else if (name.equalsIgnoreCase(P)){                           
                            //should set related node mode
                         
                            if (xNodeName!=null&&pNodeName!=null
                                     &&!pNodeName.equalsIgnoreCase(refNode)
                                     &&!xNodeName.equalsIgnoreCase(pNodeName)) {
                               setNodeCurrentMode(pNodeName);
                            }   
                            //do update in node combo
                            pToCombo.setSelectedItem(null);
                            pToCombo.setEnabled(false);
                        }else{
                            //node mode to invoke limit uint label
                            installLimitUnitValue(name,VOLTAGE);
                        }
                    }
                }
//                doUpdate();
            }
        });
        
        return combo;
    }
    
    private JComboBox getNodeComboBox(final String name){
        final DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel(nodeNames.toArray(new String[nodeNames.size()]));
        final JComboBox<String> combo;  
        combo = new JComboBox(comboModel);
        combo.setPreferredSize(PREFERRED_SIZE);
//        combo.addActionListener(new ActionListener(){
//            @Override
//            public void actionPerformed(ActionEvent e){
//                 //todo filter nodes to avoid from/to nodes the same
//                if (name.equalsIgnoreCase(P + FROM)) {
//                    String toNode = (String) pToCombo.getSelectedItem();
//                    comboModel.removeElement(toNode);
//                } else if (name.equalsIgnoreCase(P + TO)) {
//                    String fromNode = (String) pFromCombo.getSelectedItem();
//                    comboModel.removeElement(fromNode);
//                } else if (name.equalsIgnoreCase(X + FROM)) {
//                    String toNode = (String) xToCombo.getSelectedItem();
//                    comboModel.removeElement(toNode);
//                } else if (name.equalsIgnoreCase(X + TO)) {
//                    String fromNode = (String) xFromCombo.getSelectedItem();
//                    comboModel.removeElement(fromNode);
//                }
//            }
//        });
        combo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //filter condition nodes 
//                if(conditionNodes!=null){
//                    for(int index =0;index<conditionNodes.size();index++){
//                        String conditionNode = conditionNodes.get(index);
//                        if(comboModel.getIndexOf(conditionNode)!=-1){
//                            comboModel.removeElement(conditionNode);
//                        }                        
//                    }                    
//                }  
                
                if (e.getStateChange() == ItemEvent.SELECTED) {                   
                    //todo 
                    if(name.equalsIgnoreCase(P+FROM)){
                        String xNodeName = (String)xFromCombo.getSelectedItem();
                        String pNodeName = (String)pFromCombo.getSelectedItem();
                        String mode = (String) modeMap.get(P).getSelectedItem();
                        if(mode.equalsIgnoreCase(CURRENT)){
                             //set all nodes voltage
//                          setAllNodesVoltageMode(xNodeName);
                            
                            String nodeName = (String)combo.getSelectedItem();
                            if (!nodeName.equalsIgnoreCase(refNode)
                                    &&!nodeName.equalsIgnoreCase(xNodeName)) {
                              setNodeCurrentMode(nodeName);
                            }                          
                            
                        }else if (mode.equalsIgnoreCase(VOLTAGE)){
                            //set related nodes voltage
                            setNodeVoltageMode(pNodeName);
                                
                        }
                    }else if (name.equalsIgnoreCase(P+TO)){
                        //todo
                        
                    }else if (name.equalsIgnoreCase(X+FROM)){
                        String pNodeName = (String)pFromCombo.getSelectedItem();
                        String xNodeName = (String)xFromCombo.getSelectedItem();
                        String mode = (String) modeMap.get(X).getSelectedItem();
                        if(mode.equalsIgnoreCase(CURRENT)){
                           //set all nodes voltage
//                           setAllNodesVoltageMode(pNodeName);
                            
                            String nodeName = (String)combo.getSelectedItem();
                            if (!nodeName.equalsIgnoreCase(refNode)
                                     &&!nodeName.equalsIgnoreCase(pNodeName)) {
                               setNodeCurrentMode(nodeName);
                            }                          
                            
                        }else if (mode.equalsIgnoreCase(VOLTAGE)){
                            //set related nodes voltage
                             setNodeVoltageMode(xNodeName);                                
                        }
                    }
                }
//                doUpdate();
            }
        });
        
        return combo;
    }
    
    private void setNodeCurrentMode(String nodeName) {
        if (nodeName != null) {
            modeMap.get(nodeName).setSelectedItem(CURRENT);
//        modeMap.get(nodeName).setEnabled(false);
            String limit = limitMap.get(VOLTAGE + nodeName);
            //set voltage output limit
            installLimitValue(nodeName, limit);
        }
     
    }
    
    private void setNodeVoltageMode(String nodeName) {
        if (nodeName != null) {
            modeMap.get(nodeName).setSelectedItem(VOLTAGE);
//       modeMap.get(nodeName).setEnabled(true);
            String limit = limitMap.get(CURRENT + nodeName);
            //set current output limit
            installLimitValue(nodeName, limit);
        }
      

    }
    
//    private void setAllNodesVoltageMode(String exceptNode) {
//        for (int index = 0; index < nodeNames.size(); index++) {
//            String nodeName = nodeNames.get(index);
//            if (!nodeName.equalsIgnoreCase(exceptNode)) {
//                modeMap.get(nodeName).setSelectedItem(VOLTAGE);
////                modeMap.get(nodeName).setEnabled(true);
//                String limit = limitMap.get(CURRENT+nodeName);
//                //set current output limit
//                installLimitValue(nodeName, limit);
//            }
//        }
//    }
    
    private void setRefNodeFromPanel(){
         //get ref node
        for(int index =0;index<nodeNames.size();index++){
            String nodeName = nodeNames.get(index).toUpperCase();
            String fun = (String)funMap.get(nodeName).getSelectedItem();           
            if(fun!=null&&fun.equalsIgnoreCase(REFERENCE)){
                this.refNode = nodeName;
            }
        }
    }
    
    private void setRefLimitValue(String nodeName) {       
        if (fastType == null) {
            fastType = "FS360";
        }
        if (fastType.equalsIgnoreCase("FS360")) {
            installLimitValue(nodeName, CURRENT_LIMIT_360);
        } else if (fastType.equalsIgnoreCase("FS380")) {
            installLimitValue(nodeName, CURRENT_LIMIT_380);
        }
    }
    
    private JComboBox getFunComboBox(final String nodeName) {
        DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel();
        final JComboBox<String> combo;
        comboModel.addElement(FUN);
        comboModel.addElement(CONSTANT);
        comboModel.addElement(REFERENCE);
        combo = new JComboBox(comboModel);
        combo.setPreferredSize(PREFERRED_SIZE);
        combo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    //todo invoke produce new panel
                    String selectValue = (String)combo.getSelectedItem();
                    JPanel oldPanel = bondsMap.get(nodeName);
                    if(oldPanel!=null){   
                        if(selectValue.equalsIgnoreCase(FUN)){
//                            for(int index =0;index<oldPanel.getComponentCount()-2;index++){
//                                oldPanel.getComponent(index).setEnabled(true);
//                            }
//                            oldPanel.getComponent(oldPanel.getComponentCount()-2).setEnabled(false);
//                            oldPanel.getComponent(oldPanel.getComponentCount()-1).setEnabled(false);
//                            PagePatternUpdateUI.this.refNode = refBackup; 
                            setRefNodeFromPanel();
                            setComponentVisible(oldPanel, 0, 4, true);
                            setComponentVisible(oldPanel, 4, 5, false);
                            setComponentVisible(oldPanel, 7, 11, true);
                            setComponentVisible(oldPanel, 11, 12, false);
                            setComponentVisible(oldPanel, 12, 13, true);
                            
                        }else if (selectValue.equalsIgnoreCase(CONSTANT)){
//                            for(int index =0;index<oldPanel.getComponentCount()-2;index++){
////                                oldPanel.getComponent(index).setVisible(false);
//                                oldPanel.getComponent(index).setEnabled(false);
//                            }
////                            oldPanel.getComponent(oldPanel.getComponentCount()-2).setVisible(true);
////                            oldPanel.getComponent(oldPanel.getComponentCount()-1).setVisible(true);
//                            oldPanel.getComponent(oldPanel.getComponentCount()-2).setEnabled(true);
//                            oldPanel.getComponent(oldPanel.getComponentCount()-1).setEnabled(true);
//                             PagePatternUpdateUI.this.refNode = refBackup; 
                             setRefNodeFromPanel();
                             setComponentVisible(oldPanel,0,4,false);
                             setComponentVisible(oldPanel,4,6,true);
                             setComponentVisible(oldPanel,7,11,false);
                             setComponentVisible(oldPanel,11,13,true);
                        }else if (selectValue.equalsIgnoreCase(REFERENCE)){                            
                             PagePatternUpdateUI.this.refNode = nodeName;                              
                             setComponentVisible(oldPanel,0,4,false);
                             setComponentVisible(oldPanel,4,6,true);
                             setComponentVisible(oldPanel,7,11,false);
                             setComponentVisible(oldPanel,11,12,true);
                             setComponentVisible(oldPanel, 12, 13, false);
                             //auto set value
                             setRefLimitValue(nodeName);
                            
                             
                        }
                        refCombo.setSelectedItem(PagePatternUpdateUI.this.refNode);
                        xToCombo.setSelectedItem(PagePatternUpdateUI.this.refNode);
                        String modeP = (String)modeMap.get(P).getSelectedItem();
                        String pFromNode = (String)pFromCombo.getSelectedItem();
                        String pToNode = (String)pToCombo.getSelectedItem();
                        if(modeP!=null&&modeP.equalsIgnoreCase(VOLTAGE)
                                && pFromNode!=null
                                && pToNode!=null
                                && pToNode.equalsIgnoreCase(refBackup)){
                             pToCombo.setSelectedItem(PagePatternUpdateUI.this.refNode);
                        }
                       
                       
                    }
                    
                    //refresh
                   // mainPanel.updateUI();
                    
                }
                //should not save when fun changed
                //give it to panel one
//                doUpdate();
            }
        });
        
        return combo;
    }
    
    private void setComponentVisible(JPanel oldPanel, int start, int end, boolean isVisible) {
        if (isVisible) {
            for (int index = start; index < end; index++) {
//                oldPanel.getComponent(index).setVisible(true);
                oldPanel.getComponent(index).setEnabled(true);
                oldPanel.getComponent(index).setPreferredSize(PREFERRED_SIZE);
//                oldPanel.getComponent(index).setMaximumSize(PREFERRED_SIZE);
//                oldPanel.getComponent(index).setMinimumSize(PREFERRED_SIZE);
            }
        } else {
            for (int index = start; index < end; index++) {
//                oldPanel.getComponent(index).setVisible(false);
                oldPanel.getComponent(index).setEnabled(false);
                oldPanel.getComponent(index).setPreferredSize(HIDDEN_SIZE);
//                oldPanel.getComponent(index).setMaximumSize(HIDDEN_SIZE);
//                oldPanel.getComponent(index).setMinimumSize(HIDDEN_SIZE);
            }
        }
    }
    
    public void clearCache(){        
       xFromCombo.setSelectedItem(null);
       xToCombo.setSelectedItem(null);
       pFromCombo.setSelectedItem(null);
       pToCombo.setSelectedItem(null);
       refCombo.setSelectedItem(null);
       expressionText.setText("");
       modeMap.get(X).setEnabled(true);
       modeMap.get(P).setEnabled(true);
       pFromCombo.setEnabled(true);
       pToCombo.setEnabled(true);
       
       hasExpression = false;
       leftNode = null;
       rightNode = null;
       
//       modeMap.get(P).setSelectedItem(null);
//       modeMap.get(X).setSelectedItem(null);
       
       for(int i =0;i<nodeNames.size();i++){
           String nodeName = nodeNames.get(i).toUpperCase();
//           modeMap.get(nodeName).setSelectedItem(null);
           funMap.get(nodeName).setEnabled(true);
           funMap.get(nodeName).setSelectedItem(null);           
           JPanel panel = bondsMap.get(nodeName);
           for (int index = panel.getComponentCount()/2; index < panel.getComponentCount()-1; index++) {               
               JTextField area = (JTextField) panel.getComponent(index);
               if(index ==  panel.getComponentCount() -2){
                  area.setText(ORIGIN_LIMIT);      
               }else if (index ==  panel.getComponentCount() -4){
                  area.setText("1");
               }else{
                  area.setText("0");
               }
                             
           }
//           for(int index = 0;index<panel.getComponentCount();index++){
//               if(index % 2 !=0){
//                   JTextField area = (JTextField)panel.getComponent(index);
//                   area.setText("");                   
//               }
//           }
       }
    }
    
    @Override
    public void setRawPatternName(String rawPatternName){
        this.rawPatternName = rawPatternName;
    }
    
    public void afterLoadPagePattern(){
        stub1Map = new LinkedHashMap<>();
        stub2Map = new LinkedHashMap<>();
        stub3Map = new LinkedHashMap<>();
         for (int index = 0; index < nodeNames.size(); index++) {
             stub1Map.put(nodeNames.get(index), new DocumentListenerStub1());
             stub2Map.put(nodeNames.get(index), new DocumentListenerStub2());
             stub3Map.put(nodeNames.get(index), new DocumentListenerStub3());
         }
        
        for (int index = 0; index < nodeNames.size(); index++) {
            String nodeName = nodeNames.get(index).toUpperCase();
            final String type = (String) funMap.get(nodeName).getSelectedItem();
            JPanel panel = bondsMap.get(nodeName);
            final JTextField start = (JTextField)panel.getComponent(panel.getComponentCount()/2);
            final JTextField stop = (JTextField)panel.getComponent(panel.getComponentCount()/2+1);
            final JTextField step = (JTextField)panel.getComponent(panel.getComponentCount()/2+2);
            final JTextField point = (JTextField)panel.getComponent(panel.getComponentCount()/2+3);
            final JTextField constant = (JTextField)panel.getComponent(panel.getComponentCount()/2+4);
            final JTextField limit = (JTextField)panel.getComponent(panel.getComponentCount()/2+5);

            final Document startDoc = start.getDocument();
            final Document stopDoc = stop.getDocument();
            final Document stepDoc = step.getDocument();
            final Document pointDoc = point.getDocument();
            Document constantDoc = constant.getDocument();
            Document limitDoc = limit.getDocument();

            stub1Map.get(nodeName).setParams(nodeName,type, start, stop, step, point);
            stub2Map.get(nodeName).setParams(nodeName,type, start, stop, step, point);

            startDoc.addDocumentListener(stub1Map.get(nodeName));
            stopDoc.addDocumentListener(stub1Map.get(nodeName));
            stepDoc.addDocumentListener(stub1Map.get(nodeName));
            //constantDoc to share update in the listener
            constantDoc.addDocumentListener(stub1Map.get(nodeName));
            //limitDoc 
            limitDoc.addDocumentListener(stub3Map.get(nodeName));
            //todo add info into limitHashMap
            pointDoc.addDocumentListener(stub2Map.get(nodeName));
        }
        
        //add document listener to expression text
        DocumentListenerStub stub = new DocumentListenerStub(){
            @Override
            public void validate(){
                parseExpression();                
//                doUpdate();
            }
        };
        expressionText.getDocument().addDocumentListener(stub);
             
    }
    
    private void addExressionErrorInfo(){
        StringBuilder builder = new StringBuilder();
        builder.append("some errors with expression! ");
        builder.append("\n");
        this.errorInfo = builder.toString();
    }
    
    private void removeExpressionErrorInfo(){
        this.errorInfo = "";
    }
    
    private boolean isLegalNodeName(String nodeName){
        for(int index =0;index<nodeNames.size();index++){
            if(nodeName.equalsIgnoreCase(nodeNames.get(index))){
                return true;
            }
        }
        return false;        
    }
    
    private void parseExpression(){
//        if(this.getErrorInfo()!=null){
//            hasExpression = false;
//            leftNode = null;
//            rightNode = null;
//            return;
//        }
        
        String expression = expressionText.getText().trim();
        if(expression.isEmpty()){
            //todo
            //unlock
            if(leftNode == null || rightNode == null){
                for(int index =0;index<nodeNames.size();index++){
                    String nodeName = nodeNames.get(index);
                    if(!nodeName.equalsIgnoreCase(refNode)){
                        setExpressionNodePanelVisible(nodeName,true);
                    }
                    //unlock fun
                    funMap.get(nodeName).setEnabled(true);
                }
            }else if(leftNode!=null&&rightNode!=null){
                setExpressionNodePanelVisible(leftNode, true);
                setExpressionNodePanelVisible(rightNode, true);
                funMap.get(leftNode).setEnabled(true);
                funMap.get(rightNode).setEnabled(true);
            }
            pToCombo.setVisible(false);
            String pMode = (String)modeMap.get(P).getSelectedItem();
            if(pMode.equalsIgnoreCase(VOLTAGE)){
                pToCombo.setSelectedItem(refNode);
            }
            removeExpressionErrorInfo();            
            hasExpression = false;
            leftNode = null;
            rightNode = null;
        }
        String constant;
        String rightFun = null;
        rightNode = null;
        String[] leftValues = new String[3];
        if(expression.contains("=")){
            String[] values = expression.split("=");
            if(values.length<2){
                addExressionErrorInfo();
                return;
            }
            leftNode = values[0].trim().toUpperCase(); 
            if(leftNode == null||leftNode.isEmpty()||!isLegalNodeName(leftNode)){
                addExressionErrorInfo();
                return;
            }
            if(values[1].contains("+")){
                String[] subValues = values[1].split("\\+");
                if(subValues.length<2){
                    addExressionErrorInfo();
                    return;
                }
                rightNode = subValues[0].trim().toUpperCase();               
                constant = subValues[1].trim();
                if(!StringHelper.isDouble(constant)
                       ||rightNode == null
                       ||rightNode.isEmpty()){
                    addExressionErrorInfo();
                    return;
                }
                if (!isLegalNodeName(rightNode)) {
                    addExressionErrorInfo();
                    return;
                }
                rightFun = (String)funMap.get(rightNode).getSelectedItem();
                String[] rightValues = uninstallValue(rightNode,rightFun);
                leftValues = new String[rightValues.length];
                if(rightValues.length == 1){
                    modeMap.get(leftNode).setSelectedItem(CONSTANT);
                    leftValues[0] = String.format("%.4g",new Double(rightValues[0])+new Double(constant));
                }else{
                    modeMap.get(leftNode).setSelectedItem(FUN);
                    for(int index =0; index <rightValues.length-1;index++){
                        leftValues[index] = String.format("%.4g",new Double(rightValues[index])+new Double(constant));
                    }
                    leftValues[rightValues.length-1] = rightValues[rightValues.length-1];
                }
                
            }else if (values[1].contains("-")){
                //todo
            }else if (values[1].contains("*")){
                
            }else if (values[1].contains("/")){
                
            }else{
                addExressionErrorInfo();
                return;
            }
            
            if (leftNode == null || rightFun == null || leftValues[0] == null) {
                addExressionErrorInfo();
                return;
            }
            //remove step/point listner 
            //then add them back after install value
            JPanel panel = bondsMap.get(leftNode);
            if (panel != null) {
                JTextField start = (JTextField) panel.getComponent(panel.getComponentCount() / 2);
                JTextField stop = (JTextField) panel.getComponent(panel.getComponentCount() / 2 + 1);
                JTextField step = (JTextField) panel.getComponent(panel.getComponentCount() / 2 + 2);
                JTextField point = (JTextField) panel.getComponent(panel.getComponentCount() / 2 + 3);
                start.getDocument().removeDocumentListener(stub1Map.get(leftNode));
                stop.getDocument().removeDocumentListener(stub1Map.get(leftNode));
                step.getDocument().removeDocumentListener(stub1Map.get(leftNode));
                point.getDocument().removeDocumentListener(stub2Map.get(leftNode));
                installValue(leftNode, rightFun, leftValues);
                start.getDocument().addDocumentListener(stub1Map.get(leftNode));
                stop.getDocument().addDocumentListener(stub1Map.get(leftNode));
                step.getDocument().addDocumentListener(stub1Map.get(leftNode));
                point.getDocument().addDocumentListener(stub2Map.get(leftNode));
            }
            
            //set pTo node auto
            pToCombo.setVisible(true);
            String pMode = (String)modeMap.get(P).getSelectedItem();
            if(pMode.equalsIgnoreCase(VOLTAGE)){
                pFromCombo.setSelectedItem(leftNode);
                pToCombo.setSelectedItem(rightNode);
            }
            //lock leftnode panel,unlock rightnode panel
           setExpressionNodePanelVisible(leftNode,false);          
            //unlock
           setExpressionNodePanelVisible(rightNode,true);
           funMap.get(leftNode).setEnabled(false);
           funMap.get(rightNode).setEnabled(false);
           removeExpressionErrorInfo();
           hasExpression = true;
            
        }
    }
    
    private void setExpressionNodePanelVisible(String node,boolean isVisible){
        if (node == null) {
            return;
        }
        JPanel panel = bondsMap.get(node);
        String fun = (String)funMap.get(node).getSelectedItem();
        if (panel != null) {
            if (fun.equalsIgnoreCase(FUN)) {                
                setComponentVisible(panel, 0, 4, isVisible);
                setComponentVisible(panel, 4, 5, false);
                setComponentVisible(panel, 5, 6, true);
                setComponentVisible(panel, 7, 11, isVisible);
                setComponentVisible(panel, 11, 12, false);
                setComponentVisible(panel, 12, 13, true);
            }else if (fun.equalsIgnoreCase(CONSTANT)){
                setComponentVisible(panel, 0, 4, false);
                setComponentVisible(panel, 4, 5, isVisible);
                setComponentVisible(panel, 5, 6, true);
                setComponentVisible(panel, 7, 11, false);
                setComponentVisible(panel, 11, 12, isVisible);
                setComponentVisible(panel, 12, 13, true);
            }
           
        }
    }
    
    
    
     public void setPagePattern(EntityPagePattern pagePattern,Routine routine,boolean doUpdate) {
         setPagePattern(pagePattern,routine);
         isDoUpdate = doUpdate;
     }
    
    @Override
     public void setPagePattern(EntityPagePattern pagePattern,Routine routine) {
        isDoUpdate = false;
        this.routine = routine;
        this.pagePattern = pagePattern;
        this.rawPatternName = pagePattern.getName();
        this.updatePagePattern = deepCopyValue(pagePattern);//new EntityPagePattern();
//        updatePagePattern.copyValueOf(pagePattern);
        initComponent();
        loadPagePattern();
        afterLoadPagePattern();
        isDoUpdate = true;
        
     }
    @Override
     public void setPagePattern(EntityPagePattern pagePattern) {
        isDoUpdate = false;
        clearCache();
        this.pagePattern = pagePattern;
//        this.rawPatternName = pagePattern.getName();
         this.updatePagePattern = deepCopyValue(pagePattern);//new EntityPagePattern();
//        updatePagePattern.copyValueOf(pagePattern);
//         initComponent();
        loadPagePattern();
        isDoUpdate = true;
         
     }
      private EntityPagePattern deepCopyValue(EntityPagePattern other){
         EntityPagePattern result = new EntityPagePattern();
         result.type = other.type;
         result.setGroupName(other.getGroupName()); 
         result.setCategory(other.getCategory());
         result.setPName(other.getPName());
         result.setXName(other.getXName());
         result.setYNames(other.getYNames());        
         result.setPValuesPattern(other.getPValuesPattern());
         result.setXValuesPattern(other.getXValuesPattern());
         result.setName(other.getName());
         result.setDeviceType(other.getDeviceType());
         result.setDevicePolarity(other.getDevicePolarity());
//         result.setSimulationPath(other.getSimulationPaths());

        Map<String,FuncField> conditions = new LinkedHashMap<>();
        String[] conditionNames = other.getConditionNames();
        for(int index =0;index<other.getConditionNames().length;index++){
//            conditions.put(conditionNames[index], other.getCondition(conditionNames[index]));
            FuncField func = other.getCondition(conditionNames[index]);
            FuncField copyFunc = new FuncField(func.toString());
            result.setCondition(conditionNames[index], copyFunc);
        }
         
         
//          result.conditions = other.conditions;

           return result;
     }
    
      @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == updateButton) {
            doUpdate();
        } 
    }
    
    @Override
    public void doUpdate() {

        if (isDoUpdate) {
            if(getErrorInfo()!=null){
                return;
            }
            EntityPagePattern pattern = getPagePattern();
            if (PatternUtil.checkPatternLegality(pattern)) {
                //add to updated patterns        
                updatedPatterns.put(rawPatternName, pattern);//pattern.getName()
            }
        }

    }


    @Override
    public IndPagePattern getIndPagePattern() {
        if (pagePattern == null) {
            return null;
        }
        IndPagePattern indPagePattern = new IndPagePattern();
        indPagePattern.pagePattern = getPagePattern();
        
        return indPagePattern;
    }

    @Override
    public BaseVarProvider getVarProvider() {
         RoutinePattern routinePattern = RoutinePatternManager.getInstance().getRoutinePattern(routine.getDeviceType());
         return routinePattern;
    }
    
    
      private final class DocumentListenerStub3 extends DocumentListenerStub{
   
//        String type,nodeName;
//        JTextField start,stop,step,point;
        public DocumentListenerStub3(){
            
        }
        
//        public void setParams(String nodeName,String type,
//                JTextField start,JTextField stop,JTextField step,JTextField point){
//            this.nodeName = nodeName;
//            this.type = type;
//            this.start = start;
//            this.stop = stop;
//            this.step = step;
//            this.point = point;
//        }
        
        @Override
        public void validate() {          
            doUpdate();
        }
    }
    
    private final class DocumentListenerStub2 extends DocumentListenerStub{
   
        String type,nodeName;
        JTextField start,stop,step,point;
//        public DocumentListenerStub2(String type,
//                JTextField start,JTextField stop,JTextField step,JTextField point){
//            this.start = start;
//            this.stop = stop;
//            this.step = step;
//            this.point = point;
//        }
        public DocumentListenerStub2(){
            
        }
        
        public void setParams(String nodeName,String type,
                JTextField start,JTextField stop,JTextField step,JTextField point){
            this.nodeName = nodeName;
            this.type = type;
            this.start = start;
            this.stop = stop;
            this.step = step;
            this.point = point;
        }
        
        @Override
        public void validate() {
            //if (type.equalsIgnoreCase(FUN)) {

                String sstart = start.getText().trim();
                String sstop = stop.getText().trim();
                String spoint = point.getText().trim();
                if (spoint == null || spoint.isEmpty()) {
                    spoint = "1";
                }

                Double dstart = new Double(sstart);
                Double dstop = new Double(sstop);
                Integer pointNum = new Integer(spoint)-1;
                if(pointNum<1){
                   pointNum =1; 
                }
                Double dstep = (dstop - dstart) / pointNum;
               
                step.getDocument().removeDocumentListener(stub1Map.get(nodeName));
                String str = String.format("%.4g",dstep);
                step.setText(str);
                if(hasExpression){
                    parseExpression();
                }
                step.getDocument().addDocumentListener(stub1Map.get(nodeName));
               

//            } else {
//                //todo
//            }
//            doUpdate();

        }
    }
    private final class DocumentListenerStub1 extends DocumentListenerStub{
        String type,nodeName;
        JTextField start,stop,step,point;
//        public DocumentListenerStub1(String type,
//                JTextField start,JTextField stop,JTextField step,JTextField point){
//            this.start = start;
//            this.stop = stop;
//            this.step = step;
//            this.point = point;
//        }
        public DocumentListenerStub1(){
            
        }
        public void setParams(String nodeName,String type,
                JTextField start, JTextField stop, JTextField step, JTextField point) {
            this.type = type;
            this.nodeName = nodeName;
            this.start = start;
            this.stop = stop;
            this.step = step;
            this.point = point;
        }
        
        @Override
        public void validate(){
            
                    JPopupMenu errorPop = new JPopupMenu();
                   // if (type.equalsIgnoreCase(FUN)) {
                        String sstart = start.getText().trim();
                        String sstop = stop.getText().trim();
                        String sstep = step.getText().trim();
//                        if (sstep.startsWith("0") && !sstep.startsWith("0.")) {
//                            return;
//                        }
                        if (!StringHelper.isDouble(sstart)
                                || !StringHelper.isDouble(sstop)
                                || !StringHelper.isDouble(sstep)) {
                            return;
                        }
                        Double dstart = new Double(sstart);
                        Double dstop = new Double(sstop);
                        Double dstep = new Double(sstep);

                        StringBuilder builder = new StringBuilder();
//                        if(Double.compare(dstart, dstop)==1){
//                            builder.append("start maybe should not bigger than stop!");
//                            builder.append("\n");
//                        }
                        if (Double.isNaN(dstep) || Double.isNaN(dstart) || Double.isNaN(dstop)) {
                            builder.append("should not be NAN!");
                            builder.append("\n");
                        }

//                        if(Double.compare(dstep, 0) == 0){
//                            builder.append("step should not be zero!");
//                            builder.append("\n");
//                        }
//                        errorPop.setForeground(Color.red);
                        errorPop.add(builder.toString());
                        if (!builder.toString().isEmpty()) {
                            int x = PagePatternUpdateUI.this.getX() + PagePatternUpdateUI.this.getWidth() - 50;
                            int y = PagePatternUpdateUI.this.getY() + PagePatternUpdateUI.this.getHeight() / 2;
                            if (x > 0 && y > 0) {
                                errorPop.show(PagePatternUpdateUI.this, x, y);
                            }
                        }
                        Integer pointNum = 1;
                        Double d = ((dstop - dstart) / dstep);
                        if(!d.isNaN()&&!d.isInfinite()){
                            DecimalFormat df = new DecimalFormat("#####0");
                            pointNum = new Integer(df.format(d));
                            pointNum = pointNum + 1;
                        }
                       
                        if (pointNum == 0 || pointNum == Integer.MAX_VALUE) {
                            pointNum = 1;
                        }
                       point.getDocument().removeDocumentListener(stub2Map.get(nodeName));
                       point.setText(pointNum.toString());
                       if (hasExpression) {
                           parseExpression();
                       }
                       point.getDocument().addDocumentListener(stub2Map.get(nodeName));   



//                    } else if (type.equalsIgnoreCase(CONSTANT)) {
////                        String sconstant = constant.getText().trim();
////                        if (!StringHelper.isDouble(sconstant)) {
////                            return;
////                        }
////                        Double dcontant = new Double(sconstant);
//                    }
                       
//                    doUpdate();
        }
    }
    
    
    
    
}
