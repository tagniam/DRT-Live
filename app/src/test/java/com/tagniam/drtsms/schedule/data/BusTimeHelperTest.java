package com.tagniam.drtsms.schedule.data;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;

/**
 * Created by jr on 08/12/17.
 */
public class BusTimeHelperTest {


  @Test
  public void test_getRelativeTime_hrOnly() {
    Calendar cal = Calendar.getInstance();
    Date now = cal.getTime();

    cal.add(Calendar.HOUR, 5);
    Date next = cal.getTime();

    assertEquals("5 hr", BusTime.Helper.getRelativeTime(now, next));
  }

  @Test
  public void test_getRelativeTime_minOnly() {
    Calendar cal = Calendar.getInstance();
    Date now = cal.getTime();

    cal.add(Calendar.MINUTE, 45);
    Date next = cal.getTime();

    assertEquals("45 min", BusTime.Helper.getRelativeTime(now, next));
  }

  @Test
  public void test_getRelativeTime_hrAndMin() {
    Calendar cal = Calendar.getInstance();
    Date now = cal.getTime();

    cal.add(Calendar.HOUR, 2);
    cal.add(Calendar.MINUTE, 34);
    Date next = cal.getTime();

    assertEquals("2 hr 34 min", BusTime.Helper.getRelativeTime(now, next));
  }

}