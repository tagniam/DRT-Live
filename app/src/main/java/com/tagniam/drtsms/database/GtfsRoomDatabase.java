package com.tagniam.drtsms.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.fstyle.library.helper.AssetSQLiteOpenHelperFactory;
import com.huma.room_for_asset.RoomAsset;
import com.tagniam.drtsms.database.routes.Route;
import com.tagniam.drtsms.database.routes.RouteDao;

/**
 * Created by jr on 28/12/17.
 */

@Database(entities = {Route.class}, version = 2)
public abstract class GtfsRoomDatabase extends RoomDatabase {

  private static final String DB_NAME = "gtfs.db";
  private static GtfsRoomDatabase instance;
  private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db) {
      new PopulateDbAsync(instance).execute();
    }

  };

  public static GtfsRoomDatabase getDatabase(final Context context) {
    if (instance == null) {
      instance = RoomAsset.databaseBuilder(context.getApplicationContext(), GtfsRoomDatabase.class,
          DB_NAME).build();
    }
    return instance;
  }

  public abstract RouteDao routeDao();

  private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

    private final RouteDao mDao;

    PopulateDbAsync(GtfsRoomDatabase db) {
      mDao = db.routeDao();
    }

    @Override
    protected Void doInBackground(final Void... params) {
      mDao.deleteAll();
      mDao.insert(new Route("Brock","3","ffffff","006633", null, "112_merged_992564", null, null, "112"));

      return null;
    }
  }

}
