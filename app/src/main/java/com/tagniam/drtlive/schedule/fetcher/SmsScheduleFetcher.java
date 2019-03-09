package com.tagniam.drtlive.schedule.fetcher;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import com.tagniam.drtlive.R;
import com.tagniam.drtlive.schedule.data.Schedule;
import com.tagniam.drtlive.schedule.data.SmsSchedule;
import com.tagniam.drtlive.schedule.exceptions.StopNotFoundException;
import com.tagniam.drtlive.schedule.exceptions.StopTimesNotAvailableException;
import io.reactivex.ObservableEmitter;
import java.io.Serializable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SmsScheduleFetcher extends ScheduleFetcher {

  SmsScheduleFetcher(SmsManager smsSender, PendingIntent sentPendingIntent,
      PendingIntent deliveredPendingIntent, String stopId) {
    this.smsSender = smsSender;
    this.sentPendingIntent = sentPendingIntent;
    this.deliveredPendingIntent = deliveredPendingIntent;
    this.stopId = stopId;
    EventBus.getDefault().register(this);
  }

  private final static String DRT_PHONE_NO = "8447460497";
  private String stopId;
  private ObservableEmitter<Intent> emitter;
  private SmsManager smsSender;
  private PendingIntent sentPendingIntent;
  private PendingIntent deliveredPendingIntent;

  /**
   * Parses an intent, depending on the action it will communicate with the emitter and complete/
   * error out by emitting an intent.
   * and emit
   *
   * @param intent intent from internal static broadcast receiver classes
   */
  @Subscribe(sticky = true)
  public void onIntent(Intent intent) {
    if (intent == null || intent.getAction() == null) {
      return;
    }
    emitter.onNext(intent);
    switch (intent.getAction()) {
      case ScheduleFetcher.Intents.SUCCESS_ACTION:
        emitter.onComplete();
        EventBus.getDefault().unregister(this);
        break;
      case ScheduleFetcher.Intents.FAIL_ACTION:
        emitter.onError(
            (Exception) intent.getSerializableExtra(ScheduleFetcher.Intents.EXCEPTION_EXTRA));
        EventBus.getDefault().unregister(this);
        break;
    }
  }

  @Override
  public void subscribe(ObservableEmitter<Intent> emitter) {
    this.emitter = emitter;
    // Send the SMS!
    smsSender
        .sendTextMessage(DRT_PHONE_NO, null, stopId, sentPendingIntent, deliveredPendingIntent);
  }

  public static class Intents {

    public static final String SMS_SENT_ACTION = "com.tagniam.drtlive.schedule.SMS_SENT_ACTION";
    public static final String SMS_DELIVERED_ACTION = "com.tagniam.drtlive.schedule.SMS_DELIVERED_ACTION";
  }

  @Override
  public void onPause() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
  }

  @Override
  public void onResume() {
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
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
              Intent result = new Intent(ScheduleFetcher.Intents.SUCCESS_ACTION);
              result.putExtra(ScheduleFetcher.Intents.RESULT_EXTRA, (Serializable) schedule);
              EventBus.getDefault().postSticky(result);
            } catch (StopNotFoundException | StopTimesNotAvailableException e) {
              EventBus.getDefault()
                  .postSticky(new Intent(ScheduleFetcher.Intents.FAIL_ACTION)
                      .putExtra(ScheduleFetcher.Intents.EXCEPTION_EXTRA, e));
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
      int resultCode = getResultCode();

      // Something went wrong with sending, post error intent
      if (resultCode != Activity.RESULT_OK) {
        Intent error = new Intent(ScheduleFetcher.Intents.FAIL_ACTION);
        switch (resultCode) {
          case SmsManager.RESULT_ERROR_RADIO_OFF:
            error.putExtra(
                ScheduleFetcher.Intents.EXCEPTION_EXTRA,
                new Exception(context.getString(R.string.error_radio_off)));
            break;
          case SmsManager.RESULT_ERROR_NO_SERVICE:
            error.putExtra(ScheduleFetcher.Intents.EXCEPTION_EXTRA,
                new Exception(context.getString(R.string.error_no_service)));
            break;
          default:
            error.putExtra(ScheduleFetcher.Intents.EXCEPTION_EXTRA,
                new Exception(context.getString(R.string.error_generic)));
            break;
        }
        EventBus.getDefault().postSticky(error);
        return;
      }

      // SMS sent/delivered!
      EventBus.getDefault().postSticky(intent);
    }
  }
}
