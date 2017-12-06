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
import android.widget.Toast;

import com.tagniam.drtsms.schedule.MockScheduleFetcher;
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

        // UI links
        stopIdInput = findViewById(R.id.stopIdInput);
        scheduleDisplay = findViewById(R.id.scheduleDisplay);

        // Start listening for incoming schedule fetches
        scheduleReceiver = new ScheduleReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION);
        intentFilter.addAction(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION);
        intentFilter.addAction(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT);
        intentFilter.addAction(SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED);

        registerReceiver(scheduleReceiver, intentFilter);
    }

    protected void fetchSchedule(View view) {
        // Grab stop id
        String stopId = stopIdInput.getText().toString();
        ScheduleFetcher scheduleFetcher = new MockScheduleFetcher(getApplicationContext());
        scheduleFetcher.fetch(stopId);
    }

    private class ScheduleReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION:
                    Toast.makeText(getApplicationContext(), "Didn't work", Toast.LENGTH_SHORT).show();
                    unregisterReceiver(scheduleReceiver);
                    break;
                case SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT:
                    Toast.makeText(getApplicationContext(), "Sms sent", Toast.LENGTH_SHORT).show();
                    break;
                case SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED:
                    Toast.makeText(getApplicationContext(), "Sms received", Toast.LENGTH_SHORT).show();
                    break;
                case ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION:
                    String result = intent.getStringExtra("result");
                    scheduleDisplay.setText(result);
                    unregisterReceiver(scheduleReceiver);
                    break;
                default:
                    break;
            }

        }
    }

}
