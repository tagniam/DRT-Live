package com.tagniam.drtsms.database;

import android.app.Application;
import android.os.AsyncTask;
import java.util.List;

/**
 * Created by jr on 28/12/17.
 */

public class RouteRepository {

  private RouteDao mRouteDao;
  private List<Route> allRoutes;

  RouteRepository(Application application) {
    RouteRoomDatabase db = RouteRoomDatabase.getDatabase(application);
    mRouteDao = db.routeDao();
    allRoutes = mRouteDao.getAllRoutes();
  }

  List<Route> getAllRoutes() {
    return allRoutes;
  }
}
