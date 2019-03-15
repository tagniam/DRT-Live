package com.tagniam.drtlive.schedule.data;

import com.tagniam.drtlive.schedule.fetcher.ApiScheduleFetcher.Trip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

class ApiBusTime implements BusTime {

  private String route;
  private String direction;
  private List<Date> times;


  /**
   * Generate the times of the current route
   *
   * @param now the current time
   * @param trips, guaranteed to be of the same route and non-empty
   */
  public ApiBusTime(Calendar now, List<Trip> trips) {
    this.times = new ArrayList<>();
    this.route = trips.get(0).routeId;
    // TODO put direction in here
    this.direction = "";

    for (Trip trip : trips) {
      this.times.add(epochTimeToDate(now, trip));
    }
  }

  /**
   * Converts the API response's time field to an actual date.
   * Can either return the real time if the trip has one, or the scheduled time.
   *
   * @param now the current time
   * @param trip
   * @return trip's time in date form
   */
  private Date epochTimeToDate(Calendar now, Trip trip) {
    Calendar cal = Calendar.getInstance();

    // Set time accordingly
    if (trip.hasRealTime) {
      cal.setTimeInMillis(trip.realTime);
    } else {
      cal.setTimeInMillis(trip.scheduledTime);
    }

    // Set current date
    cal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE));

    // Next day if actual time is before current time
    if (cal.before(now)) {
      cal.add(Calendar.DATE, 1);
    }
    return cal.getTime();
  }

  @Override
  public String getRoute() {
    return this.route;
  }

  @Override
  public String getDirection() {
    return this.direction;
  }

  @Override
  public List<Date> getTimes() {
    return this.times;
  }
}

