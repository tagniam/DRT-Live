package com.tagniam.drtsms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

public class MapActivity extends AppCompatActivity {

  MapView map = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    map = findViewById(R.id.map);
    map.setTileSource(TileSourceFactory.MAPNIK);
    map.setBuiltInZoomControls(true);
    map.setMultiTouchControls(true);

    // Setup markers
    List<OverlayItem> stops = new ArrayList<>();
    stops.add(new OverlayItem("William Jackson & Brock", "Stop",
        new GeoPoint(43.87551d, -79.085892d)));

    ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<>(
        getApplicationContext(), stops,
        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
          @Override
          public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
            //do something
            return true;
          }

          @Override
          public boolean onItemLongPress(final int index, final OverlayItem item) {
            return false;
          }
        });
    mOverlay.setFocusItemsOnTap(true);
    map.getOverlays().add(mOverlay);


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
