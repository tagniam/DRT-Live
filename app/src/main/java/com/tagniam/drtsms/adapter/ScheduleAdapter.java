package com.tagniam.drtsms.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tagniam.drtsms.R;
import com.tagniam.drtsms.schedule.data.BusTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
   * Returns the difference of two dates as Map
   *
   * Modified from https://stackoverflow.com/questions/1555262/
   * calculating-the-difference-between-two-java-date-instances/10650881#10650881.
   *
   * @param now the current date
   * @param next the next date
   */
  private static Map<TimeUnit, Long> computeDiff(Date now, Date next) {
    long diffInMillies = next.getTime() - now.getTime();
    List<TimeUnit> units = new ArrayList<TimeUnit>(EnumSet.allOf(TimeUnit.class));
    Collections.reverse(units);
    Map<TimeUnit, Long> result = new LinkedHashMap<TimeUnit, Long>();
    long milliesRest = diffInMillies;
    for (TimeUnit unit : units) {
      long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
      long diffInMilliesForUnit = unit.toMillis(diff);
      milliesRest = milliesRest - diffInMilliesForUnit;
      result.put(unit, diff);
    }
    return result;
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
    holder.times.setText(formatTimes(curr.getTimes()));
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
   * Formats the list of times to be readable.
   *
   * @param times List of Date
   * @return time string in format {time}| {time}|...
   */
  private String formatTimes(List<Date> times) {
    Date now = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.CANADA);
    StringBuilder str = new StringBuilder();
    for (Date time : times) {
      str.append(nextTime(now, time)).append(", ");
    }
    return str.toString().substring(0, str.length() - 2);
  }

  private String nextTime(Date now, Date next) {
    StringBuilder str = new StringBuilder();

    Map<TimeUnit, Long> diff = computeDiff(now, next);
    int days = diff.get(TimeUnit.DAYS).intValue();
    int hours = diff.get(TimeUnit.HOURS).intValue();
    int minutes = diff.get(TimeUnit.MINUTES).intValue();

    if (days != 0) {
      str.append(days).append(" days").append(" ");
    }

    if (hours != 0) {
      str.append(hours).append(" hrs").append(" ");
    }

    if (minutes != 0) {
      str.append(minutes).append(" mins");
    }

    return str.toString();
  }

  /**
   * Contents of each bus time card.
   */
  static class BusTimeHolder extends RecyclerView.ViewHolder {

    TextView route;
    TextView direction;
    TextView times;

    BusTimeHolder(View itemView) {
      super(itemView);
      route = itemView.findViewById(R.id.route);
      direction = itemView.findViewById(R.id.direction);
      times = itemView.findViewById(R.id.times);
    }
  }
}
