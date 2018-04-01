package com.tagniam.drtsms;

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
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

public class MapFragment extends Fragment {

  private MapView map;
  private SimpleFastPointOverlayOptions pointOptions;

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
    map.setBuiltInZoomControls(true);
    map.setMultiTouchControls(true);

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

  private void setupMapPoints() {
    Single.fromCallable(new Callable<List<Stop>>() {
      @Override
      public List<Stop> call() {
        // Query database for stops
        return GtfsRoomDatabase.getDatabase(getView().getContext())
            .stopDao().loadAllStops();
      }
    }).map(new Function<List<Stop>, List<IGeoPoint>>() {
      @Override
      public List<IGeoPoint> apply(List<Stop> stops) {
        // Convert stops to points using co-ordinates and stop name
        List<IGeoPoint> points = new ArrayList<>();
        for (Stop stop : stops) {
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
            SimpleFastPointOverlay pointsOverlay = new SimpleFastPointOverlay(pt, pointOptions);

            pointsOverlay.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
              @Override
              public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
                Toast.makeText(map.getContext()
                    , ((LabelledGeoPoint) points.get(point)).getLabel()
                    , Toast.LENGTH_SHORT).show();
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
  public void onPause() {
    super.onPause();
    map.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    map.onResume();
  }

}
