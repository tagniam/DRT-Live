package com.tagniam.drtsms.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

/**
 * Created by jr on 28/12/17.
 */

@Database(entities = {Route.class}, version = 1)
public abstract class GtfsRoomDatabase extends RoomDatabase {

  public abstract RouteDao routeDao();

  private static GtfsRoomDatabase instance;

  static GtfsRoomDatabase getDatabase(final Context context) {
    if (instance == null) {
      synchronized (GtfsRoomDatabase.class) {
        if (instance == null) {
          instance = Room.databaseBuilder(context.getApplicationContext(), GtfsRoomDatabase.class,
              "route").addCallback(sRoomDatabaseCallback).build();
        }
      }
    }
    return instance;
  }

  private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

    @Override
    public void onOpen(@NonNull SupportSQLiteDatabase db) {
      new PopulateDbAsync(instance).execute();
    }

  };

  private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

    private final RouteDao mDao;

    PopulateDbAsync(GtfsRoomDatabase db) {
      mDao = db.routeDao();
    }

    @Override
    protected Void doInBackground(final Void... params) {
      mDao.deleteAll();
      mDao.insert(new Route("Brock",3,"ffffff","006633", null, "112_merged_992564", null, null, "112"));

      return null;
    }
  }

}
