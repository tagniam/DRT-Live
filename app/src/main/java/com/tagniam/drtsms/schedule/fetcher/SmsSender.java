package com.tagniam.drtsms.schedule.fetcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.greenrobot.eventbus.EventBus;

public class SmsSender extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    switch (getResultCode()) {
      case Activity.RESULT_OK:
        // SMS received!
        EventBus.getDefault().postSticky(intent);
        break;
      default:
        // SMS failed, post fail
        EventBus.getDefault().postSticky(new Intent(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION));
        break;
    }
  }
}
