package com.tagniam.drtsms.schedule;

import android.content.Context;
import android.content.Intent;

/**
 * Created by jr on 01/12/17.
 */

public class SmsScheduleFetcher implements ScheduleFetcher {
    private Context context;

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
