package com.tagniam.drtsms;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import com.tagniam.drtsms.database.stops.Stop;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

public class MapFragment extends Fragment {

  private static final GeoPoint MAP_CENTER = new GeoPoint(43.90546, -78.9563);
  private static final double MAP_MIN_ZOOM = 14.0;
  private static final double MAP_MAX_ZOOM = 20.0;
  private static final double MAP_LOCAL_ZOOM = 18.0;
  private static final double MAP_OVERLAY_MIN_ZOOM = 17.0;
  private static final int FINE_LOCATION_PERMISSION_REQUEST = 0;
  private MapView map;
  private List<Stop> stops = new ArrayList<>();
  private List<IGeoPoint> points = new ArrayList<>();
  private SimpleFastPointOverlay pointsOverlay;
  private SimpleFastPointOverlayOptions pointOptions;
  private OnStopClickListener callback;
  private MyLocationNewOverlay locationOverlay;
  private FrameLayout mapFrame;
  private FloatingActionButton locationButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    // Setup dimmable
    mapFrame = view.findViewById(R.id.map_frame);
    mapFrame.getForeground().setAlpha(0);
    mapFrame.invalidate();

    // Setup point style
    Paint pointStyle = new Paint();
    pointStyle.setStyle(Paint.Style.FILL);
    pointStyle.setColor(Color.parseColor("#48C873"));

    // Setup point options
    pointOptions =
        SimpleFastPointOverlayOptions.getDefaultStyle()
            .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
            .setRadius(7)
            .setIsClickable(true)
            .setCellSize(15)
            .setPointStyle(pointStyle);

