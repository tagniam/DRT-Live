package com.tagniam.drtsms.schedule.fetcher;

import android.content.Intent;
import android.telephony.SmsManager;
import io.reactivex.ObservableEmitter;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RxSmsScheduleFetcher extends RxScheduleFetcher {

  private final String DRT_PHONE_NO = "8447460497";
  private String stopId;
  private ObservableEmitter<Intent> emitter;

  public RxSmsScheduleFetcher(String stopId) {
    this.stopId = stopId;
  }

  @Override
  public void subscribe(ObservableEmitter<Intent> emitter) {
    this.emitter = emitter;
    SmsManager smsSender = SmsManager.getDefault();
    smsSender.sendTextMessage(DRT_PHONE_NO, null, stopId, null, null);
    emitter.onNext(new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT));
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onEvent(Intent intent) {
    if (ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION.equals(intent.getAction())) {
      emitter.onNext(intent);
      emitter.onComplete();
    } else if (ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION.equals(intent.getAction())) {
      emitter.onError(new Exception());
    }
  }
}
