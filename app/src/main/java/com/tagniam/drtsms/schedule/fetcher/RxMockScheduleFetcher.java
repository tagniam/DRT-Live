package com.tagniam.drtsms.schedule.fetcher;

import android.content.Intent;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.data.SmsSchedule;
import com.tagniam.drtsms.schedule.exceptions.StopNotFoundException;
import com.tagniam.drtsms.schedule.exceptions.StopTimesNotAvailableException;
import io.reactivex.ObservableEmitter;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

public class RxMockScheduleFetcher extends RxScheduleFetcher {

  private static final String MOCK_MSG =
      "Stop MOCK1604:\r\n"
          + "Rt 900 WB: 7:09p| 7:25p| 7:40p| 7:54p| 8:09p| 8:29p| 8:49p\r\n"
          + "Rt 916 WB: 7:16p| 7:44p| 8:12p| 8:42p\r\n"
          + "std msg rates may apply";
  private int step;
  private Timer timer = new Timer();

  @Override
  public void subscribe(final ObservableEmitter<Intent> emitter) throws Exception {
       step = 0;
    timer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            // Emulate sending sms
            if (step == 0) {
              emitter.onNext((new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT)));
            }

            // Emulate delivered sms
            else if (step == 1) {
              emitter.onNext((new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED)));
            }

            // Emulate success sms, send result back and end timer
            else if (step == 4) {
              // Send a success right away with string
              try {
                Schedule schedule = new SmsSchedule(MOCK_MSG);
                Intent resultIntent = new Intent(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
                resultIntent.putExtra(
                    ScheduleFetcher.SCHEDULE_FETCH_RESULT, (Serializable) schedule);

                emitter.onNext(resultIntent);
                emitter.onComplete();
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
