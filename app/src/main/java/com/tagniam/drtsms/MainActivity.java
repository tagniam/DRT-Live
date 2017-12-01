package com.tagniam.drtsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tagniam.drtsms.schedule.ScheduleFetcher;
import com.tagniam.drtsms.schedule.SmsScheduleFetcher;

public class MainActivity extends AppCompatActivity {
    private EditText stopIdInput;
    private TextView scheduleDisplay;
    private ScheduleReceiver scheduleReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stopIdInput = findViewById(R.id.stopIdInput);
        scheduleDisplay = findViewById(R.id.scheduleDisplay);
    }

    protected void fetchSchedule(View view) {
        // Grab stop id
        String stopId = stopIdInput.getText().toString();
        ScheduleFetcher scheduleFetcher = new SmsScheduleFetcher(getApplicationContext());
        scheduleFetcher.fetch(stopId);

        // Wait to receive the schedule
        scheduleReceiver = new ScheduleReceiver();
        registerReceiver(scheduleReceiver,
                new IntentFilter(ScheduleFetcher.SCHEDULE_FETCH_COMPLETED_ACTION));
    }

    private class ScheduleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result");
            scheduleDisplay.setText(result);
            unregisterReceiver(scheduleReceiver);
        }
    }

}
