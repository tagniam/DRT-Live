package com.tagniam.drtsms.schedule;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jr on 01/12/17.
 */

public class MockScheduleFetcher implements ScheduleFetcher {
    private Context context;
    private final String MOCK_MSG = "Stop MOCK1604:\r\n" +
        "Rt 900 WB: 7:09p| 7:25p| 7:40p| 7:54p| 8:09p| 8:29p| 8:49p\r\n" +
        "Rt 916 WB: 7:16p| 7:44p| 8:12p| 8:42p\r\n" +
        "std msg rates may apply";
    private int step;
    private Timer timer = new Timer();


    public MockScheduleFetcher(Context context) {
        this.context = context;
    }

    /**
     * Emulate the sms sending process through timed broadcasts
     * @param stopId id of the DRT stop to be queried for schedule
     */
    @Override
    public void fetch(String stopId) {
        step = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Emulate sending sms
                if (step == 0) {
                    context.sendBroadcast(new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT));
                }

                // Emulate delivered sms
                else if (step == 1) {
                    context.sendBroadcast(new Intent(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED));
                }

                // Emulate success sms, send result back and end timer
                else if (step == 4) {
                    // Send a success right away with string
                    Intent resultIntent = new Intent();
                    resultIntent.setAction(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
                    resultIntent.putExtra("result", MOCK_MSG);
                    context.sendBroadcast(resultIntent);
                    timer.cancel();
                }

                step++;
            }
        }, 1000, 1000);
    }
}
