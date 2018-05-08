package com.tagniam.drtsms.schedule.data;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.tagniam.drtsms.schedule.fetcher.ApiScheduleFetcher.Departure;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ApiBusTimeTest {

  private static List<Departure> departures;

  @Before
  public void setUp() {
    // Initialize test data departures
    departures = new ArrayList<>();

    Departure d1 = new Departure();
    d1.route = "900";
    d1.strTime = "12 min";
    Departure d2 = new Departure();
    d2.route = "900";
    d2.strTime = "09:23";

    departures.add(d1);
    departures.add(d2);
  }

  @Test
  public void getRoute() {
    ApiBusTime apiBusTime = new ApiBusTime(departures);
    assertThat(apiBusTime.getRoute(), is("900"));
  }

  @Test
  public void getDirection() {
    ApiBusTime apiBusTime = new ApiBusTime(departures);
    assertThat(apiBusTime.getDirection(), is(""));
  }

  @Test
  public void getTimes() {
    Calendar cal = Calendar.getInstance();
    ApiBusTime apiBusTime = new ApiBusTime(departures);
    List<Date> times = apiBusTime.getTimes();

    cal.add(Calendar.MINUTE, 12);
    Date nowPlusTwelveMins = cal.getTime();
    assertTrue(times.get(0).equals(nowPlusTwelveMins));

    cal.setTime(times.get(1));
    assertThat(cal.get(Calendar.HOUR), is(9));
    assertThat(cal.get(Calendar.MINUTE), is(23));
    assertThat(cal.get(Calendar.AM_PM), is(Calendar.AM));

  }
}