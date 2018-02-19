package com.tagniam.drtsms.schedule.fetcher;

import android.content.Context;

/** Created by jr on 01/12/17. */
public abstract class ScheduleFetcher {
  // Action string for completion of schedule fetching
  public static final String SCHEDULE_FETCH_SUCCESS_ACTION =
      "com.tagniam.drtsms.schedule.SCHEDULE_FETCH_SUCCESS_ACTION";
  // Action string for cancellation of schedule fetching
  public static final String SCHEDULE_FETCH_FAIL_ACTION = "com.tagniam.drtsms.schedule.SCHEDULE_FETCH_FAIL_ACTION";
  // Extra string for schedule data output
  public static final String SCHEDULE_FETCH_RESULT = "com.tagniam.drtsms.schedule.SCHEDULE_FETCH_RESULT";
  // Input string for stop id input
  static final String SCHEDULE_FETCH_STOP_ID = "com.tagniam.drtsms.schedule.SCHEDULE_FETCH_STOP_ID";
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
    }

    else {
      return new SmsScheduleFetcher(context);
    }
  }

  /**
   * Fetches the schedule of the stopId. A broadcast is sent with action
   * SCHEDULE_FETCH_SUCCESS_ACTION, for asynchronous receipt.
   *
   * @param stopId id of the DRT stop to be queried for schedule
   */
  public abstract void fetch(String stopId);

  final Context getApplicationContext() {
    return context;
  }
}
