package com.tagniam.drtsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import com.tagniam.drtsms.MapFragment.OnStopClickListener;
import android.widget.Toast;
import com.tagniam.drtsms.adapter.ScheduleAdapter;
import com.tagniam.drtsms.adapter.StopCursorAdapter;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.fetcher.RxMockScheduleFetcher;
import com.tagniam.drtsms.schedule.fetcher.RxScheduleFetcher;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher;
import com.tagniam.drtsms.schedule.fetcher.SmsScheduleFetcher;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnStopClickListener {

  private TextView statusLine;
  private SearchView stopIdInput;
  //private ScheduleReceiver scheduleReceiver;
  private RecyclerView scheduleView;
  private BottomSheetBehavior bottomSheetBehavior;
  private MapFragment map;

  // Query listener for searchview
  private SearchView.OnQueryTextListener onQueryTextListener =
      new OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
          // Check that only numbers are in query
          if (android.text.TextUtils.isDigitsOnly(query)) {
            fetchSchedule(query);
            stopIdInput.clearFocus();
            // Update map
            map.clickStop(query);
          }
          return true;
        }

        @Override
        public boolean onQueryTextChange(String query) {
          findMatchingStops(query);
          return true;
        }

        /**
         * Query the database for stop IDs/names containing the given query.
         * @param query query from the search view
         */
        private void findMatchingStops(String query) {
          query = "%" + query.replace(" ", "%") + "%";

          // Query the database in a new thread
          Single.just(query)
              .map(new Function<String, Cursor>() {
                @Override
                public Cursor apply(String s) {
                  return GtfsRoomDatabase.getDatabase(getApplicationContext())
                      .stopDao().findStopsByNameOrId(s);
                }
              })
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(new DisposableSingleObserver<Cursor>() {

                @Override
                public void onSuccess(Cursor cursor) {
                  stopIdInput.setSuggestionsAdapter(new StopCursorAdapter(MainActivity.this,
                      cursor, stopIdInput));
                }

                @Override
                public void onError(Throwable e) {
                  updateStatusLine(ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION);
                }
              });
        }
      };

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    statusLine = findViewById(R.id.statusLine);

    stopIdInput = findViewById(R.id.stopIdInput);
    stopIdInput.setOnQueryTextListener(onQueryTextListener);

    // Remove underline from search view
    int searchPlateId = stopIdInput.getContext().getResources()
        .getIdentifier("android:id/search_plate", null, null);
    findViewById(searchPlateId).setBackground(null);
    int searchButtonId = stopIdInput.getContext().getResources()
        .getIdentifier("android:id/search_close_btn", null, null);
    findViewById(searchButtonId).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        map.clearClick();
        stopIdInput.setQuery("", false);
        stopIdInput.requestFocus();
      }
    });

    bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    map = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

    setupScheduleView();
    //listenForScheduleFetches();
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
  /*
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
  */

  /**
   * Fetches the schedule.
   */
  public void fetchSchedule(String stopId) {
    //ScheduleFetcher scheduleFetcher = ScheduleFetcher.getInstance(getApplicationContext());
    //scheduleFetcher.fetch(stopId);

    Observable.create(new RxMockScheduleFetcher())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DisposableObserver<Intent>() {
          @Override
          public void onNext(Intent intent) {
            // Unpack intent
            if (intent.getAction() == null) {
              return;
            }
            updateStatusLine(intent.getAction());
            Schedule schedule = RxScheduleFetcher.Intents.getScheduleFromIntent(intent);
            if (schedule != null) {
              populateScheduleView(schedule);
            }
          }

          @Override
          public void onError(Throwable e) {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
          }

          @Override
          public void onComplete() {
            Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
          }
        });
  }

  /**
   * Updates the schedule view with the fetched schedule.
   *
   * @param schedule fetched schedule
   */
  private void populateScheduleView(Schedule schedule) {
    // Get bus time objects
    ScheduleAdapter scheduleAdapter = new ScheduleAdapter(getApplicationContext(),
        schedule.getBusTimes(), new Date());
    scheduleView.setAdapter(scheduleAdapter);
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
  }

  /**
   * Updates the status line based on the action we get from the schedule fetcher.
   */
  private void updateStatusLine(String action) {
    switch (action) {
      case ScheduleFetcher.SCHEDULE_FETCH_FAIL_ACTION:
        statusLine.setText(getString(R.string.progress_schedule_fetch_fail));
        statusLine.setBackgroundColor(getResources().getColor(R.color.colorError));
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
        statusLine.setVisibility(View.GONE);
        break;
      default:
        break;
    }
  }

  /**
   * Sets the query once a stop has been
   *
   * @param stopCode stop's identifier, which will be set as the query
   */
  @Override
  public void onStopClick(String stopCode) {
    stopIdInput.setQuery(stopCode, false);
  }

  /*
  private class ScheduleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action == null) {
        return;
      }

      updateStatusLine(action);
      // We got the message from DRT! Yay!
      if (action.equals(ScheduleFetcher.SCHEDULE_FETCH_SUCCESS_ACTION)) {
        Schedule schedule =
            (Schedule) intent.getSerializableExtra(ScheduleFetcher.SCHEDULE_FETCH_RESULT);
        populateScheduleView(schedule);
      }
    }
  }
  */
}
