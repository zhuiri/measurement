/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.datacore.pattern;

/**
 *
 * @author renjing
 */

import com.platformda.datacore.DevicePolarity;
import com.platformda.datacore.DeviceTag;
import com.platformda.datacore.DeviceType;
import com.platformda.iv.datacore.DeviceTypeManager;
import com.platformda.datacore.EntityPageType;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.datacore.pattern.EntityPagePatternGroup;
import com.platformda.datacore.pattern.FuncField;
import com.platformda.utility.common.Appender;
import com.platformda.utility.common.FileUtil;
import com.platformda.utility.common.LoaderUtil;
import com.platformda.utility.common.Pair;
import com.platformda.utility.common.StringUtil;
import com.platformda.utility.common.VarProvider;
import com.platformda.utility.common.WriterAppender;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PagePatternLoader
{
  protected DeviceType deviceType;
  protected DevicePolarity devicePolarity;

  public static void save(List<EntityPagePattern> patterns, String path, VarProvider varProvider)
    throws IOException
  {
    FileWriter fw = new FileWriter(path);

    fw.append("// Platform Design Automation\n");
    fw.append("// Page Pattern\n");

    List<String> vars = new ArrayList();
    for (EntityPagePattern pagePattern : patterns) {
      pagePattern.fetchVars(vars);
    }
    boolean appendDummy = true;
    for (EntityPagePattern pagePattern : patterns) {
      if ((pagePattern.getDeviceType() != null) && (pagePattern.getDevicePolarity() != null)) {
        fw.append(String.format("DeviceType %s DevicePolarity %s ", new Object[] { pagePattern.getDeviceType().getName(), pagePattern.getDevicePolarity().getName() }));
        appendDummy = false;
        break;
      }
    }
    for (String var : vars) {
      fw.append(var);
      double value = varProvider.getDouble(var);
      if (Double.isNaN(value))
      {
        String strValue = varProvider.getString(var);
        if (strValue != null) {
          fw.append(" ");
          fw.append(strValue);
          fw.append(" ");
        } else {
          fw.append(" 0 ");
        }
      }
      else {
        fw.append(" ");
        fw.append(StringUtil.getConciseString(value));
        fw.append(" ");
      }

    }

    if ((appendDummy) && (vars.isEmpty())) {
      fw.append("dummy 0");
    }
    fw.append("\n\n");

    WriterAppender writerAppender = new WriterAppender(fw);
    for (EntityPagePattern pagePattern : patterns) {
      save(pagePattern, writerAppender);
    }

    fw.flush();
    fw.close();
  }

  public static void save(EntityPagePattern pagePattern, Appender fw) throws IOException {
    fw.append("Page\n");

    fw.append("Name ");
    fw.append(pagePattern.getName());
    fw.append("\n");

    if (pagePattern.getDeviceType() != null) {
      fw.append(String.format("DeviceType %s\n", new Object[] { pagePattern.getDeviceType().getName() }));
      if (pagePattern.getDevicePolarity() != null) {
        fw.append(String.format("DevicePolarity %s\n", new Object[] { pagePattern.getDevicePolarity().getName() }));
      }
    }

    if(pagePattern.getYNames()!=null){
        fw.append(String.format("Y %s\n", new Object[] { StringUtil.concatenate(pagePattern.getYNames(), " ") }));
    }
    if(pagePattern.getXValuesPattern()!=null){
         fw.append(String.format("X %s=%s\n", new Object[] { pagePattern.getXName(), pagePattern.getXValuesPattern().toString() }));
    }
    if(pagePattern.getPValuesPattern()!=null){
          fw.append(String.format("P %s=%s\n", new Object[] { pagePattern.getPName(), pagePattern.getPValuesPattern().toString() }));
    }   
  

    if (StringUtil.isValid(pagePattern.getGroupName())) {
      fw.append(String.format("Group %s\n", new Object[] { pagePattern.getGroupName() }));
    }
    if (StringUtil.isValid(pagePattern.getCategory())) {
      fw.append(String.format("Category %s\n", new Object[] { pagePattern.getCategory() }));
    }

    String[] conditionNames = pagePattern.getConditionNames();
    for (String con : conditionNames) {
      fw.append(String.format("Condition %s=%s\n", new Object[] { con, pagePattern.getCondition(con).toString() }));
    }

    if (pagePattern.getType() == 2)
    {
      Map<String,String> simulationPaths = pagePattern.getSimulationPaths();
      if (simulationPaths != null) {
        for (Map.Entry entry : simulationPaths.entrySet()) {
          fw.append(String.format("%s %s\n", new Object[] { entry.getKey(), entry.getValue() }));
        }
      }
    }
    fw.append("End\n\n");
  }

  public EntityPagePatternGroup load(String path, DeviceTag deviceTag) throws Exception {
    List lines = FileUtil.loadFileToList(path);
    return load(lines, deviceTag);
  }

  public EntityPagePatternGroup load(List<String> lines, DeviceTag deviceTag) throws Exception {
    EntityPagePatternGroup patterGroup = new EntityPagePatternGroup();
    this.deviceType = null;
    this.devicePolarity = null;

    StringUtil.filterIniLines(lines);
    int line = 0;
    if (line < lines.size()) {
      String str = ((String)lines.get(line)).trim();

      loadCondition(str, patterGroup);
      line++;
    }

    if ((this.deviceType == null) && (deviceTag != null)) {
      this.deviceType = deviceTag.getDeviceType();
      this.devicePolarity = deviceTag.getDevicePolarity();
    }

    EntityPagePattern pagePattern = null;

    List pageLines = new ArrayList();
    for (; line < lines.size(); line++) {
      String string = ((String)lines.get(line)).trim();
      if (string.equalsIgnoreCase("page")) {
        pageLines.clear();
        pageLines.add(string);
        for (; line < lines.size(); line++) {
          string = ((String)lines.get(line)).trim();
          if (string.equalsIgnoreCase("end")) {
            pageLines.add(string);
            pagePattern = loadPagePattern(pageLines);
            patterGroup.addPattern(pagePattern);
            break;
          }
          pageLines.add(string);
        }
      }

    }

    for (EntityPagePattern epp : patterGroup.getPatterns())
    {
      if (epp.getDeviceType() == null) {
        epp.setDeviceType(this.deviceType);
        epp.setDevicePolarity(this.devicePolarity);
      }
    }

    return patterGroup;
  }

    public EntityPagePattern loadPagePattern(List<String> lines) {
        EntityPagePattern pagePattern = new EntityPagePattern();

        for (String string : lines) {
            string = string.replaceAll("\t", " ").trim();
            String[] strs = LoaderUtil.splitAgainstBracket(string, " ", '(', ')');
            if (strs == null || strs.length == 0) {
                continue;
            }
            String key = strs[0];
            if (key.equalsIgnoreCase("y")) {
                String[] yNames = new String[strs.length - 1];
                for (int i = 0; i < yNames.length; i++) {
                    yNames[i] = strs[(i + 1)];
                }
                pagePattern.setYNames(yNames);
            }
            
            
            
            if (strs.length == 1) {
                if (key.equalsIgnoreCase("page")) {
                    pagePattern = new EntityPagePattern();
                }
            } else if (strs.length == 2) {
                if (key.equalsIgnoreCase("name")) {
                    pagePattern.setName(strs[1]);
                } else if (key.equalsIgnoreCase("group")) {
                    pagePattern.setGroupName(strs[1]);
                } else if (key.equalsIgnoreCase("category")) {
                    pagePattern.setCategory(strs[1]);
                } else if (key.equalsIgnoreCase("x")) {
                    Pair p = StringUtil.splitPair(strs[1], "=");
                    if (p != null) {
                        pagePattern.setXName(p.name);
                        pagePattern.setXValuesPattern(new FuncField(p.value));
                    }                  
                } else if (key.equalsIgnoreCase("p")) {
                    Pair p = StringUtil.splitPair(strs[1], "=");
                    if (p != null) {
                        pagePattern.setPName(p.name);
                        pagePattern.setPValuesPattern(new FuncField(p.value));
                    }                  
                } else if (isSimulationPathKey(key)) {
                    pagePattern.setType(2);
                    pagePattern.setSimulationPath(key, strs[1]);
                }else if (key.equalsIgnoreCase("condition")) {
                    for (int i = 1; i < strs.length; i++) {
                        Pair p = StringUtil.splitPair(strs[i], "=");
                        if (p != null) {
                            String name = p.name;
                            String valueStr = p.value;
                            pagePattern.setCondition(name, new FuncField(valueStr));
                        }                     
                    }
                } else if (!key.equalsIgnoreCase("end")) {
                    if (key.equalsIgnoreCase("deviceType")) {
                        pagePattern.setDeviceType(DeviceTypeManager.getDeviceType(strs[1]));
                    } else if (key.equalsIgnoreCase("devicePolarity")) {
                        pagePattern.setDevicePolarity(DeviceTypeManager.getDevicePolarity(strs[1]));
                    }
                } 
            }

        }
        return pagePattern;
    }

  protected boolean isSimulationPathKey(String key) {
    key = key.toLowerCase();
    if ((key.endsWith(EntityPageType.KEY_NETLIST_GEN_PATH)) || (key.endsWith(EntityPageType.KEY_RESULT_READ_PATH)) || (key.endsWith(EntityPageType.KEY_SOURCE_PATH))) {
      return true;
    }
    return false;
  }

  public void loadCondition(String str, EntityPagePatternGroup patterGroup) throws Exception {
    String[] parts = str.split("[ \t]+");
    if (parts.length % 2 != 0) {
      throw new Exception("Failed to load page condition");
    }
    for (int i = 0; i < parts.length; i += 2)
    {
      if ((parts[i].equalsIgnoreCase("CircuitType")) || (parts[i].equalsIgnoreCase("DeviceType")))
        this.deviceType = DeviceTypeManager.getDeviceType(parts[(i + 1)]);
      else if (parts[i].equalsIgnoreCase("DevicePolarity"))
        this.devicePolarity = DeviceTypeManager.getDevicePolarity(parts[(i + 1)]);
      else
        try {
          patterGroup.setVar(parts[i].toLowerCase(), Double.valueOf(Double.parseDouble(parts[(i + 1)])));
        }
        catch (NumberFormatException nfe) {
          patterGroup.setVar(parts[i].toLowerCase(), parts[(i + 1)]);
        }
    }
  }
}
