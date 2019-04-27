package com.tagniam.drtlive.schedule.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.tagniam.drtlive.schedule.exceptions.StopTimesNotAvailableException;
import com.tagniam.drtlive.schedule.fetcher.ApiScheduleFetcher.Trip;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ApiScheduleTest {

  private static List<Trip> trips;

  @Before
  public void setUp() {
    // Initialize mock list of trips
    trips = new ArrayList<>();

    Trip d1 = new Trip();
    d1.routeId = "900";
    d1.realTime = "12 min";

    Trip d2 = new Trip();
    d2.routeId = "112";
    d2.strTime = "0 min";

    Trip d3 = new Trip();
    d3.routeId = "900";
    d3.strTime = "09:23";

    Trip d4 = new Trip();
    d4.routeId = "112";
    d4.strTime = "23:45";

    Trip d5 = new Trip();
    d5.routeId = "902";
    d5.strTime = "12:23";

    trips.add(d1);
    trips.add(d2);
    trips.add(d3);
    trips.add(d4);
    trips.add(d5);
  }

  @Test
  public void test_getStopNumber() throws StopTimesNotAvailableException {
    ApiSchedule schedule = new ApiSchedule("MOCK1604", departures);
    assertThat(schedule.getStopNumber(), is("MOCK1604"));
  }

  @Test
  public void test_getBusTimes() throws StopTimesNotAvailableException {
    ApiSchedule schedule = new ApiSchedule("MOCK1604", departures);
    assertThat(schedule.getBusTimes().size(), is(3));
  }

  @Test
  public void test_getBusTimes_() throws StopTimesNotAvailableException {
    ApiSchedule schedule = new ApiSchedule("MOCK1604", departures);
    assertThat(schedule.getBusTimes().size(), is(3));
  }
}