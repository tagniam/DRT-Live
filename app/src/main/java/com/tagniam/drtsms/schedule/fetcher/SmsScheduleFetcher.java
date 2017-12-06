package com.tagniam.drtsms.schedule.fetcher;

import android.content.Context;
import android.content.Intent;

/**
 * Created by jr on 01/12/17.
 */

public class SmsScheduleFetcher implements ScheduleFetcher {
    private Context context;

    // More progress action strings
    public static final String SCHEDULE_FETCH_SMS_SENT = "com.tagniam.drtsms.schedule.SCHEDULE_FETCH_SMS_SENT";
    // Action string for cancellation of schedule fetching
    public static final String SCHEDULE_FETCH_SMS_DELIVERED = "com.tagniam.drtsms.schedule.SCHEDULE_FETCH_SMS_DELIVERED";

    public SmsScheduleFetcher(Context context) {
        this.context = context;
    }

    @Override
    public void fetch(String stopId) {
        // Create intent
        Intent intent = new Intent();
        intent.setClass(context, SmsScheduleService.class);
        intent.putExtra("stop_id", stopId);
        context.startService(intent);
    }
}
