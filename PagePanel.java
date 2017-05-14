/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.utility.common.BaseVarProvider;
import java.util.Map;

/**
 *
 * @author renjing
 */
public interface PagePanel {
    
    void setPagePattern (EntityPagePattern pagePattern);
    
    void setPagePattern (EntityPagePattern pagePattern,Routine routine);
    
    Map<String,EntityPagePattern> getUpdatedPatterns();
    
    IndPagePattern getIndPagePattern();
    
    EntityPagePattern getPagePattern();
    
    BaseVarProvider getVarProvider();
    
}
