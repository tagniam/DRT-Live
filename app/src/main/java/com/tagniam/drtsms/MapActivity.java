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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

public class MapActivity extends AppCompatActivity {

  private MapView map;
  private GtfsRoomDatabase db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    map = findViewById(R.id.map);
    map.setTileSource(TileSourceFactory.MAPNIK);
    map.setBuiltInZoomControls(true);
    map.setMultiTouchControls(true);

    // Load stop points from database
    Single<List<IGeoPoint>> loadStops =
        Single.create(
            new SingleOnSubscribe<List<IGeoPoint>>() {

              @Override
              public void subscribe(SingleEmitter<List<IGeoPoint>> emitter) throws Exception {
                try {
                  GtfsRoomDatabase db = GtfsRoomDatabase.getDatabase(getApplicationContext());
                  List<IGeoPoint> stops = new ArrayList<>();
                  for (Stop stop : db.stopDao().loadAllStops()) {
                    stops.add(new LabelledGeoPoint(stop.stopLat, stop.stopLon, stop.stopName));
                  }
                  emitter.onSuccess(stops);
                } catch (Exception e) {
                  emitter.onError(e);
                }
              }
            })
            .subscribeOn(Schedulers.newThread());

    DisposableSingleObserver<List<IGeoPoint>> loadStopsObserver = loadStops.subscribeWith(
        new DisposableSingleObserver<List<IGeoPoint>>() {
          @Override
          public void onSuccess(List<IGeoPoint> stops) {
            // wrap them in a theme
            SimplePointTheme pt = new SimplePointTheme(stops, true);

            // create label style
            Paint pointStyle = new Paint();
            pointStyle.setStyle(Paint.Style.FILL);
            pointStyle.setColor(Color.parseColor("#0000ff"));
            pointStyle.setTextAlign(Paint.Align.CENTER);
            pointStyle.setTextSize(24);

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
                    , "You clicked " + ((LabelledGeoPoint) points.get(point)).getLabel()
                    , Toast.LENGTH_SHORT).show();
              }
            });

            // add overlay
            map.getOverlays().add(sfpo);

          }

          @Override
          public void onError(Throwable e) {

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

}
