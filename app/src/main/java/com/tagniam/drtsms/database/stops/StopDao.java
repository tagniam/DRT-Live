package com.tagniam.drtsms.database.stops;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.database.Cursor;
import io.reactivex.Single;
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

  @Query("SELECT stop_id AS _id, * FROM stops WHERE stop_name LIKE :query OR stop_code LIKE :query LIMIT 10")
  Cursor findStopsByNameOrId(String query);
}
