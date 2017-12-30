package com.tagniam.drtsms.database;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import android.support.test.InstrumentationRegistry;
import com.tagniam.drtsms.database.routes.Route;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jr on 29/12/17.
 */
public class GtfsRoomDatabaseTest {

  private GtfsRoomDatabase mDatabase;

  @Before
  public void setUp() throws Exception {
    mDatabase = GtfsRoomDatabase.getDatabase(InstrumentationRegistry.getTargetContext());
  }

  @After
  public void tearDown() throws Exception {
    mDatabase.close();
  }

  @Test
  public void loadRoutes() {
    List<Route> routes = mDatabase.routeDao().getAllRoutes();
    assertThat(routes.size(), is(160));
  }
}