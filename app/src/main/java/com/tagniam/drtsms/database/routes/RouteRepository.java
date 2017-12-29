package com.tagniam.drtsms.database.routes;

import android.app.Application;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import java.util.List;

/**
 * Created by jr on 28/12/17.
 */

public class RouteRepository {

  private RouteDao mRouteDao;
  private List<Route> allRoutes;

  RouteRepository(Application application) {
    GtfsRoomDatabase db = GtfsRoomDatabase.getDatabase(application);
    mRouteDao = db.routeDao();
    allRoutes = mRouteDao.getAllRoutes();
  }

  List<Route> getAllRoutes() {
    return allRoutes;
  }
}
