package com.tagniam.drtlive.schedule.fetcher;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.SmsManager;
import com.tagniam.drtlive.schedule.data.Schedule;

import io.reactivex.ObservableOnSubscribe;

public abstract class ScheduleFetcher implements ObservableOnSubscribe<Intent> {

  private static Mode mode = Mode.WEB;

  public static ScheduleFetcher getFetcher(Context context, String stopId) {
    if (haveNetworkConnection(context))
      mode = Mode.WEB;
    else
      mode = Mode.SMS;

    switch (mode) {
      case DEBUG:
        return new MockScheduleFetcher(stopId);
      case SMS:
        PendingIntent sentPendingIntent = PendingIntent
            .getBroadcast(context, 0,
                new Intent(SmsScheduleFetcher.Intents.SMS_SENT_ACTION), 0);
        PendingIntent deliveredPendingIntent = PendingIntent
            .getBroadcast(context, 0,
                new Intent(SmsScheduleFetcher.Intents.SMS_DELIVERED_ACTION), 0);
        return new SmsScheduleFetcher(SmsManager.getDefault(), sentPendingIntent,
            deliveredPendingIntent, stopId);
      case WEB:
        return new ApiScheduleFetcher(stopId);
    }
    return null;
  }

  /**
   * Detects if the device currently has an internet connection (wifi or mobile).
   * @param context needed to get system service
   * @return true if has network connection
   */
  private static boolean haveNetworkConnection(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    if (netInfo == null) return false;
    return netInfo.getType() == ConnectivityManager.TYPE_WIFI ||
            netInfo.getType() == ConnectivityManager.TYPE_MOBILE;
  }


  /**
   * Lifecycle hooks to manage the fetcher once the app is paused/resumed.
   */
  public abstract void onPause();

  public abstract void onResume();

  // Modes for different types of schedules
  public enum Mode {
    DEBUG,
    SMS,
    WEB
  }
  public static class Intents {

    // Action string for completion of schedule fetching
    public static final String SUCCESS_ACTION =
        "com.tagniam.drtlive.schedule.SUCCESS_ACTION";
    // Action string for cancellation of schedule fetching
    public static final String FAIL_ACTION = "com.tagniam.drtlive.schedule.FAIL_ACTION";
    // Extra string for schedule data output
    public static final String RESULT_EXTRA = "com.tagniam.drtlive.schedule.RESULT_EXTRA";
    // Extra string for exceptions
    public static final String EXCEPTION_EXTRA = "com.tagniam.drtlive.schedule.EXCEPTION_EXTRA";

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
