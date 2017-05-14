/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.platformda.iv.database;

import com.iciql.Iciql;

@Iciql.IQTable(inheritColumns = true)
//@Iciql.IQIndexes({@com.iciql.Iciql.IQIndex({"sourceId"}), @com.iciql.Iciql.IQIndex({"sourceId", "pageId"})})
public class AnalogDoubleYPageData extends BasicPageData
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
  public double y1;

  @Iciql.IQColumn
  public double y2;

  @Override
  public double getY(int index)
  {
    switch (index) {
    case 0:
      return this.y1;
    case 1:
      return this.y2;
    }
    return (0.0D / 0.0D);
  }

  @Override
  public void setY(int index, double value)
  {
    switch (index) {
    case 0:
      this.y1 = value;
      break;
    case 1:
      this.y2 = value;
      break;
    }
  }
}
