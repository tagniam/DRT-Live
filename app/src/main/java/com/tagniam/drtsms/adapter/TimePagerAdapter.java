package com.tagniam.drtsms.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tagniam.drtsms.R;
import java.util.List;

/**
 * Created by jr on 08/12/17.
 */

public class TimePagerAdapter extends PagerAdapter {

  private Context context;
  private List<Pair<String, String>> times;
  private ScheduleAdapter scheduleAdapter;

  TimePagerAdapter(Context context, List<Pair<String, String>> times, ScheduleAdapter scheduleAdapter) {
    this.context = context;
    this.times = times;
    this.scheduleAdapter = scheduleAdapter;
  }

  @Override
  public int getCount() {
    return times.size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public Object instantiateItem(final ViewGroup container, final int position) {
    LayoutInflater layoutInflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = layoutInflater.inflate(R.layout.text_time, null);
    TextView time = view.findViewById(R.id.time);
    TextView timeSubtitle = view.findViewById(R.id.time_subtitle);

    time.setText(times.get(position).first);
    time.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        scheduleAdapter.onTimeClick(container, position);
      }
    });

    timeSubtitle.setText(times.get(position).second);


    ViewPager vp = (ViewPager) container;
    vp.addView(view, 0);
    return view;
  }


  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {

    ViewPager vp = (ViewPager) container;
    View view = (View) object;
    vp.removeView(view);

  }

  interface OnTimeClickListener {

    void onTimeClick(ViewGroup container, int position);
  }
}
