package com.tagniam.drtsms.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by jr on 28/12/17.
 */

@Entity(tableName = "routes")
public class Route {
  @PrimaryKey
  public int id;
  public String routeLongName;
  public int routeType;
  public String routeTextColor;
  public String routeColor;
  public String agencyId;
  public String routeId;
  public String routeUrl;
  public String routeDesc;
  public String routeShortName;

  public Route(String routeLongName, int routeType, String routeTextColor,
      String routeColor, String agencyId, String routeId, String routeUrl, String routeDesc,
      String routeShortName) {
    this.routeLongName = routeLongName;
    this.routeType = routeType;
    this.routeTextColor = routeTextColor;
    this.routeColor = routeColor;
    this.agencyId = agencyId;
    this.routeId = routeId;
    this.routeUrl = routeUrl;
    this.routeDesc = routeDesc;
    this.routeShortName = routeShortName;
  }




}
