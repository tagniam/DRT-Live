package com.tagniam.drtsms.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.tagniam.drtsms.R;
import com.tagniam.drtsms.database.GtfsRoomDatabase;
import com.tagniam.drtsms.schedule.data.BusTime;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import java.util.Date;
import java.util.List;

/**
 * Created by jr on 07/12/17.
 */
public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.BusTimeHolder> {

  private Context context;
  private List<BusTime> busTimes;
  private Date now;

  /**
   * Setup the adapter with a list of bus times.
   *
   * @param context application context
   * @param busTimes list of bus time objects
   * @param now the current time
   */
  public ScheduleAdapter(Context context, List<BusTime> busTimes, Date now) {
    this.context = context;
    this.busTimes = busTimes;
    this.now = now;
  }

  /**
   * Initializes the view holder and specifies the layout of each item in the schedule view.
   *
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
   * Sets up the layout of the route and direction.
   *
   * @param holder current BusTimeHolder
   * @param busTime current BusTime data to display
   */
  private void setupRouteDirectionLayout(final BusTimeHolder holder, BusTime busTime) {
    holder.direction.setText(busTime.getDirection());
    holder.route.setText(busTime.getRoute());

    // Find route's name with database query
    Single.just(busTime.getRoute())
        .map(new Function<String, String>() {
          @Override
          public String apply(String shortName) {
            return GtfsRoomDatabase.getDatabase(context)
                .routeDao().findLongNameByShortName(shortName);
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new DisposableSingleObserver<String>() {

          @Override
          public void onSuccess(String name) {
            holder.name.setText(name);
          }

          @Override
          public void onError(Throwable e) {
            Toast.makeText(context,
                context.getResources().getString(R.string.error_generic),
                Toast.LENGTH_SHORT).show();
          }
        });
  }

  /**
   * Sets up the layout of the time display and tab indicators.
   *
   * @param holder current BusTimeHolder
   * @param busTime current BusTime data to display
   */
  private void setupTimeLayout(BusTimeHolder holder, BusTime busTime) {
    // Creates the tab layout for the times
    TimePagerAdapter timePagerAdapter = new TimePagerAdapter(context, busTime, now);
    holder.timePager.setAdapter(timePagerAdapter);

    // Initialize dots for tab indicators
    final int numTimes = busTime.getTimes().size();
    final ImageView[] dots = new ImageView[numTimes];

    // Layout parameters
    LinearLayout.LayoutParams params = BusTimeHolder
        .getTimeDotsLayoutParams(
            (int) context.getResources().getDimension(R.dimen.card_dot_marginHorizontal));

    for (int i = 0; i < numTimes; i++) {
      // Create new dot, by default inactive
      dots[i] = new ImageView(context);
      dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.nonactive_dot));

      // Add current dot
      holder.timeDots.addView(dots[i], params);
    }

    // First dot will be active
    dots[0].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.active_dot));

    // Listener for when page is scrolled
    holder.timePager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

      }

      @Override
      public void onPageSelected(int position) {
        // Update dot at `position` to be the active dot
        for (int i = 0; i < numTimes; i++) {
          dots[i].setImageDrawable(ContextCompat.getDrawable(context, R.drawable.nonactive_dot));
        }

        dots[position].setImageDrawable(ContextCompat.getDrawable(context,
            R.drawable.active_dot));
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

  }

  /**
   * Specifies the content of each item in the schedule view.
   *
   * @param holder BusTimeHolder to be modified
   * @param position item in the list to be displayed
   */
  @Override
  public void onBindViewHolder(BusTimeHolder holder, int position) {
    BusTime curr = busTimes.get(position);

    setupRouteDirectionLayout(holder, curr);
    setupTimeLayout(holder, curr);
  }

  /**
   * Returns the number of bus times in the schedule view.
   *
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
    TextView name;
    ViewPager timePager;
    LinearLayout timeDots;

    BusTimeHolder(View itemView) {
      super(itemView);
      route = itemView.findViewById(R.id.route);
      direction = itemView.findViewById(R.id.direction);
      // Select name so that marquee works
      name = itemView.findViewById(R.id.name);
      name.setSelected(true);
      timePager = itemView.findViewById(R.id.timePager);
      timeDots = itemView.findViewById(R.id.timeDots);
    }

    /**
     * Get the time dots pager layout.
     *
     * @param dotMargin pixels between each dot
     * @return layout of the time dots
     */
    static LinearLayout.LayoutParams getTimeDotsLayoutParams(int dotMargin) {
      // Layout parameters
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.weight = 1.0f;
      params.gravity = Gravity.CENTER_HORIZONTAL;
      params.setMargins(dotMargin, 0, dotMargin, 0);
      return params;
    }

  }

}
