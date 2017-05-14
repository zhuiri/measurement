/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.admin;

import com.platformda.datacore.pattern.EntityPagePattern;
import com.platformda.iv.measure.MeaDevice;
import com.platformda.iv.measure.MeaDeviceGroup;
import com.platformda.iv.measure.MeaSubDie;
import com.platformda.spec.SpecPattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Junyi
 */
public class DUTPack implements RoutineSelector {

    Routine routine;
    List<EntityPagePattern> pagePatterns = new ArrayList<EntityPagePattern>();
    List<SpecPattern> specPatterns = new ArrayList<SpecPattern>();

    public DUTPack(Routine routine) {
        this.routine = routine;
    }

    public Routine getRoutine() {
        return routine;
    }

    public boolean isEmpty() {
        return pagePatterns.isEmpty() && specPatterns.isEmpty();
    }
    
    public List<EntityPagePattern> getSelectedPagePatterns() {
        return pagePatterns;
    }

    public List<SpecPattern> getSelectedSpecPatterns() {
        return specPatterns;
    }

    public void clear() {
        pagePatterns.clear();
        specPatterns.clear();
    }

    public void selectAllPages(Routine routine) {       
        if(this.routine == routine){
           pagePatterns.clear();
           pagePatterns.addAll(routine.getPagePatterns());
        }
    }
    
    public void deSelectAllPages(Routine routine){
         if(this.routine == routine){
               pagePatterns.clear();
         }       
    }
    
    
    public void selectAllSpecs(Routine routine) {       
        if(this.routine == routine){
           specPatterns.clear();
           specPatterns.addAll(routine.getSpecPatterns());
        }
    }
    
    public void deSelectAllSpecs(Routine routine){
         if(this.routine == routine){
               specPatterns.clear();
         }       
    }   
    
    private List<EntityPagePattern> sortAsRoutinePatterns(List<EntityPagePattern> patterns){
        final List<EntityPagePattern> routinePatterns = routine.getPagePatterns();
         Collections.sort(patterns, new Comparator<EntityPagePattern>(){
             @Override 
             public int compare(EntityPagePattern o1,EntityPagePattern o2){
                 int result = 1;
                 int index1 = routinePatterns.indexOf(o1);
                 int index2 = routinePatterns.indexOf(o2);
                 if(index1>index2){
                     result =  1;
                 }else if (index1<index2){
                     result =  -1;
                 }else if (index1 == index2){
                     result =  0;
                 }                 
                 return result;
             }
         });         
         return patterns;        
    }

    public void addPagePattern(EntityPagePattern pagePattern) {
        pagePatterns.add(pagePattern);
        //sort it as routine patterns
        pagePatterns = sortAsRoutinePatterns(pagePatterns);
    }
    
    public void removePagePattern(EntityPagePattern pagePattern) {
        for(int index = pagePatterns.size()-1;index>=0;index--){
            if(pagePatterns.get(index) == pagePattern){
                pagePatterns.remove(index);
                return;
            }
        }
//        pagePatterns.remove(pagePattern);
    }
    
    private List<SpecPattern> sortAsRoutineSpecs(List<SpecPattern> patterns){
        final List<SpecPattern> routinePatterns = routine.getSpecPatterns();
         Collections.sort(patterns, new Comparator<SpecPattern>(){
             @Override 
             public int compare(SpecPattern o1,SpecPattern o2){
                 int result = 1;
                 int index1 = routinePatterns.indexOf(o1);
                 int index2 = routinePatterns.indexOf(o2);
                 if(index1>index2){
                     result =  1;
                 }else if (index1<index2){
                     result =  -1;
                 }else if (index1 == index2){
                     result =  0;
                 }                 
                 return result;
             }
         });         
         return patterns;        
    }
    
    public void addSpecPattern(SpecPattern specPattern) {
        specPatterns.add(specPattern);
        //sort it 
        specPatterns = sortAsRoutineSpecs(specPatterns);
    }

    public void removeSpecPattern(SpecPattern specPattern) {
        specPatterns.remove(specPattern);
    }

    @Override
    public boolean isSelected(Routine routine) {
        return routine == this.routine;
    }

    @Override
    public boolean isSelected(Routine routine, EntityPagePattern pagePattern) {
        if (routine == this.routine) {
            //should not using contains 
            //because contains method in ArrayList using equals
            //while in the EntityPagePattern equals means the same value
            //cant distingush two pattern which have the same value
            //return pagePatterns.contains(pagePattern);
            for(int index =0;index<pagePatterns.size();index++){
                if(pagePatterns.get(index) == pagePattern){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isSelected(Routine routine, SpecPattern specPattern) {
        if (routine == this.routine) {
            return specPatterns.contains(specPattern);
        }
        return false;
    }

    @Override
    public boolean isSelected(MeaSubDie subDie) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSelected(MeaDeviceGroup deviceGroup) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSelected(MeaDevice meaDevice) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isSelected(MeaDevice meaDevice, Routine routine) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
