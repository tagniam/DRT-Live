package com.tagniam.drtlive.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import com.fstyle.library.helper.AssetSQLiteOpenHelperFactory;
import com.tagniam.drtlive.database.routes.Route;
import com.tagniam.drtlive.database.routes.RouteDao;
import com.tagniam.drtlive.database.stops.Stop;
import com.tagniam.drtlive.database.stops.StopDao;

/**
 * Created by jr on 28/12/17.
 */

@Database(entities = {Route.class, Stop.class}, version = 1, exportSchema = false)
public abstract class GtfsRoomDatabase extends RoomDatabase {

  private static final String DB_NAME = "gtfs.db";
  private static GtfsRoomDatabase instance;

  public static GtfsRoomDatabase getDatabase(final Context context) {
    if (instance == null) {
      instance = Room.databaseBuilder(context.getApplicationContext(), GtfsRoomDatabase.class,
          DB_NAME).openHelperFactory(new AssetSQLiteOpenHelperFactory()).build();
    }
    return instance;
  }

  public abstract RouteDao routeDao();

  public abstract StopDao stopDao();

}
