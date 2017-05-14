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

@Iciql.IQTable(inheritColumns = true)
//@Iciql.IQIndexes({@com.iciql.Iciql.IQIndex({"sourceId"}), @com.iciql.Iciql.IQIndex({"sourceId", "pageId"})})
public class AnalogPageData extends BasicPageData
{

//  @Iciql.IQColumn
//  public long sourceId;
//
//  @Iciql.IQColumn
//  public long pageId;
//
//  @Iciql.IQColumn
//  public double p;
//
//  @Iciql.IQColumn
//  public double x;

  @Iciql.IQColumn
  public double y;

  @Override
  public double getY(int index)
  {
    return this.y;
  }

  @Override
  public void setY(int index, double value) {
    this.y = value;
  }
}