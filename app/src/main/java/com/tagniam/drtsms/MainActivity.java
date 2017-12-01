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

import com.tagniam.drtsms.schedule.ScheduleService;

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

        // Start service to send msg
        Intent intent = new Intent();
        intent.setClass(this, ScheduleService.class);
        intent.putExtra("stop_id", stopId);
        startService(intent);

        // Wait for result
        registerScheduleReceiver();
    }

    private void registerScheduleReceiver() {
        scheduleReceiver = new ScheduleReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScheduleService.SCHEDULE_RECEIVED);

        registerReceiver(scheduleReceiver, intentFilter);
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
