package com.tagniam.drtsms.database.stops;

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
}