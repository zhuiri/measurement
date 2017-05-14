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
public class AnalogMultiYPageData extends BasicPageData
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

  @Iciql.IQColumn
  public double y3;

  @Iciql.IQColumn
  public double y4;

  @Iciql.IQColumn
  public double y5;

  @Iciql.IQColumn
  public double y6;

  @Iciql.IQColumn
  public double y7;

  @Iciql.IQColumn
  public double y8;
  
  @Override
  public double getY(int index)
  {
    switch (index) {
    case 0:
      return this.y1;
    case 1:
      return this.y2;
    case 2:
      return this.y3;
    case 3:
      return this.y4;
    case 4:
      return this.y5;
    case 5:
      return this.y6;
    case 6:
      return this.y7;
    case 7:
      return this.y8;
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
    case 2:
      this.y3 = value;
      break;
    case 3:
      this.y4 = value;
      break;
    case 4:
      this.y5 = value;
      break;
    case 5:
      this.y6 = value;
      break;
    case 6:
      this.y7 = value;
      break;
    case 7:
      this.y8 = value;
      break;
    }
  }
}
