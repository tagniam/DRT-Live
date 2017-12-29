package com.tagniam.drtsms.database;

import android.arch.persistence.room.Query;
import java.util.List;

/**
 * Created by jr on 28/12/17.
 */

public interface RouteDao {
  @Query("SELECT * from routes")
  List<Route> getAllRoutes();
}
