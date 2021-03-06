package com.tagniam.drtlive.database.routes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.tagniam.drtlive.database.GtfsRoomDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by jr on 29/12/17.
 */
@RunWith(AndroidJUnit4.class)
public class RouteDaoTest {

  private GtfsRoomDatabase mDatabase;
  private RouteDao mDao;

  @Before
  public void setUp() {
    mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
        GtfsRoomDatabase.class).build();
    mDao = mDatabase.routeDao();
  }

  @After
  public void tearDown() {
    mDatabase.close();
  }

  @Test
  public void test_findLongNameByShortName() {
    mDao.insert(new Route("PULSE", "3", "000000",
        "00ffcc", null, "900_merged_992530", null,
        null, "900"));
    String longName = mDao.findLongNameByShortName("900");
    assertThat(longName, is("PULSE"));
  }

}