package com.tagniam.drtsms;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import com.tagniam.drtsms.database.stops.Stop;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

public class MapActivity extends AppCompatActivity {

  private static final GeoPoint CENTER = new GeoPoint(43.90546, -78.9563);
  private static final double ZOOM = 10.f;
  private static final double ZOOM_2 = 18.f;
  private MapView map;
  private Button chooseStop;
  private DisposableSingleObserver<List<IGeoPoint>> loadStopsObserver;
  private List<Stop> stops;
  private int selectedStopIdx = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    // Setup map
    map = findViewById(R.id.map);
    map.setTileSource(TileSourceFactory.MAPNIK);
    map.setBuiltInZoomControls(true);
    map.setMultiTouchControls(true);

    // Choose stop button
    chooseStop = findViewById(R.id.chooseStop);
    chooseStop.setVisibility(View.GONE);
    chooseStop.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        chooseStop();
      }
    });

    setupMapPoints();
    setupMyLocationOverlay();
  }

  /**
   * Display the stop points on the map.
   */
  private void setupMapPoints() {
    // Load stop points from database
    Single<List<IGeoPoint>> loadStops = Single.create(new LoadStops())
        .subscribeOn(Schedulers.newThread()).subscribeOn(Schedulers.newThread());

    // Display stops on map after querying db
    loadStopsObserver = loadStops.subscribeWith(new DisplayStops());
  }

  /**
   * Displays a 'my location button' on the map.
   */
  private void setupMyLocationOverlay() {
    MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(
        getApplicationContext()), map);
    myLocationNewOverlay.enableFollowLocation();
    myLocationNewOverlay.enableMyLocation();
    IMapController controller = map.getController();

    if (myLocationNewOverlay.isMyLocationEnabled()) {
      map.getOverlays().add(myLocationNewOverlay);

      // Set center of the map
      controller.setCenter(myLocationNewOverlay.getMyLocation());
      controller.setZoom(ZOOM_2);
    } else {
      // Default to area center
      controller.setCenter(CENTER);
      controller.setZoom(ZOOM);
    }
  }

  /**
   * Choose the current stop for schedule fetching, back in the main activity.
   */
  private void chooseStop() {
    if (selectedStopIdx != -1) {
      Intent intent = new Intent(getApplicationContext(), MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.putExtra(Stop.EXTRA_STOP, stops.get(selectedStopIdx));
      startActivity(intent);
      finish();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    map.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    map.onPause();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    loadStopsObserver.dispose();
  }

  @Override
  public void finish() {
    ViewGroup view = (ViewGroup) getWindow().getDecorView();
    view.removeAllViews();
    super.finish();
  }

  /**
   * Loads stops and corresponding geo-points from the database.
   */
  private class LoadStops implements SingleOnSubscribe<List<IGeoPoint>> {

    @Override
    public void subscribe(SingleEmitter<List<IGeoPoint>> emitter) throws Exception {
      try {
        GtfsRoomDatabase db = GtfsRoomDatabase.getDatabase(getApplicationContext());
        List<IGeoPoint> points = new ArrayList<>();
        stops = new ArrayList<>();

        for (Stop stop : db.stopDao().loadAllStops()) {
          // Save actual stop objects for later
          stops.add(stop);
          points.add(new LabelledGeoPoint(stop.stopLat, stop.stopLon, stop.stopName));
        }

        emitter.onSuccess(points);
      } catch (Exception e) {
        emitter.onError(e);
      }
    }
  }

  /**
   * Loads the points onto the map UI.
   */
  private class DisplayStops extends DisposableSingleObserver<List<IGeoPoint>> {

    @Override
    public void onSuccess(List<IGeoPoint> points) {
      // wrap them in a theme
      SimplePointTheme pt = new SimplePointTheme(points, false);

      // create label style
      Paint pointStyle = new Paint();
      pointStyle.setStyle(Paint.Style.FILL);
      pointStyle.setColor(Color.parseColor("#48C873"));

      // set some visual options for the overlay
      // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
      SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
          .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
          .setRadius(7).setIsClickable(true).setCellSize(15).setPointStyle(pointStyle);

      // create the overlay with the theme
      final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt, opt);

      // onClick callback
      sfpo.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
        @Override
        public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
          Toast.makeText(map.getContext()
              , stops.get(point).stopName
              , Toast.LENGTH_SHORT).show();

          // Enable the "choose" button
          chooseStop.setVisibility(View.VISIBLE);
          selectedStopIdx = point;
        }
      });

      // add overlay
      map.getOverlays().add(sfpo);
    }

    @Override
    public void onError(final Throwable e) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(map.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
          finish();
        }
      });
    }
  }
}
