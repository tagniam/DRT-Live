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
  public void test_getRoute() {
    ApiBusTime apiBusTime = new ApiBusTime(Calendar.getInstance(), departures);
    assertThat(apiBusTime.getRoute(), is("900"));
  }

  @Test
  public void test_getDirection() {
    ApiBusTime apiBusTime = new ApiBusTime(Calendar.getInstance(), departures);
    assertThat(apiBusTime.getDirection(), is(""));
  }

  @Test
  public void test_getTimes_sameDay() {
    Calendar now = Calendar.getInstance();

    // Set up mock date for 2018/04/09 08:00
    now.set(Calendar.YEAR, 2018);
    now.set(Calendar.MONTH, 4);
    now.set(Calendar.DATE, 9);
    now.set(Calendar.HOUR_OF_DAY, 8);
    now.set(Calendar.MINUTE, 0);

    ApiBusTime apiBusTime = new ApiBusTime(now, departures);
    List<Date> times = apiBusTime.getTimes();

    // Check first
    Calendar first = (Calendar) now.clone();
    first.set(Calendar.MINUTE, 12);
    assertTrue(isSameDay(first.getTime(), times.get(0)));
    assertTrue(isSameTime(first.getTime(), times.get(0)));

    // Check second
    Calendar second = (Calendar) now.clone();
    second.set(Calendar.HOUR_OF_DAY, 9);
    second.set(Calendar.MINUTE, 23);
    assertTrue(isSameDay(second.getTime(), times.get(1)));
    assertTrue(isSameTime(second.getTime(), times.get(1)));
  }

  @Test
  public void test_getTimes_oneNextDay() {
    Calendar now = Calendar.getInstance();

    // Set up mock date for 2018/04/09 23:00
    now.set(Calendar.YEAR, 2018);
    now.set(Calendar.MONTH, 4);
    now.set(Calendar.DATE, 9);
    now.set(Calendar.HOUR_OF_DAY, 23);
    now.set(Calendar.MINUTE, 0);

    ApiBusTime apiBusTime = new ApiBusTime(now, departures);
    List<Date> times = apiBusTime.getTimes();

    // Check first
    Calendar first = (Calendar) now.clone();
    first.set(Calendar.MINUTE, 12);
    assertTrue(isSameDay(first.getTime(), times.get(0)));
    assertTrue(isSameTime(first.getTime(), times.get(0)));

    // Check second
    Calendar second = (Calendar) now.clone();
    second.set(Calendar.HOUR_OF_DAY, 9);
    second.set(Calendar.MINUTE, 23);
    assertTrue(isNextDay(times.get(1), second.getTime()));
    assertTrue(isSameTime(second.getTime(), times.get(1)));
  }

  private boolean isSameDay(Date d1, Date d2) {
    Calendar c1 = Calendar.getInstance();
    c1.setTime(d1);
    Calendar c2 = Calendar.getInstance();
    c2.setTime(d2);
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
        c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
        c1.get(Calendar.DATE) == c2.get(Calendar.DATE);
  }

  private boolean isNextDay(Date d1, Date d2) {
    Calendar c1 = Calendar.getInstance();
    c1.setTime(d1);
    Calendar c2 = Calendar.getInstance();
    c2.setTime(d2);
    c2.add(Calendar.DATE, 1);
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
        c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
        c1.get(Calendar.DATE) == c2.get(Calendar.DATE);
  }

  private boolean isSameTime(Date d1, Date d2) {
    Calendar c1 = Calendar.getInstance();
    c1.setTime(d1);
    Calendar c2 = Calendar.getInstance();
    c2.setTime(d2);
    return c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) &&
        c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);
  }
}