package com.tagniam.drtsms.schedule.data;

import static org.junit.Assert.assertEquals;

import com.tagniam.drtsms.schedule.exceptions.StopNotFoundException;
import com.tagniam.drtsms.schedule.exceptions.StopTimesNotAvailableException;
import org.junit.Test;

/**
 * Created by jr on 06/12/17.
 */
public class SmsScheduleTest {

  private final String TEST_MSG =
      "Stop MOCK1604:\r\n"
          + "Rt 900 WB: 7:09p| 7:25p| 7:40p| 7:54p| 8:09p| 8:29p| 8:49p\r\n"
          + "Rt 916 WB: 7:16p| 7:44p| 8:12p| 8:42p\r\n"
          + "std msg rates may apply";
  private final String ERROR_MSG = "Stop Number 93108 not found.";

  @Test
  public void test_getStopNumber() throws StopNotFoundException, StopTimesNotAvailableException {
    Schedule schedule = new SmsSchedule(TEST_MSG);
    assertEquals("MOCK1604", schedule.getStopNumber());
  }

  @Test
  public void test_getBusTimes() throws StopNotFoundException, StopTimesNotAvailableException {
    Schedule schedule = new SmsSchedule(TEST_MSG);
    assertEquals(2, schedule.getBusTimes().size());
  }

  @Test(expected = StopNotFoundException.class)
  public void test_invalidStopNumber()
      throws StopNotFoundException, StopTimesNotAvailableException {
    Schedule schedule = new SmsSchedule(ERROR_MSG);
  }
}
