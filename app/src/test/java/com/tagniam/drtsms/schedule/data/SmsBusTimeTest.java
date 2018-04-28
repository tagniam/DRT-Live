package com.tagniam.drtsms.schedule.data;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Test;

/** Created by jr on 06/12/17. */
public class SmsBusTimeTest {

  private final String TEST_MSG_1 = "Rt 900 WB: 7:09p| 12:25a| 5:59a";
  private final String TEST_MSG_2 = "Rt 916 Counter Clockwise: 7:16p| 7:44p| 8:12p| 8:42p";

  @Test
  public void test_getRoute() {
    BusTime busTime = new SmsBusTime(TEST_MSG_1);
    BusTime busTime2 = new SmsBusTime(TEST_MSG_2);
    assertEquals("900", busTime.getRoute());
    assertEquals("916", busTime2.getRoute());
  }

  @Test
  public void test_getDirection() {
    BusTime busTime = new SmsBusTime(TEST_MSG_1);
    BusTime busTime2 = new SmsBusTime(TEST_MSG_2);

    assertEquals("Westbound", busTime.getDirection());
    assertEquals("Counter Clockwise", busTime2.getDirection());
  }

  @Test
  public void test_getTimes() {
    Calendar cal = Calendar.getInstance();
    BusTime busTime = new SmsBusTime(TEST_MSG_1);
    List<Date> times = busTime.getTimes();

    // Check each time individually
    // Should be 7:09 pm
    cal.setTime(times.get(0));
    assertEquals(7, cal.get(Calendar.HOUR));
    assertEquals(9, cal.get(Calendar.MINUTE));
    assertEquals(Calendar.PM, cal.get(Calendar.AM_PM));

    // Should be 12:25 am
    cal.setTime(times.get(1));
    assertEquals(0, cal.get(Calendar.HOUR));
    assertEquals(25, cal.get(Calendar.MINUTE));
    assertEquals(Calendar.AM, cal.get(Calendar.AM_PM));

    // Should be 5:59 am
    cal.setTime(times.get(2));
    assertEquals(5, cal.get(Calendar.HOUR));
    assertEquals(59, cal.get(Calendar.MINUTE));
    assertEquals(Calendar.AM, cal.get(Calendar.AM_PM));
  }
}
