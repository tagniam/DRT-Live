package com.tagniam.drtsms.database.stops;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by jr on 18/02/18.
 */

@Entity(tableName = "stops")
public class Stop {

  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "stop_id")
  public String stopId;

  @ColumnInfo(name = "stop_lat")
  public double stopLat;
  @ColumnInfo(name = "wheelchair_boarding")
  public int wheelchairBoarding;
  @ColumnInfo(name = "stop_code")
  public String stopCode;
  @ColumnInfo(name = "stop_lon")
  public double stopLon;
  @ColumnInfo(name = "stop_timezone")
  public String stopTimezone;
  @ColumnInfo(name = "stop_url")
  public String stopUrl;
  @ColumnInfo(name = "parent_station")
  public String parentStation;
  @ColumnInfo(name = "stop_desc")
  public String stopDesc;
  @ColumnInfo(name = "stop_name")
  public String stopName;
  @ColumnInfo(name = "location_type")
  public String locationType;
  @ColumnInfo(name = "zone_id")
  public String zoneId;

  public Stop(@NonNull String stopId, double stopLat, int wheelchairBoarding,
      String stopCode, double stopLon, String stopTimezone, String stopUrl,
      String parentStation, String stopDesc, String stopName, String locationType,
      String zoneId) {
    this.stopId = stopId;
    this.stopLat = stopLat;
    this.wheelchairBoarding = wheelchairBoarding;
    this.stopCode = stopCode;
    this.stopLon = stopLon;
    this.stopTimezone = stopTimezone;
    this.stopUrl = stopUrl;
    this.parentStation = parentStation;
    this.stopDesc = stopDesc;
    this.stopName = stopName;
    this.locationType = locationType;
    this.zoneId = zoneId;
  }

}
