package com.tagniam.drtsms.schedule.fetcher;

import android.content.Context;
import android.content.Intent;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.data.SmsSchedule;
import com.tagniam.drtsms.schedule.exceptions.StopNotFoundException;
import com.tagniam.drtsms.schedule.exceptions.StopTimesNotAvailableException;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/** Created by jr on 01/12/17. */
public class MockScheduleFetcher extends ScheduleFetcher {
  private static final String MOCK_MSG =
      "Stop MOCK1604:\r\n"
          + "Rt 900 WB: 7:09p| 7:25p| 7:40p| 7:54p| 8:09p| 8:29p| 8:49p\r\n"
          + "Rt 916 WB: 7:16p| 7:44p| 8:12p| 8:42p\r\n"
          + "std msg rates may apply";
  private int step;
  private Timer timer = new Timer();

  MockScheduleFetcher(Context context) {
    super(context);
  }

  /**
   * Emulate the sms sending process through timed broadcasts
   *
   * @param stopId id of the DRT stop to be queried for schedule
   */
  @Override
  public void fetch(String stopId) {
    step = 0;
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            // Emulate sending sms
            if (step == 0) {
              getApplicationContext().sendBroadcast(new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT));
            }

            // Emulate delivered sms
            else if (step == 1) {
              getApplicationContext().sendBroadcast(new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED));
            }

            // Emulate success sms, send result back and end timer
            else if (step == 4) {
              // Send a success right away with string
              try {
                Schedule schedule = new SmsSchedule(MOCK_MSG);
                Intent resultIntent = new Intent();
                resultIntent.setAction(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
                resultIntent.putExtra(
                    ScheduleFetcher.SCHEDULE_FETCH_RESULT, (Serializable) schedule);
                getApplicationContext().sendBroadcast(resultIntent);
                timer.cancel();
              } catch (StopTimesNotAvailableException | StopNotFoundException e) {
                // Won't happen
              }
            }

            step++;
          }
        },
        1000,
        1000);
  }
}
