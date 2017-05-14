package com.platformda.iv.analysis;

import com.platformda.ep.EvaluatorVisitor;
import com.platformda.ep.Node;
import com.platformda.ep.PEP;
import com.platformda.ep.PEPUtil;
import com.platformda.ep.VarContext;
import com.platformda.utility.common.StringUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Expression Output
 *
 * @author Junyi
 */
public class AdvMeaOutput implements Cloneable {

    private String name;
    private String exp = "";
    private Node node;
    //
    private String[] varNames;
    private boolean asOutput = true;

    public AdvMeaOutput(String name) {
        this.name = name;
    }

    public void setExp(String exp) {
        this.exp = exp;
        PEP pep = PEP.getInstance();
        pep.parseExpression(exp);
        node = pep.getTopNode();
        List<String> vars = new ArrayList<String>();
        PEPUtil.fetchVarNames(node, vars);
        varNames = vars.toArray(new String[vars.size()]);
    }

    public String[] getVarNames() {
        return varNames;
    }

    public double getValue(final double[] meaValues) {
        if (meaValues.length != varNames.length) {
            return Double.NaN;
        }
        VarContext varContext = new VarContext() {
            @Override
            public Object getVarValue(String key) {
                int index = StringUtil.indexOf(key, varNames);
                return meaValues[index];
            }
        };
        EvaluatorVisitor ev = new EvaluatorVisitor();
        Object obj;
        try {
            obj = ev.getValue(node, null, varContext, 1);
            // TODO: obj instanceof List
            if (obj instanceof Number) {
                return ((Number) obj).doubleValue();
            }

        } catch (Exception ex) {
        }
        return Double.NaN;
    }

    public String getName() {
        return name;
    }

    public String getExp() {
        return exp;
    }

    @Override
    public Object clone() {
        try {
            AdvMeaOutput output = (AdvMeaOutput) super.clone();
            return output;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean asOutput() {
        return asOutput;
    }

    public void setAsOutput(boolean asOutput) {
        this.asOutput = asOutput;
    }
}
