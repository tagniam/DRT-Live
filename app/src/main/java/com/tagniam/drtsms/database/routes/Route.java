package com.tagniam.drtsms.database.routes;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.util.TableInfo.Column;
import android.support.annotation.NonNull;

/**
 * Created by jr on 28/12/17.
 */

@Entity(tableName = "routes")
public class Route {

  @PrimaryKey @NonNull
  @ColumnInfo(name = "route_id")          public String routeId;

  @ColumnInfo(name = "route_long_name")   public String routeLongName;
  @ColumnInfo(name = "route_type")        public String routeType;
  @ColumnInfo(name = "route_text_color")  public String routeTextColor;
  @ColumnInfo(name = "route_color")       public String routeColor;
  @ColumnInfo(name = "agency_id")         public String agencyId;
  @ColumnInfo(name = "route_url")         public String routeUrl;
  @ColumnInfo(name = "route_desc")        public String routeDesc;
  @ColumnInfo(name = "route_short_name")  public String routeShortName;

  public Route(String routeLongName, String routeType, String routeTextColor,
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
