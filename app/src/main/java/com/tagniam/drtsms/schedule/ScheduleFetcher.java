package com.tagniam.drtsms.schedule;

import android.content.BroadcastReceiver;

/**
 * Created by jr on 01/12/17.
 */

public interface ScheduleFetcher {
    // Action string for completion of schedule fetching
    String SCHEDULE_FETCH_COMPLETED_ACTION = "com.tagniam.drtsms.schedule.SCHEDULE_FETCH_COMPLETED_ACTION";
    // Action string for cancellation of schedule fetching
    String SCHEDULE_FETCH_CANCELLED_ACTION = "com.tagniam.drtsms.schedule.SCHEDULE_FETCH_CANCELLED_ACTION";

    /**
     * Fetches the schedule of the stopId. A broadcast is sent with action SCHEDULE_FETCH_COMPLETED_ACTION,
     * for asynchronous receipt.
     *
     * @param stopId id of the DRT stop to be queried for schedule
     */
    void fetch(String stopId);
}
