/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.spec.SpecPattern;
import com.platformda.utility.common.BaseVarProvider;
import org.jdom2.Element;

/**
 *
 * @author Junyi
 */
public class IndSpecPattern extends BaseVarProvider {

    SpecPattern specPattern;

    public Element asElement(String name) {
        Element elem = new Element(name);

        Element varsElem = super.exportTo("vars");
        elem.addContent(varsElem);

        return elem;
    }

    public void fromElement(Element elem) {
        super.importFrom(elem.getChild("vars"));
    }
}
