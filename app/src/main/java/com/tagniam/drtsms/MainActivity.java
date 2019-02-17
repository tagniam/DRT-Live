package com.tagniam.drtsms;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements MapFragment.OnStopClickListener {

  private BottomNavigationView navigation;
  private ScheduleFetcher scheduleFetcher;
  private Disposable scheduleFetcherDisposable;

  private void openPrestoApp() {
    Intent intent;
    PackageManager manager = getPackageManager();
    String prestoPackageName = getString(R.string.presto_app_package_name);
    try {
      intent = manager.getLaunchIntentForPackage(prestoPackageName);
      if (intent == null) throw new PackageManager.NameNotFoundException();

      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      startActivity(intent);
    } catch (PackageManager.NameNotFoundException exception) {
      intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse("market://details?id=" + prestoPackageName));
      startActivity(intent);
    }
  }

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().hide();
    setContentView(R.layout.activity_main);

    navigation = findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
          case R.id.action_presto:
            openPrestoApp();
            break;
        }

        return false;
      }
    });
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
                                //updateStatusLine(intent.getAction());
                                Schedule schedule = ScheduleFetcher.Intents.getScheduleFromIntent(intent);
                                if (schedule != null) {
                                  //displaySchedule(schedule);
                                  Toast.makeText(getApplicationContext(), schedule.getBusTimes().get(0).getRoute(), Toast.LENGTH_SHORT).show();
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

  @Override
  public void onStopClick(String stopCode) {
    fetchSchedule(stopCode);
  }
}
