package com.tagniam.drtsms.database;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by jr on 28/12/17.
 */

public abstract class RouteRoomDatabase extends RoomDatabase {

  public abstract RouteDao routeDao();

  private static RouteRoomDatabase instance;

  static RouteRoomDatabase getDatabase(final Context context) {
    if (instance == null) {
      synchronized (RouteRoomDatabase.class) {
        if (instance == null) {
          instance = Room.databaseBuilder(context.getApplicationContext(), RouteRoomDatabase.class,
              "route").build();
        }
      }
    }
    return instance;
  }

}
