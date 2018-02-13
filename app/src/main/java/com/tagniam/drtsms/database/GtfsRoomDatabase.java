package com.tagniam.drtsms.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.fstyle.library.helper.AssetSQLiteOpenHelperFactory;
import com.tagniam.drtsms.database.routes.Route;
import com.tagniam.drtsms.database.routes.RouteDao;

/**
 * Created by jr on 28/12/17.
 */

@Database(entities = {Route.class}, version = 1)
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

}
