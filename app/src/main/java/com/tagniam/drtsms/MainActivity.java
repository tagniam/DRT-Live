package com.tagniam.drtsms;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

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
    //getSupportActionBar().hide();
    getSupportActionBar().setTitle("Map");
    setContentView(R.layout.activity_main);

    BottomNavigationView navigation = findViewById(R.id.navigation);
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
}
