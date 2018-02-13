package com.tagniam.drtsms.database.routes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import java.util.List;
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
  public void setUp() throws Exception {
    mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
        GtfsRoomDatabase.class).build();
    mDao = mDatabase.routeDao();
  }

  @After
  public void tearDown() throws Exception {
    mDatabase.close();
  }

  @Test
  public void test_findLongNameByShortName() throws Exception {
    mDao.insert(new Route("PULSE", "3", "000000",
       "00ffcc", null, "900_merged_992530", null,
        null, "900"));
    String longName = mDao.findLongNameByShortName("900");
    assertThat(longName, is("PULSE"));
  }

}