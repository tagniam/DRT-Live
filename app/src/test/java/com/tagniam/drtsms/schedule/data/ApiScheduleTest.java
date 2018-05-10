package com.tagniam.drtsms.schedule.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.tagniam.drtsms.schedule.exceptions.StopTimesNotAvailableException;
import com.tagniam.drtsms.schedule.fetcher.ApiScheduleFetcher.Departure;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ApiScheduleTest {

  private static List<Departure> departures;

  @Before
  public void setUp() {
    // Initialize mock list of departures
    departures = new ArrayList<>();

    Departure d1 = new Departure();
    d1.route = "900";
    d1.strTime = "12 min";
    Departure d2 = new Departure();
    d2.route = "112";
    d2.strTime = "0 min";
    Departure d3 = new Departure();
    d3.route = "900";
    d3.strTime = "09:23";
    Departure d4 = new Departure();
    d4.route = "112";
    d4.strTime = "23:45";
    Departure d5 = new Departure();
    d5.route = "902";
    d5.strTime = "12:23";

    departures.add(d1);
    departures.add(d2);
    departures.add(d3);
    departures.add(d4);
    departures.add(d5);
  }

  @Test
  public void test_getStopNumber() throws StopTimesNotAvailableException {
    ApiSchedule schedule = new ApiSchedule(Calendar.getInstance(), "MOCK1604", departures);
    assertThat(schedule.getStopNumber(), is("MOCK1604"));
  }

  @Test
  public void test_getBusTimes() throws StopTimesNotAvailableException {
    ApiSchedule schedule = new ApiSchedule(Calendar.getInstance(), "MOCK1604", departures);
    assertThat(schedule.getBusTimes().size(), is(3));
  }

  @Test
  public void test_getBusTimes_() throws StopTimesNotAvailableException {
    ApiSchedule schedule = new ApiSchedule(Calendar.getInstance(), "MOCK1604", departures);
    assertThat(schedule.getBusTimes().size(), is(3));
  }
}