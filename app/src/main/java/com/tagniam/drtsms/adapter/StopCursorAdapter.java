package com.tagniam.drtsms.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.tagniam.drtsms.MainActivity;
import com.tagniam.drtsms.R;
import com.tagniam.drtsms.database.stops.Stop;

public class StopCursorAdapter extends CursorAdapter {
  private LayoutInflater layoutInflater;
  private SearchView searchView;

  public StopCursorAdapter(Context context, Cursor cursor, SearchView sv) {
    super(context, cursor, false);
    this.searchView = sv;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return layoutInflater.inflate(R.layout.search_item, parent, false);
  }

  @Override
  public void bindView(View view, final Context context, Cursor cursor) {
    String stopName = cursor.getString(cursor.getColumnIndexOrThrow("stop_name"));
    String stopId = cursor.getString(cursor.getColumnIndexOrThrow("stop_code"));

    TextView searchStopName = view.findViewById(R.id.search_stop_name);
    searchStopName.setText(stopName);

    TextView searchStopId = view.findViewById(R.id.search_stop_id);
    searchStopId.setText("Stop #" + stopId);

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TextView searchStopId = view.findViewById(R.id.search_stop_id);
        String stopId = searchStopId.getText().toString();
        searchView.setQuery(stopId.substring(6), true);
      }
    });

  }

}
