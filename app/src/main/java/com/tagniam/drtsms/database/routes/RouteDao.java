package com.tagniam.drtsms.database.routes;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import java.util.List;

/**
 * Created by jr on 28/12/17.
 */

@Dao
public interface RouteDao {

  @Insert
  void insert(Route route);

  @Query("DELETE FROM routes")
  void deleteAll();

  @Query("SELECT * FROM routes")
  List<Route> getAllRoutes();
}
