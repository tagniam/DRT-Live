package com.tagniam.drtlive.schedule.data;

import java.util.List;

/**
 * Created by jr on 06/12/17.
 */
public interface Schedule {

  String getStopNumber();

  List<BusTime> getBusTimes();
}
