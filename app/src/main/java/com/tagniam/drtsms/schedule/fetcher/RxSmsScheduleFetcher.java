package com.tagniam.drtsms.schedule.fetcher;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import io.reactivex.ObservableEmitter;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RxSmsScheduleFetcher extends RxScheduleFetcher {

  private final String DRT_PHONE_NO = "8447460497";
  private Context context;
  private String stopId;
  private ObservableEmitter<Intent> emitter;

  public RxSmsScheduleFetcher(Context context, String stopId) {
    this.stopId = stopId;
    this.context = context;
  }

  @Override
  public void subscribe(ObservableEmitter<Intent> emitter) {
    this.emitter = emitter;

    PendingIntent sentPendingIntent = PendingIntent
        .getBroadcast(context, 0, new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT), 0);
    PendingIntent deliveredPendingIntent = PendingIntent
        .getBroadcast(context, 0, new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED), 0);

    SmsManager smsSender = SmsManager.getDefault();
    smsSender
        .sendTextMessage(DRT_PHONE_NO, null, stopId, sentPendingIntent, deliveredPendingIntent);
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onEvent(Intent intent) {
    if (intent.getAction() == null) {
      return;
    }
    switch (intent.getAction()) {
      case SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT:
      case SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED:
        emitter.onNext(intent);
        break;
      case ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION:
        emitter.onNext(intent);
        emitter.onComplete();
        break;
      case ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION:
        // TODO parse error from intent
        emitter.onError(new Exception());
        break;
    }
  }
}
