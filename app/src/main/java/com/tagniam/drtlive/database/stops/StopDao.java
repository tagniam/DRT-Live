package com.tagniam.drtlive.database.stops;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by jr on 18/02/18.
 */

@Dao
public interface StopDao {

  @Insert
  void insert(Stop stop);

  @Query("SELECT * FROM stops")
  List<Stop> loadAllStops();

  @Query("SELECT * FROM stops WHERE stop_name LIKE :query OR stop_code LIKE :query LIMIT 10")
  List<Stop> findStopsByNameOrId(String query);

  @Query("SELECT stop_name FROM stops WHERE stop_code = :stopCode")
  String getStopName(String stopCode);
}
