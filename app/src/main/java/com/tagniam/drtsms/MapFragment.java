package com.tagniam.drtsms;

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.tagniam.drtsms.adapter.ScheduleAdapter;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import com.tagniam.drtsms.database.stops.Stop;
import com.tagniam.drtsms.schedule.data.Schedule;
import com.tagniam.drtsms.schedule.fetcher.ScheduleFetcher;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;

public class MapFragment extends Fragment {

  private static final GeoPoint MAP_CENTER = new GeoPoint(43.90546, -78.9563);
  private static final double MAP_MIN_ZOOM = 14.0;
  private static final double MAP_MAX_ZOOM = 20.0;
  private static final double MAP_LOCAL_ZOOM = 18.0;
  private static final double MAP_OVERLAY_MIN_ZOOM = 17.0;
  private static final int FINE_LOCATION_PERMISSION_REQUEST = 0;

  /** Map shit **/
  private MapView map;
  private List<Stop> stops = new ArrayList<>();
  private List<LabelledGeoPoint> points = new ArrayList<>();
  private MyLocationNewOverlay locationOverlay;
  private FloatingActionButton locationButton;
  private FolderOverlay overlay;
  private FloatingSearchView searchView;

  /** Bottom sheet **/
  private RecyclerView scheduleView;
  private BottomSheetBehavior bottomSheetBehavior;
  private ScheduleFetcher scheduleFetcher;
  private Disposable scheduleFetcherDisposable;
  private BroadcastReceiver timeTickReceiver;
  private ScheduleAdapter scheduleAdapter;

