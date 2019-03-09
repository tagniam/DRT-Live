package com.tagniam.drtlive.database.routes;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

/**
 * Created by jr on 28/12/17.
 */

@Dao
public interface RouteDao {

  @Insert
  void insert(Route route);

  @Query("SELECT route_long_name FROM routes WHERE route_short_name = :shortName LIMIT 1")
  String findLongNameByShortName(String shortName);

}
