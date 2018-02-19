package com.tagniam.drtsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.tagniam.drtsms.adapter.ScheduleAdapter;
import com.tagniam.drtsms.database.stops.Stop;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher;
import com.tagniam.drtsms.schedule.fetcher.SmsScheduleFetcher;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

  private TextView statusLine;
  private EditText stopIdInput;
  private ScheduleReceiver scheduleReceiver;
  private RecyclerView scheduleView;
  private ImageView displayMap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Display route map
    displayMap = findViewById(R.id.displayMap);
    displayMap.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(view.getContext(), MapActivity.class);
        startActivity(intent);
      }
    });

    statusLine = findViewById(R.id.statusLine);
    stopIdInput = findViewById(R.id.stopIdInput);
    stopIdInput.setOnKeyListener(new OnKeyListener() {
      @Override
      public boolean onKey(View view, int i, KeyEvent keyEvent) {
        // Get schedule on enter
        if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) &&
            (i == KeyEvent.KEYCODE_ENTER)) {
          // Hide soft keyboard
          InputMethodManager imm = (InputMethodManager) getSystemService(
              Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
          // Fetch schedule
          fetchSchedule();
          return true;
        }
        return false;
      }
    });

    // If we came from the map activity, set the stop number automatically and fetch
    Intent intent = getIntent();
    Stop stop = (Stop) intent.getSerializableExtra(Stop.EXTRA_STOP);
    if (stop != null) {
      stopIdInput.setText(stop.stopCode);
      fetchSchedule();
    }

    setupScheduleView();
    listenForScheduleFetches();
  }

  /**
   * Sets up the recycler view where the schedule will be displayed.
   */
  private void setupScheduleView() {
    // Schedule view setup
    scheduleView = findViewById(R.id.scheduleDisplay);
    LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
    scheduleView.setLayoutManager(layoutManager);
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

  /**
   * Fetches the schedule.
   */
  public void fetchSchedule() {
    // Grab stop id
    String stopId = stopIdInput.getText().toString();
    ScheduleFetcher scheduleFetcher = ScheduleFetcher.getInstance(getApplicationContext());
    scheduleFetcher.fetch(stopId);
  }

  /**
   * Updates the schedule view with the fetched schedule.
   *
   * @param schedule fetched schedule
   */
  private void populateScheduleView(Schedule schedule) {
    Date now = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.CANADA);
    // Update message
    statusLine.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    statusLine.setText(getString(R.string.progress_schedule_fetch_last_updated,
        dateFormat.format(now)));

    // Get bus time objects
    ScheduleAdapter scheduleAdapter = new ScheduleAdapter(getApplicationContext(),
        schedule.getBusTimes(), now);
    scheduleView.setAdapter(scheduleAdapter);
  }

  private class ScheduleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      switch (intent.getAction()) {
        case ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION:
          statusLine.setText(getString(R.string.progress_schedule_fetch_fail));
          unregisterReceiver(scheduleReceiver);
          break;
        case SmsScheduleFetcher.SCHEDULE_FETCH_SMS_SENT:
          statusLine.setVisibility(View.VISIBLE);
          statusLine.setBackgroundColor(getResources().getColor(R.color.colorAccent));
          statusLine.setText(getString(R.string.progress_schedule_fetch_sms_sent));
          break;
        case SmsScheduleFetcher.SCHEDULE_FETCH_SMS_DELIVERED:
          statusLine.setBackgroundColor(getResources().getColor(R.color.colorAccent));
          statusLine.setText(getString(R.string.progress_schedule_fetch_sms_delivered));
          break;
        case ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION:
          Schedule schedule =
              (Schedule) intent.getSerializableExtra(ScheduleFetcher.SCHEDULE_FETCH_RESULT);
          populateScheduleView(schedule);
          break;
        default:
          break;
      }
    }
  }
}