  /** Schedule fetcher shit **/


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_map, container, false);
    scheduleView = view.findViewById(R.id.scheduleDisplay);
    scheduleView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
    return view;
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupMap(view);
    setupMapPoints();
    setupSearchView(view);
    setupBottomSheetBehavior(view);
    setupLocationButton(view);
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
            new Function<List<Stop>, List<LabelledGeoPoint>>() {
              @Override
              public List<LabelledGeoPoint> apply(List<Stop> busStops) {
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
            new DisposableSingleObserver<List<LabelledGeoPoint>>() {
              @Override
              public void onSuccess(final List<LabelledGeoPoint> points) {
                // Add points to map
                overlay = new FolderOverlay();
                List<OverlayItem> items = new ArrayList<>();

                for (LabelledGeoPoint point : points) {
                  OverlayItem item = new OverlayItem("", "", point);
                  item.setMarker(ContextCompat.getDrawable(getActivity(), R.drawable.marker_bus));
                  items.add(new OverlayItem("", "", point));
                }


                Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.marker_bus);
                ItemizedIconOverlay<OverlayItem> mOverlay =
                        new ItemizedIconOverlay<>(items, icon, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                          @Override
                          public boolean onItemSingleTapUp(int index, OverlayItem item) {
                            Stop stop = stops.get(index);
                            fetchSchedule(stop.stopCode, stop.stopName);
                            return false;
                          }

                          @Override
                          public boolean onItemLongPress(int index, OverlayItem item) {
                            return false;
                          }
                        }, getActivity().getApplicationContext());

                map.getOverlays().add(mOverlay);
                System.out.println(items.size());


                //for (final LabelledGeoPoint point : points) {
                //  OverlayItem overlayItem = new OverlayItem("", "", point);
                //  overlayItem.set



                //  Marker marker = new Marker(map);
                //  marker.setPosition(new GeoPoint(point.getLatitude(), point.getLongitude()));
                //  marker.setInfoWindow(null);
                //  marker.setIcon(ContextCompat.getDrawable(getActivity(), R.drawable.marker_bus));
                //  marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                //    @Override
                //    public boolean onMarkerClick(Marker marker, MapView mapView) {
                //      Stop stop = stops.get(points.indexOf(point));
                //      fetchSchedule(stop.stopCode, stop.stopName);
                //      return false;
                //    }
                //  });
                //  overlay.add(marker);
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
  private Stop getNearestStop(GeoPoint location) {
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
    }
    return stops.get(index);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

  }

  @Override
  public void onDetach() {
    super.onDetach();
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
        map.scrollTo((int) point.getLatitude(), (int) point.getLongitude());
        map.getController().animateTo(point);
      }
    }
  }

  private void setupSearchView(View view) {
    // Setup search view
    searchView = view.findViewById(R.id.floating_search_view);
    searchView.setLeftActionMode(FloatingSearchView.LEFT_ACTION_MODE_SHOW_SEARCH);
    searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
      @Override
      public void onSearchTextChanged(String oldQuery, String newQuery) {
        // get suggestions
        String query = "%" + newQuery.replace(" ", "%") + "%";

        Single.just(query)
                .map(new Function<String, List<Stop>>() {
                  @Override
                  public List<Stop> apply(String s) throws Exception {
                    return GtfsRoomDatabase.getDatabase(getActivity().getApplicationContext())
                            .stopDao()
                            .findStopsByNameOrId(s);
                  }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<Stop>>() {
                  @Override
                  public void onSuccess(List<Stop> stops) {
                    // Construct search suggestions
                    List<SearchSuggestion> suggestions = new ArrayList<>();
                    for (Stop stop : stops) {
                      suggestions.add(new StopSuggestion(stop));
                    }

                    searchView.swapSuggestions(suggestions);
                  }

                  @Override
                  public void onError(Throwable e) {

                  }
                });
      }
    });

    searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
      @Override
      public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
        // Cast suggestion to query suggestion
        StopSuggestion suggestion = (StopSuggestion) searchSuggestion;
        searchView.clearSearchFocus();
        searchView.setSearchText("");
        fetchSchedule(suggestion.getStopCode(), suggestion.getStopName());
      }

      @Override
      public void onSearchAction(String currentQuery) {
        Toast.makeText(getActivity().getApplicationContext(), currentQuery, Toast.LENGTH_SHORT).show();

      }
    });
  }

  private void setupMap(View view) {
    // Setup map
    map = view.findViewById(R.id.map);
    map.setTileSource(TileSourceFactory.MAPNIK);
    map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
    map.setMultiTouchControls(true);

    map.setMaxZoomLevel(MAP_MAX_ZOOM);
    map.setMinZoomLevel(MAP_MIN_ZOOM);
    map.getController().setCenter(MAP_CENTER);
    map.getController().setZoom(MAP_MIN_ZOOM);

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
                  map.getOverlays().remove(overlay);
                } else if (!map.getOverlays().contains(overlay)) {
                  map.getOverlays().add(overlay);
                }
                return false;
              }
            });
  }

  private void setupLocationButton(View view) {
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
                                                    Stop stop = getNearestStop(location);

                                                    // Zoom/center/select nearest stop
                                                    int index = stops.indexOf(stop);
                                                    IGeoPoint point = points.get(index);
                                                    map.scrollTo((int) point.getLatitude(), (int) point.getLongitude());
                                                    map.getController().setZoom(MAP_LOCAL_ZOOM);
                                                    map.getController().setCenter(points.get(index));

                                                    if (stop.stopCode == null) {
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
                                                      fetchSchedule(stop.stopCode, stop.stopName);
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

  }

  public void hideLocationButton() {
    locationButton.setVisibility(View.INVISIBLE);
  }

  public void showLocationButton() {
    locationButton.setVisibility(View.VISIBLE);
  }

  /**
   * Fetches the schedule.
   */
  public void fetchSchedule(final String stopId, final String stopName) {
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    // Set title bar text
    TextView stopNameTextView = getActivity().findViewById(R.id.stopName);
    stopNameTextView.setText(stopName);
    // Set progress bar
    ProgressBar progressBar = getActivity().findViewById(R.id.progressBar);
    progressBar.setIndeterminate(true);

    clickStop(stopId);

    scheduleFetcher = ScheduleFetcher.getFetcher(getActivity().getApplicationContext(), stopId);
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
                                  displaySchedule(schedule);
                                }
                              }

                              @Override
                              public void onError(Throwable e) {
                                Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG)
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
    // Set progress bar
    ProgressBar progressBar = getActivity().findViewById(R.id.progressBar);
    progressBar.setIndeterminate(false);
    // Display bottom sheet schedule
    scheduleAdapter = new ScheduleAdapter(getActivity().getApplicationContext(), schedule.getBusTimes(),
            new Date());
    scheduleView.setAdapter(scheduleAdapter);

    // Update time every time a minute passes
    timeTickReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        // Get current time
        updateSchedule();
      }
    };

    getActivity().registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
  }

  private void updateSchedule() {
    scheduleAdapter.updateTimes(new Date());
    scheduleAdapter.notifyDataSetChanged();
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (scheduleFetcherDisposable != null && !scheduleFetcherDisposable.isDisposed()) {
      scheduleFetcherDisposable.dispose();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (scheduleFetcher != null) {
      scheduleFetcher.onPause();
    }
    if (timeTickReceiver != null) {
      getActivity().unregisterReceiver(timeTickReceiver);
    }
    if (locationOverlay != null) {
      locationOverlay.disableMyLocation();
    }
    map.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (scheduleFetcher != null) {
      scheduleFetcher.onResume();
    }
    if (timeTickReceiver != null) {
      getActivity().registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }
    if (scheduleAdapter != null) {
      updateSchedule();
    }
    if (locationOverlay != null) {
      locationOverlay.enableMyLocation();
    }
    map.onResume();
  }

  private void setupBottomSheetBehavior(View view) {
    bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottom_sheet));
    // Setup bottom sheet  <include layout="@layout/sheet_bottom"/>
    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        switch (newState) {
          case BottomSheetBehavior.STATE_HIDDEN:
            showLocationButton();
            //disableDim();
            // Clear click on map and search when bottom sheet gets hidden
            clearClick();
            //stopIdInput.setQuery("", false);
            searchView.setSearchText("");
            break;
          case BottomSheetBehavior.STATE_EXPANDED:
            hideLocationButton();
            break;
          case BottomSheetBehavior.STATE_COLLAPSED:
            hideLocationButton();
            break;
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {

      }
    });

  }
  /** Clears any selection made. */
  public void clearClick() {
    //pointsOverlay.setSelectedPoint(null);
  }

  public void enableDim() {
    //mapFrame.getForeground().setAlpha(100);
    //mapFrame.invalidate();
  }

  public void disableDim() {
    //mapFrame.getForeground().setAlpha(0);
    //mapFrame.invalidate();
  }
}
