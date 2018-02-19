package com.tagniam.drtsms;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

public class MapActivity extends AppCompatActivity {

  private static final GeoPoint CENTER = new GeoPoint(43.90546, -78.9563);
  private static final double ZOOM = 9.f;
  private MapView map;
  private DisposableSingleObserver<List<IGeoPoint>> loadStopsObserver;
  private List<Stop> stops;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    // Setup map
    map = findViewById(R.id.map);
    map.setTileSource(TileSourceFactory.MAPNIK);
    map.setBuiltInZoomControls(true);
    map.setMultiTouchControls(true);
    IMapController controller = map.getController();
    controller.setCenter(CENTER);
    controller.setZoom(ZOOM);

    // Load stop points from database
    Single<List<IGeoPoint>> loadStops =
        Single.create(
            new SingleOnSubscribe<List<IGeoPoint>>() {

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
            })
            .subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread());

    // Display stops on map after querying db
    loadStopsObserver = loadStops.subscribeWith(
        new DisposableSingleObserver<List<IGeoPoint>>() {
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
                    , "You clicked " + stops.get(point).stopCode
                    , Toast.LENGTH_SHORT).show();
              }
            });

            // add overlay
            map.getOverlays().add(sfpo);
          }

          @Override
          public void onError(Throwable e) {
            Toast.makeText(map.getContext(), "Something went wrong with the map",
                Toast.LENGTH_SHORT).show();
            finish();
          }
        });
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
    map.onDetach();
    loadStopsObserver.dispose();
  }

}
