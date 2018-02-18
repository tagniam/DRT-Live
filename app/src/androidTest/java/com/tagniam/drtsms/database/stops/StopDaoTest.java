package com.tagniam.drtsms.database.stops;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jr on 18/02/18.
 */
public class StopDaoTest {

  private GtfsRoomDatabase mDatabase;
  private StopDao mDao;

  @Before
  public void setUp() throws Exception {
    mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
        GtfsRoomDatabase.class).build();
    mDao = mDatabase.stopDao();
  }

  @After
  public void tearDown() throws Exception {
    mDatabase.close();
  }

  @Test
  public void test_loadAllStops() throws Exception {
    List<Stop> stops = new ArrayList<>();
    stops.add(new Stop("Pick GO1:1", 43.831147, 1, "2549",
        -79.084237, null, null, null, null,
        "Pickering Station", "0", null));
    stops.add(new Stop("Pick GO2:2", 33.831147, 1, "2548",
        -69.084237, null, null, null, null,
        "Pickering Station 2", "0", null));

    for (Stop stop : stops) {
      mDao.insert(stop);
    }

    List<Stop> dbStops = mDao.loadAllStops();
    for (int i = 0; i < stops.size(); i++) {
      Stop stop = stops.get(i), dbStop = dbStops.get(i);

      assertThat(dbStop.stopId, is(stop.stopId));
      assertThat(dbStop.stopLat, is(stop.stopLat));
      assertThat(dbStop.wheelchairBoarding, is(stop.wheelchairBoarding));
      assertThat(dbStop.stopCode, is(stop.stopCode));
      assertThat(dbStop.stopLon, is(stop.stopLon));
      assertThat(dbStop.stopTimezone, is(stop.stopTimezone));
      assertThat(dbStop.stopUrl, is(stop.stopUrl));
      assertThat(dbStop.parentStation, is(stop.parentStation));
      assertThat(dbStop.stopDesc, is(stop.stopDesc));
      assertThat(dbStop.stopName, is(stop.stopName));
      assertThat(dbStop.locationType, is(stop.locationType));
      assertThat(dbStop.zoneId, is(stop.zoneId));
    }

  }

}