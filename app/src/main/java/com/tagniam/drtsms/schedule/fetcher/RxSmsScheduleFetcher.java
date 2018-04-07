package com.tagniam.drtsms.schedule.fetcher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.data.SmsSchedule;
import com.tagniam.drtsms.schedule.exceptions.StopNotFoundException;
import com.tagniam.drtsms.schedule.exceptions.StopTimesNotAvailableException;
import io.reactivex.ObservableEmitter;
import java.io.Serializable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class RxSmsScheduleFetcher extends RxScheduleFetcher {

  private final static String DRT_PHONE_NO = "8447460497";
  private Context context;
  private String stopId;
  private ObservableEmitter<Intent> emitter;
  private SmsManager smsSender;

  public RxSmsScheduleFetcher(Context context, SmsManager smsSender, String stopId) {
    this.context = context;
    this.smsSender = smsSender;
    this.stopId = stopId;
    EventBus.getDefault().register(this);
  }

  @Override
  public void subscribe(ObservableEmitter<Intent> emitter) {
    this.emitter = emitter;

    // Create pending intents that trigger given actions when SMS is sent/received
    PendingIntent sentPendingIntent = PendingIntent
        .getBroadcast(context, 0, new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT), 0);
    PendingIntent deliveredPendingIntent = PendingIntent
        .getBroadcast(context, 0, new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED), 0);

    // Send the SMS!
    smsSender
        .sendTextMessage(DRT_PHONE_NO, null, stopId, sentPendingIntent, deliveredPendingIntent);
  }

  /**
   * Parses an intent, depending on the action it will communicate with the emitter and complete/
   * error out by emitting an intent.
   * and emit
   *
   * @param intent intent from internal static broadcast receiver classes
   */
  @Subscribe(sticky = true)
  public void onIntent(Intent intent) {
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
        EventBus.getDefault().unregister(this);
        break;
      case ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION:
        emitter.onError((Exception) intent.getSerializableExtra(Intents.EXCEPTION_EXTRA));
        EventBus.getDefault().unregister(this);
        break;
    }
  }

  @Override
  public void onPause() {
    EventBus.getDefault().unregister(this);
  }

  @Override
  public void onResume() {
    EventBus.getDefault().register(this);
  }

  /**
   * Listens for response from DRT.
   */
  public static class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {

          // Only analyze if from DRT
          // Note: apparently all SMS from DRT < 160 char, so don't need to concatenate msgs
          if (smsMessage.getOriginatingAddress().equals(DRT_PHONE_NO)) {
            try {
              Schedule schedule = new SmsSchedule(smsMessage.getMessageBody());
              Intent result = new Intent(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
              result.putExtra(ScheduleFetcher.SCHEDULE_FETCH_RESULT, (Serializable) schedule);
              EventBus.getDefault().postSticky(result);
            } catch (StopNotFoundException | StopTimesNotAvailableException e) {
              EventBus.getDefault()
                  .postSticky(new Intent(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION)
                      .putExtra(Intents.EXCEPTION_EXTRA, e));
            }
            break;
          }
        }
      }
    }
  }

  /**
   * Tracks the sending status of the outgoing SMS message.
   */
  public static class SmsSender extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      switch (getResultCode()) {
        case Activity.RESULT_OK:
          // SMS received!
          EventBus.getDefault().postSticky(intent);
          break;
        default:
          // SMS failed, post fail
          EventBus.getDefault().postSticky(new Intent(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION));
          break;
      }
    }
  }
}
