package com.tagniam.drtsms.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tagniam.drtsms.R;
import com.tagniam.drtsms.schedule.data.BusTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Created by jr on 07/12/17. */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.BusTimeHolder> {
  private List<BusTime> busTimes = new ArrayList<>();

  /**
   * Setup the adapter with a list of bus times.
   * @param busTimes list of bus time objects
   */
  public ScheduleAdapter(List<BusTime> busTimes) {
    this.busTimes = busTimes;
  }

  /**
   * Initializes the view holder and specifies the layout of each item in the schedule view.
   * @param parent parent ViewGroup
   * @param viewType type of view
   * @return BusTimeHolder with card view layout
   */
  @Override
  public BusTimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new BusTimeHolder(LayoutInflater.from(parent.getContext()).inflate(
            R.layout.card_schedule, parent, false));
  }

  /**
   * Specifies the content of each item in the schedule view.
   * @param holder BusTimeHolder to be modified
   * @param position item in the list to be displayed
   */
  @Override
  public void onBindViewHolder(BusTimeHolder holder, int position) {
    BusTime curr = busTimes.get(position);
    holder.route.setText(curr.getRoute());
    holder.direction.setText(curr.getDirection());
  }

  /**
   * Returns the number of bus times in the schedule view.
   * @return # of bus times in schedule view
   */
  @Override
  public int getItemCount() {
    return busTimes.size();
  }

  /**
   * Contents of each bus time card.
   */
  static class BusTimeHolder extends RecyclerView.ViewHolder {

    TextView route;
    TextView direction;

    BusTimeHolder(View itemView) {
      super(itemView);
      route = itemView.findViewById(R.id.route);
      direction = itemView.findViewById(R.id.direction);
    }
  }
}
