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
import com.tagniam.drtsms.R;

public class StopCursorAdapter extends CursorAdapter {
  private LayoutInflater layoutInflater;
  private Context context;
  private SearchView searchView;

  public StopCursorAdapter(Context context, Cursor cursor, SearchView sv) {
    super(context, cursor, false);
    this.context = context;
    this.searchView = sv;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    View v = layoutInflater.inflate(R.layout.search_item, parent, false);
    return v;
  }

  @Override
  public void bindView(View view, final Context context, Cursor cursor) {
    String stopName = cursor.getString(cursor.getColumnIndexOrThrow("stop_name"));
    String stopId = cursor.getString(cursor.getColumnIndexOrThrow("stop_code"));

    TextView searchStopName = view.findViewById(R.id.search_stop_name);
    searchStopName.setText(stopName);

    TextView searchStopId = view.findViewById(R.id.search_stop_id);
    searchStopId.setText("Id " + stopId);

    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        //take next action based user selected item
        TextView name = view.findViewById(R.id.search_stop_name);
        searchView.setIconified(true);
        Toast.makeText(context, "Selected suggestion " + name.getText(),
            Toast.LENGTH_LONG).show();
      }
    });

  }

}
