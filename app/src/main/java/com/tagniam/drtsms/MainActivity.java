package com.tagniam.drtsms;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;
import com.tagniam.drtsms.MapFragment.OnStopClickListener;
import com.tagniam.drtsms.adapter.ScheduleAdapter;
import com.tagniam.drtsms.adapter.StopCursorAdapter;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher.Intents;
import com.tagniam.drtsms.schedule.fetcher.SmsScheduleFetcher;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnStopClickListener {

  private TextView statusLine;
  private SearchView stopIdInput;
  private RecyclerView scheduleView;
  private BottomSheetBehavior bottomSheetBehavior;
  private MapFragment map;
  private ScheduleFetcher scheduleFetcher;
  private Disposable scheduleFetcherDisposable;

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
         *
         * @param query query from the search view
         */
        private void findMatchingStops(String query) {
          query = "%" + query.replace(" ", "%") + "%";

          // Query the database in a new thread
          Single.just(query)
              .map(
                  new Function<String, Cursor>() {
                    @Override
                    public Cursor apply(String s) {
                      return GtfsRoomDatabase.getDatabase(getApplicationContext())
                          .stopDao()
                          .findStopsByNameOrId(s);
                    }
                  })
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(
                  new DisposableSingleObserver<Cursor>() {

                    @Override
                    public void onSuccess(Cursor cursor) {
                      stopIdInput.setSuggestionsAdapter(
                          new StopCursorAdapter(MainActivity.this, cursor, stopIdInput));
                    }

                    @Override
                    public void onError(Throwable e) {
                      updateStatusLine(Intents.FAIL_ACTION);
                    }
                  });
        }
      };

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // Setup status line
    statusLine = findViewById(R.id.statusLine);

    // Setup search view
    stopIdInput = findViewById(R.id.stopIdInput);
    stopIdInput.setOnQueryTextListener(onQueryTextListener);
    // Remove underline from search view
    int searchPlateId =
        stopIdInput
            .getContext()
            .getResources()
            .getIdentifier("android:id/search_plate", null, null);
    findViewById(searchPlateId).setBackground(null);
    final int searchButtonId =
        stopIdInput
            .getContext()
            .getResources()
            .getIdentifier("android:id/search_close_btn", null, null);
    findViewById(searchButtonId)
        .setOnClickListener(
            new OnClickListener() {
              @Override
              public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                map.clearClick();
                stopIdInput.setQuery("", false);
                stopIdInput.requestFocus();
              }
            });

    // Setup bottom sheet
    bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
          // Clear click on map and search when bottom sheet gets hidden
          map.clearClick();
          stopIdInput.setQuery("", false);
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {

      }
    });

    // Setup map
    map = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);

    // Setup schedule view
    scheduleView = findViewById(R.id.scheduleDisplay);
    scheduleView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (scheduleFetcherDisposable != null && !scheduleFetcherDisposable.isDisposed()) {
      scheduleFetcherDisposable.dispose();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (scheduleFetcher != null) {
      scheduleFetcher.onPause();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (scheduleFetcher != null) {
      scheduleFetcher.onResume();
    }
  }

  /**
   * Fetches the schedule.
   */
  public void fetchSchedule(final String stopId) {
    scheduleFetcher = ScheduleFetcher.getFetcher(getApplicationContext(), stopId);
    scheduleFetcherDisposable =
        Observable.create(scheduleFetcher)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(
                new DisposableObserver<Intent>() {
                  @Override
                  public void onNext(Intent intent) {
                    // Unpack intent
                    if (intent.getAction() == null) {
                      return;
                    }
                    updateStatusLine(intent.getAction());
                    Schedule schedule = ScheduleFetcher.Intents.getScheduleFromIntent(intent);
                    if (schedule != null) {
                      displaySchedule(schedule);
                    }
                  }

                  @Override
                  public void onError(Throwable e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG)
                        .show();
                  }

                  @Override
                  public void onComplete() {
                  }
                });
  }

  /**
   * Display the schedule's information.
   *
   * @param schedule schedule object with bus times + routes
   */
  private void displaySchedule(final Schedule schedule) {
    // Display bottom sheet schedule
    ScheduleAdapter scheduleAdapter =
        new ScheduleAdapter(
            getApplicationContext(), schedule.getBusTimes(), new Date());
    scheduleView.setAdapter(scheduleAdapter);
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    // Set bottom sheet title
    Single.just(schedule.getStopNumber())
        .map(new Function<String, String>() {
          @Override
          public String apply(String stopCode) {
            // Get stop name
            return GtfsRoomDatabase.getDatabase(getApplicationContext())
                .stopDao().getStopName(stopCode);
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DisposableSingleObserver<String>() {
          @Override
          public void onSuccess(String name) {
            if (name == null) {
              // Display stop code in case it's not found in the db
              stopIdInput
                  .setQuery(getString(R.string.search_stop_id, schedule.getStopNumber()), false);
            }
            // Display stop name
            stopIdInput.setQuery(name, false);
          }

          @Override
          public void onError(Throwable e) {
            // Display stop code in case it's not found in the db
            stopIdInput
                .setQuery(getString(R.string.search_stop_id, schedule.getStopNumber()), false);
          }
        });

  }

  /** Updates the status line based on the action we get from the schedule fetcher. */
  private void updateStatusLine(String action) {
    statusLine.setVisibility(View.VISIBLE);
    switch (action) {
      case Intents.FAIL_ACTION:
        statusLine.setText(getString(R.string.progress_schedule_fetch_fail));
        statusLine.setBackgroundColor(getResources().getColor(R.color.colorError));
        break;
      case SmsScheduleFetcher.Intents.SMS_SENT_ACTION:
        statusLine.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        statusLine.setText(getString(R.string.progress_schedule_fetch_sms_sent));
        break;
      case SmsScheduleFetcher.Intents.SMS_DELIVERED_ACTION:
        statusLine.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        statusLine.setText(getString(R.string.progress_schedule_fetch_sms_delivered));
        break;
      case Intents.SUCCESS_ACTION:
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
}
