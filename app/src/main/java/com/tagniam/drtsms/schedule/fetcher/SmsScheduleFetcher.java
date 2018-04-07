package com.tagniam.drtsms.schedule.fetcher;

import android.content.Context;
import android.content.Intent;
import com.tagniam.drtsms.schedule.fetcher.RxScheduleFetcher.Intents;

/**
 * Created by jr on 01/12/17.
 */
public class SmsScheduleFetcher extends ScheduleFetcher {

  SmsScheduleFetcher(Context context) {
    super(context);
  }

  @Override
  public void fetch(String stopId) {
    // Create intent
    Intent intent = new Intent();
    intent.setClass(getApplicationContext(), SmsScheduleService.class);
    intent.putExtra(Intents.STOP_ID_EXTRA, stopId);
    getApplicationContext().startService(intent);
  }
}
