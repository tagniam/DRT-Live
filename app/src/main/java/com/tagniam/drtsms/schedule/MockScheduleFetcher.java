package com.tagniam.drtsms.schedule;

import android.content.Context;
import android.content.Intent;

/**
 * Created by jr on 01/12/17.
 */

public class MockScheduleFetcher implements ScheduleFetcher {
    private Context context;
    private final String MOCK_MSG = "Stop 1604:\r\n" +
        "Rt 900 WB: 7:09p| 7:25p| 7:40p| 7:54p| 8:09p| 8:29p| 8:49p\r\n" +
        "Rt 916 WB: 7:16p| 7:44p| 8:12p| 8:42p\r\n" +
        "std msg rates may apply";


    public MockScheduleFetcher(Context context) {
        this.context = context;
    }

    /**
     * Broadcasts the message right away
     * @param stopId id of the DRT stop to be queried for schedule
     */
    @Override
    public void fetch(String stopId) {
        // Send a success right away with string
        Intent resultIntent = new Intent();
        resultIntent.setAction(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
        resultIntent.putExtra("result", MOCK_MSG);
        context.sendBroadcast(resultIntent);
    }
}
