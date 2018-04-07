package com.tagniam.drtsms.schedule.fetcher;

import android.content.Intent;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.data.SmsSchedule;
import com.tagniam.drtsms.schedule.exceptions.StopNotFoundException;
import io.reactivex.ObservableEmitter;
import java.io.Serializable;

public class RxMockScheduleFetcher extends RxScheduleFetcher {

  private static final String MOCK_MSG =
      "Stop MOCK1604:\r\n"
          + "Rt 900 WB: 7:09p| 7:25p| 7:40p| 7:54p| 8:09p| 8:29p| 8:49p\r\n"
          + "Rt 916 WB: 7:16p| 7:44p| 8:12p| 8:42p\r\n"
          + "std msg rates may apply";
  private String stopId;

  public RxMockScheduleFetcher(String stopId) {
    this.stopId = stopId;
  }

  @Override
  public void subscribe(final ObservableEmitter<Intent> emitter) {
    try {
      Thread.sleep(1000);
      emitter.onNext(new Intent(RxSmsScheduleFetcher.Intents.SMS_SENT));

      Thread.sleep(1000);
      emitter.onNext(new Intent(RxSmsScheduleFetcher.Intents.SMS_DELIVERED));

      Thread.sleep(2000);
      if (android.text.TextUtils.isDigitsOnly(stopId)) {
        Schedule schedule = new SmsSchedule(MOCK_MSG);
        Intent resultIntent = new Intent(Intents.SUCCESS_ACTION);
        resultIntent.putExtra(
            Intents.RESULT_EXTRA, (Serializable) schedule);

        emitter.onNext(resultIntent);
        emitter.onComplete();
      } else {
        throw new StopNotFoundException();
      }

    } catch (Exception e) {
      emitter.onError(e);
    }
  }

  @Override
  public void onPause() {

  }

  @Override
  public void onResume() {

  }
}
