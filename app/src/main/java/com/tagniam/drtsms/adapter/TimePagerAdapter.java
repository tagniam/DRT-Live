package com.tagniam.drtsms.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.tagniam.drtsms.R;
import com.tagniam.drtsms.schedule.data.BusTime;
import java.util.Date;

/**
 * Created by jr on 08/12/17.
 */

public class TimePagerAdapter extends PagerAdapter {

  private Context context;
  private BusTime busTime;
  private Date now;

  TimePagerAdapter(Context context, BusTime busTime, Date now) {
    this.context = context;
    this.busTime = busTime;
    this.now = now;
  }

  @Override
  public int getCount() {
    return busTime.getTimes().size();
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override
  public Object instantiateItem(ViewGroup container, final int position) {
    LayoutInflater layoutInflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = layoutInflater.inflate(R.layout.time_tab, null);
    TextView time = view.findViewById(R.id.time);
    time.setText(BusTime.Helper.getRelativeTime(now, busTime.getTimes().get(position)));

    /* Might be useful later
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        if(position == 0){
          Toast.makeText(context, "Slide 1 Clicked", Toast.LENGTH_SHORT).show();
        } else if(position == 1){
          Toast.makeText(context, "Slide 2 Clicked", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(context, "Slide 3 Clicked", Toast.LENGTH_SHORT).show();
        }

      }
    });*/

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
}
