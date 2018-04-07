package com.tagniam.drtsms.schedule.fetcher;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import com.tagniam.drtsms.schedule.data.Schedule;
import io.reactivex.ObservableOnSubscribe;

public abstract class RxScheduleFetcher implements ObservableOnSubscribe<Intent> {

  public static class Intents {
    /**
     * Gets a schedule object from an intent if it has been passed in.
     * @param intent intent with an action string in this class
     * @return null if no schedule is sent yet, or a Schedule object if it has been sent
     */
    public static Schedule getScheduleFromIntent(Intent intent) {
      if (intent.getAction() == null || !intent.getAction().equals(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION)) {
        return null;
      }
      return (Schedule) intent.getSerializableExtra(ScheduleFetcher.SCHEDULE_FETCH_RESULT);
    }

    public static final String EXCEPTION_EXTRA = "tagniam.exception";
  }

  /**
   * Lifecycle hooks to manage the fetcher once the app is paused/resumed.
   */
  public abstract void onPause();

  public abstract void onResume();

  private static final boolean DEBUG = false;

  public static RxScheduleFetcher getFetcher(Context context, String stopId) {
    if (DEBUG) {
      return new RxMockScheduleFetcher(stopId);
    } else {
      PendingIntent sentPendingIntent = PendingIntent
          .getBroadcast(context, 0, new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT), 0);
      PendingIntent deliveredPendingIntent = PendingIntent
          .getBroadcast(context, 0, new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED), 0);
      return new RxSmsScheduleFetcher(SmsManager.getDefault(), sentPendingIntent,
          deliveredPendingIntent, stopId);
    }
  }
}
