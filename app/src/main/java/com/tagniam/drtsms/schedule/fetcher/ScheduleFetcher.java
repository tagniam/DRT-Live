package com.tagniam.drtsms.schedule.fetcher;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import com.tagniam.drtsms.schedule.data.Schedule;
import io.reactivex.ObservableOnSubscribe;

public abstract class ScheduleFetcher implements ObservableOnSubscribe<Intent> {

  public static ScheduleFetcher getFetcher(Context context, String stopId) {
    if (DEBUG) {
      return new MockScheduleFetcher(stopId);
    } else {
      PendingIntent sentPendingIntent = PendingIntent
          .getBroadcast(context, 0,
              new Intent(SmsScheduleFetcher.Intents.SMS_SENT_ACTION), 0);
      PendingIntent deliveredPendingIntent = PendingIntent
          .getBroadcast(context, 0,
              new Intent(SmsScheduleFetcher.Intents.SMS_DELIVERED_ACTION), 0);
      return new SmsScheduleFetcher(SmsManager.getDefault(), sentPendingIntent,
          deliveredPendingIntent, stopId);
    }
  }

  /**
   * Lifecycle hooks to manage the fetcher once the app is paused/resumed.
   */
  public abstract void onPause();

  public abstract void onResume();

  private static final boolean DEBUG = true;

  public static class Intents {

    // Action string for completion of schedule fetching
    public static final String SUCCESS_ACTION =
        "com.tagniam.drtsms.schedule.SUCCESS_ACTION";
    // Action string for cancellation of schedule fetching
    public static final String FAIL_ACTION = "com.tagniam.drtsms.schedule.FAIL_ACTION";
    // Extra string for schedule data output
    public static final String RESULT_EXTRA = "com.tagniam.drtsms.schedule.RESULT_EXTRA";
    // Extra string for exceptions
    public static final String EXCEPTION_EXTRA = "com.tagniam.drtsms.schedule.EXCEPTION_EXTRA";

    /**
     * Gets a schedule object from an intent if it has been passed in.
     *
     * @param intent intent with an action string in this class
     * @return null if no schedule is sent yet, or a Schedule object if it has been sent
     */
    public static Schedule getScheduleFromIntent(Intent intent) {
      if (intent.getAction() == null || !intent.getAction().equals(SUCCESS_ACTION)) {
        return null;
      }
      return (Schedule) intent.getSerializableExtra(RESULT_EXTRA);
    }

  }
}
