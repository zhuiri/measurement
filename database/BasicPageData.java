/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.database;

import com.iciql.Iciql;

/**
 *
 * @author renjing
 */
@Iciql.IQTable
@Iciql.IQIndexes({@com.iciql.Iciql.IQIndex({"sourceId"}), @com.iciql.Iciql.IQIndex({"sourceId", "pageId"})})
public class BasicPageData {
  @Iciql.IQColumn
  public long sourceId;

  @Iciql.IQColumn
  public long pageId;
  
  @Iciql.IQColumn
  public double p;

  @Iciql.IQColumn
  public double x;

  public long getPageId(){
      return this.pageId;
  }
  
  public long getSourceId(){
      return this.sourceId;
  }
  
  public double getY(int index){
      return 0.0;
  }
  
  public void setY(int index, double value){
      //nothing
  }
  

}
