package com.tagniam.drtsms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

public class MapActivity extends AppCompatActivity {

  MapView map = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_map);

    map = findViewById(R.id.map);
    map.setTileSource(TileSourceFactory.MAPNIK);
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
