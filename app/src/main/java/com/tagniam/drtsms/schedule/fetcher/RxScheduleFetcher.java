package com.tagniam.drtsms.schedule.fetcher;

import android.content.Intent;
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
}
