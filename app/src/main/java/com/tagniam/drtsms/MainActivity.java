package com.tagniam.drtsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher;
import com.tagniam.drtsms.schedule.fetcher.SmsScheduleFetcher;

public class MainActivity extends AppCompatActivity {
  private ScheduleReceiver scheduleReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Start listening for incoming schedule fetches
    scheduleReceiver = new ScheduleReceiver();
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
    intentFilter.addAction(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION);
    intentFilter.addAction(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT);
    intentFilter.addAction(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED);

    registerReceiver(scheduleReceiver, intentFilter);
  }

  /*
  protected void fetchSchedule(View view) {
    // Grab stop id
    String stopId = stopIdInput.getText().toString();
    ScheduleFetcher scheduleFetcher = new SmsScheduleFetcher(getApplicationContext());
    scheduleFetcher.fetch(stopId);
  }*/

  private class ScheduleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION:
          Toast.makeText(getApplicationContext(), getString(R.string.progress_schedule_fetch_fail),
                  Toast.LENGTH_SHORT).show();
          unregisterReceiver(scheduleReceiver);
          break;
        case SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT:
          Toast.makeText(getApplicationContext(), getString(R.string.progress_schedule_fetch_sms_sent),
                  Toast.LENGTH_SHORT).show();
          break;
        case SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED:
          Toast.makeText(getApplicationContext(), getString(R.string.progress_schedule_fetch_sms_delivered),
                  Toast.LENGTH_SHORT).show();
          break;
        case ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION:
          Schedule schedule =
              (Schedule) intent.getSerializableExtra(ScheduleFetcher.SCHEDULE_FETCH_RESULT);
          Toast.makeText(getApplicationContext(), schedule.toString(),
                  Toast.LENGTH_SHORT).show();
          unregisterReceiver(scheduleReceiver);
          break;
        default:
          break;
      }
    }
  }
}
