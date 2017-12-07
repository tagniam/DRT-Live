package com.tagniam.drtsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.tagniam.drtsms.schedule.adapter.ScheduleAdapter;
import com.tagniam.drtsms.schedule.data.BusTime;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.data.SmsBusTime;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher;
import com.tagniam.drtsms.schedule.fetcher.SmsScheduleFetcher;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private ScheduleReceiver scheduleReceiver;
  private final String TEST_MSG_1 = "Rt 900 WB: 7:09p| 12:25a| 5:59a";
  private final String TEST_MSG_2 = "Rt 916 Counter Clockwise: 7:16p| 7:44p| 8:12p| 8:42p";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      // Schedule view setup
      RecyclerView scheduleView = findViewById(R.id.scheduleDisplay);
      LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
      scheduleView.setLayoutManager(layoutManager);

      // Mock bus time objects for now
      List<BusTime> busTimeList = new ArrayList<>();
      busTimeList.add(new SmsBusTime(TEST_MSG_1));
      busTimeList.add(new SmsBusTime(TEST_MSG_2));

      // Sets up adapter, contents of schedule view
      ScheduleAdapter scheduleAdapter = new ScheduleAdapter(busTimeList);
      scheduleView.setAdapter(scheduleAdapter);

      listenForScheduleFetches();
  }

    /**
     * Start listening to incoming schedule fetches.
     */
    private void listenForScheduleFetches() {
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
