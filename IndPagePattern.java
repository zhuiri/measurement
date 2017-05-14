/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.EntityPageType;
import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.ep.VarContext;
import com.platformda.iv.MeaDAO;
import com.platformda.iv.MeaData;
import com.platformda.utility.common.BaseVarProvider;
import com.platformda.utility.common.StringUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.jdom2.Element;

/**
 * independent page pattern which has
 *
 * @author Junyi
 */
public class IndPagePattern extends BaseVarProvider implements Cloneable, VarContext {

    EntityPagePattern pagePattern;
    List<EntityPageType> pageTypes = new ArrayList<EntityPageType>();
    int[] pageTypeCache = null;

    @Override
    public Object clone() throws CloneNotSupportedException {
        IndPagePattern clone = (IndPagePattern) super.clone();
        clone.pagePattern = (EntityPagePattern) pagePattern.clone();
        clone.pageTypes = new ArrayList<EntityPageType>();
        clone.vars = new LinkedHashMap<String, Object>();
        copyVars(this, clone);
        return clone;
    }
    
    public EntityPagePattern getPagePattern(){
        return this.pagePattern;
    }
    
//    public void setPagePattern( EntityPagePattern pagePattern){
//        this.pagePattern = pagePattern;
//    }

    public EntityPageType getUniquePageType(EntityPageType pageType, MeaData data, MeaDAO dao) {
        int index = pageTypes.indexOf(pageType);
        if (index < 0) {
            long nextId = data.getNextPageTypeId();
            pageType.setId(nextId);
            pageTypes.add(pageType);
            data.addPageType(pageType);
            dao.savePageType(pageType);
            dao.commit();
        } else {
            pageType = pageTypes.get(index);
        }
        return pageType;
    }

    public List<EntityPageType> getPageTypes() {
        return pageTypes;
    }

    public void updatePageTypes(MeaData meaData) {
        if (pageTypeCache == null) {
            return;
        }
        for (int type : pageTypeCache) {
            EntityPageType pageType = meaData.getPageTypeById(type);
            if (pageType != null) {
                pageTypes.add(pageType);
            }
        }
        pageTypeCache = null;

    }

    public Element asElement(String name) {
        Element elem = new Element(name);

//        StringBuilder builder = new StringBuilder();
//        StringBuilderAppender appender = new StringBuilderAppender(builder);
//        try {
//            PagePatternLoader.save(pagePattern, appender);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//        String[] lines = builder.toString().split("\n");
//        Element linesElem = new Element("lines");
//        elem.addContent(linesElem);
//        for (String line : lines) {
//            Element lineElem = new Element("line");
//            lineElem.setText(line);
//            linesElem.addContent(lineElem);
//        }
        StringBuilder builder = new StringBuilder();
        for (EntityPageType pageType : pageTypes) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(String.valueOf(pageType.getId()));
        }
        elem.setAttribute("types", builder.toString());

        Element varsElem = super.exportTo("vars");
        elem.addContent(varsElem);

        return elem;
    }

    public void fromElement(Element elem) {
//        Element linesElem = elem.getChild("lines");
//        List<Element> lineElems = linesElem.getChildren("line");
//        String[] lines = new String[lineElems.size()];
//        for (int i = 0; i < lines.length; i++) {
//            lines[i] = lineElems.get(i).getText();
//        }
//        PagePatternLoader loader = new PagePatternLoader();
//        pagePattern = loader.loadPagePattern(Arrays.asList(lines));
//        // TODO: device type, device polarity
        String typesStr = elem.getAttributeValue("types");
        if (StringUtil.isValid(typesStr)) {
            String[] parts = typesStr.split(",");
            pageTypeCache = StringUtil.convertStringArrayToIntArray(parts);
        }
        super.importFrom(elem.getChild("vars"));
    }

    @Override
    public Object getVarValue(String key) {
        return getVar(key);
    }
}
