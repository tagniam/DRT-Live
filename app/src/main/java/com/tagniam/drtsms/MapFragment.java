package com.tagniam.drtsms;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import com.tagniam.drtsms.database.stops.Stop;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

public class MapFragment extends Fragment {

  private MapView map;
  private static final GeoPoint MAP_CENTER = new GeoPoint(43.90546, -78.9563);
  private static final double MAP_MIN_ZOOM = 10.0;
  private static final double MAP_MAX_ZOOM = 20.0;

  private List<Stop> stops = new ArrayList<>();
  private List<IGeoPoint> points = new ArrayList<>();
  private SimpleFastPointOverlay pointsOverlay;
  private SimpleFastPointOverlayOptions pointOptions;
  OnStopClickListener callback;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_map, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // Setup map
    map = view.findViewById(R.id.map);
    map.setTileSource(TileSourceFactory.MAPNIK);
    map.setBuiltInZoomControls(false);
    map.setMultiTouchControls(true);

    map.setMaxZoomLevel(MAP_MAX_ZOOM);
    map.setMinZoomLevel(MAP_MIN_ZOOM);
    map.getController().setCenter(MAP_CENTER);
    map.getController().setZoom(MAP_MIN_ZOOM);

    // Setup point style
    Paint pointStyle = new Paint();
    pointStyle.setStyle(Paint.Style.FILL);
    pointStyle.setColor(Color.parseColor("#48C873"));

    // Setup point options
    pointOptions = SimpleFastPointOverlayOptions.getDefaultStyle()
        .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
        .setRadius(7).setIsClickable(true).setCellSize(15).setPointStyle(pointStyle);

    // Setup map points
    setupMapPoints();
  }

  /**
   * Gets all the stops from the database and displays them on the map.
   */
  private void setupMapPoints() {
    Single.fromCallable(new Callable<List<Stop>>() {
      @Override
      public List<Stop> call() {
        // Query database for stops
        return GtfsRoomDatabase.getDatabase(getActivity().getApplicationContext())
            .stopDao().loadAllStops();
      }
    }).map(new Function<List<Stop>, List<IGeoPoint>>() {
      @Override
      public List<IGeoPoint> apply(List<Stop> busStops) {
        // Convert stops to points using co-ordinates and stop name
        stops = new ArrayList<>();
        stops.addAll(busStops);
        points = new ArrayList<>();
        for (Stop stop : busStops) {
          points.add(new LabelledGeoPoint(stop.stopLat, stop.stopLon, stop.stopName));
        }
        return points;
      }
    }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DisposableSingleObserver<List<IGeoPoint>>() {
          @Override
          public void onSuccess(List<IGeoPoint> points) {
            // Add points to map, using configured point style & options
            SimplePointTheme pt = new SimplePointTheme(points, false);
            pointsOverlay = new SimpleFastPointOverlay(pt, pointOptions);

            pointsOverlay.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
              @Override
              public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
                // Notify activity which stop we selected
                callback.onStopClick(stops.get(point).stopCode);
              }
            });

            map.getOverlays().add(pointsOverlay);
          }

          @Override
          public void onError(Throwable e) {
            Toast.makeText(getActivity().getApplicationContext(),
                getActivity().getApplicationContext().getResources()
                    .getString(R.string.error_generic),
                Toast.LENGTH_SHORT).show();
          }
        });
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    try {
      callback = (OnStopClickListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement OnStopClickListener");
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    map.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    map.onResume();
  }

  /**
   * Selects the given stop on the map and zooms in/centers on it.
   *
   * @param stopCode id of the stop to select
   */
  public void clickStop(String stopCode) {
    // Find stop with the given stopCode
    for (int i = 0; i < stops.size(); i++) {
      Stop stop = stops.get(i);
      if (stop.stopCode.equals(stopCode)) {
        // Corresponding point will be at the same index
        final IGeoPoint point = points.get(i);

        // Select, center & zoom to point
        map.getController().setCenter(point);
        map.getController().setZoom(18.f);
        pointsOverlay.setSelectedPoint(i);
        map.getController().animateTo(point);
      }
    }
  }

  /**
   * Detect when a stop is clicked on the map.
   */
  public interface OnStopClickListener {

    void onStopClick(String stopCode);
  }

}
