package com.tagniam.drtsms.schedule.fetcher;

import android.content.Context;

/**
 * Created by jr on 01/12/17.
 */
public abstract class ScheduleFetcher {

  private static final boolean DEBUG = false;
  private Context context;

  ScheduleFetcher(Context context) {
    this.context = context;
  }

  /**
   * Returns an instance of a ScheduleFetcher. If debug mode is on, a mock fetcher is returned.
   * Otherwise the normal SMS fetcher is returned.
   *
   * @param context application context
   * @return schedule fetcher
   */
  public static ScheduleFetcher getInstance(Context context) {
    if (DEBUG) {
      return new MockScheduleFetcher(context);
    } else {
      return new SmsScheduleFetcher(context);
    }
  }

  /**
   * Fetches the schedule of the stopId. A broadcast is sent with action
   * SUCCESS_ACTION, for asynchronous receipt.
   *
   * @param stopId id of the DRT stop to be queried for schedule
   */
  public abstract void fetch(String stopId);

  final Context getApplicationContext() {
    return context;
  }
}