    // Setup location button
    locationButton = view.findViewById(R.id.location);
    locationButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View view) {
            // Make sure we have permissions to use location
            if (ActivityCompat.checkSelfPermission(
                getActivity().getApplicationContext(), permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
              // Request permissions first
              ActivityCompat.requestPermissions(
                  getActivity(),
                  new String[]{permission.ACCESS_FINE_LOCATION},
                  FINE_LOCATION_PERMISSION_REQUEST);
            } else {
              // Setup overlay for my location, make sure we don't duplicate an existing one
              if (locationOverlay == null) {
                locationOverlay =
                    new MyLocationNewOverlay(
                        new GpsMyLocationProvider(getActivity().getApplicationContext()), map);
                map.getOverlays().add(locationOverlay);
              }

              locationOverlay.enableMyLocation();

              if (locationOverlay.isMyLocationEnabled()) {
                // When location is available, set center/zoom and enter nearest stop automagically
                final MyLocationNewOverlay myLocationNewOverlay = locationOverlay;
                locationOverlay.runOnFirstFix(
                    new Runnable() {
                      @Override
                      public void run() {
                        // Have to run this on main thread through RxJava to update UI
                        Single.just(myLocationNewOverlay.getMyLocation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                new DisposableSingleObserver<GeoPoint>() {
                                  @Override
                                  public void onSuccess(GeoPoint location) {
                                    String stopCode = clickNearestStop(location);
                                    if (stopCode == null) {
                                      // Notify user and set center/zoom to current location
                                      Toast.makeText(
                                          getActivity().getApplicationContext(),
                                          getActivity()
                                              .getApplicationContext()
                                              .getResources()
                                              .getString(R.string.error_generic),
                                          Toast.LENGTH_SHORT)
                                          .show();
                                      map.getController().setCenter(location);
                                      map.getController().setZoom(MAP_LOCAL_ZOOM);
                                    } else {
                                      // Do some magic!
                                      callback.onStopClick(stopCode);
                                    }
                                  }

                                  @Override
                                  public void onError(Throwable e) {
                                    Toast.makeText(
                                        getActivity().getApplicationContext(),
                                        e.getMessage(),
                                        Toast.LENGTH_SHORT)
                                        .show();
                                  }
                                });
                      }
                    });
              } else {
                Toast.makeText(
                    getActivity().getApplicationContext(),
                    getActivity()
                        .getApplicationContext()
                        .getResources()
                        .getString(R.string.notification_location),
                    Toast.LENGTH_SHORT)
                    .show();
              }
            }
          }
        });

    // Setup zoom listener
    map.addMapListener(
        new MapListener() {
          @Override
          public boolean onScroll(ScrollEvent event) {
            return false;
          }

          @Override
          public boolean onZoom(ZoomEvent event) {
            if (event.getZoomLevel() < MAP_OVERLAY_MIN_ZOOM) {
              map.getOverlays().remove(pointsOverlay);
            } else if (!map.getOverlays().contains(pointsOverlay)) {
              map.getOverlays().add(pointsOverlay);
            }
            return false;
          }
        });
    // Setup map points
    setupMapPoints();
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == FINE_LOCATION_PERMISSION_REQUEST
        && grantResults.length == 1
        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      locationButton.performClick();
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  /**
   * Gets all the stops from the database and displays them on the map.
   */
  private void setupMapPoints() {
    Single.fromCallable(
        new Callable<List<Stop>>() {
          @Override
          public List<Stop> call() {
            // Query database for stops
            return GtfsRoomDatabase.getDatabase(getActivity().getApplicationContext())
                .stopDao()
                .loadAllStops();
          }
        })
        .map(
            new Function<List<Stop>, List<IGeoPoint>>() {
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
            })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            new DisposableSingleObserver<List<IGeoPoint>>() {
              @Override
              public void onSuccess(List<IGeoPoint> points) {
                // Add points to map, using configured point style & options
                SimplePointTheme pt = new SimplePointTheme(points, false);
                pointsOverlay = new SimpleFastPointOverlay(pt, pointOptions);

                pointsOverlay.setOnClickListener(
                    new SimpleFastPointOverlay.OnClickListener() {
                      @Override
                      public void onClick(
                          SimpleFastPointOverlay.PointAdapter points, Integer point) {
                        // Notify activity which stop we selected
                        callback.onStopClick(stops.get(point).stopCode);
                      }
                    });
              }

              @Override
              public void onError(Throwable e) {
                Toast.makeText(
                    getActivity().getApplicationContext(),
                    getActivity()
                        .getApplicationContext()
                        .getResources()
                        .getString(R.string.error_generic),
                    Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  /**
   * Clicks the stop nearest to the given location.
   *
   * @param location location, preferably current location
   * @return stop code of nearest stop, or null if no stops found
   */
  private String clickNearestStop(GeoPoint location) {
    int index = -1;
    double minDistance = Double.MAX_VALUE;
    for (int i = 0; i < points.size(); i++) {
      double distance = location.distanceToAsDouble(points.get(i));
      if (distance < minDistance) {
        index = i;
        minDistance = distance;
      }
    }

    if (index == -1) {
      return null;
    } else {
      // Zoom/center/select nearest stop
      pointsOverlay.setSelectedPoint(index);
      map.getController().setZoom(MAP_LOCAL_ZOOM);
      map.getController().setCenter(points.get(index));
      return stops.get(index).stopCode;
    }
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
  public void onDetach() {
    super.onDetach();
    callback = null;
  }

  @Override
  public void onPause() {
    super.onPause();
    map.onPause();
    if (locationOverlay != null) {
      locationOverlay.disableMyLocation();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    map.onResume();
    if (locationOverlay != null) {
      locationOverlay.enableMyLocation();
    }
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
        map.getController().setZoom(MAP_LOCAL_ZOOM);
        pointsOverlay.setSelectedPoint(i);
        map.getController().animateTo(point);
      }
    }
  }

  /** Clears any selection made. */
  public void clearClick() {
    pointsOverlay.setSelectedPoint(null);
  }

  public void hideLocationButton() {
    locationButton.setVisibility(View.INVISIBLE);
  }

  public void showLocationButton() {
    locationButton.setVisibility(View.VISIBLE);
  }

  public void enableDim() {
    mapFrame.getForeground().setAlpha(100);
    mapFrame.invalidate();
  }

  public void disableDim() {
    mapFrame.getForeground().setAlpha(0);
    mapFrame.invalidate();
  }

  /** Detect when a stop is clicked on the map. */
  public interface OnStopClickListener {

    void onStopClick(String stopCode);
  }
}
