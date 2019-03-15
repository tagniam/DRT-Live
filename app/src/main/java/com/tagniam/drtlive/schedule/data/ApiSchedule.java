package com.tagniam.drtlive.schedule.data;

import com.tagniam.drtlive.schedule.exceptions.NullResponseException;
import com.tagniam.drtlive.schedule.exceptions.StopTimesNotAvailableException;
import com.tagniam.drtlive.schedule.fetcher.ApiScheduleFetcher.StopTimes;
import com.tagniam.drtlive.schedule.fetcher.ApiScheduleFetcher.Trip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ApiSchedule implements Schedule {

  private String stopNumber;
  private List<BusTime> busTimes;

  /**
   * Construct a Schedule object from the API response.
   *
   * @param now current time
   * @param stopNumber stop #
   * @param stopTimes API response
   */
  public ApiSchedule(Calendar now, String stopNumber, StopTimes stopTimes)
          throws NullResponseException {
    this.stopNumber = stopNumber;

    // Split trips by routeId
    HashMap<String, List<Trip>> tripsByRoute = new LinkedHashMap<>();
    for (Trip trip : stopTimes.trips) {
      if (!tripsByRoute.containsKey(trip.routeId)) {
        tripsByRoute.put(trip.routeId, new ArrayList<Trip>());
      }
      tripsByRoute.get(trip.routeId).add(trip);
    }

    this.busTimes = new ArrayList<>();
    for (List<Trip> trips : tripsByRoute.values()) {
      this.busTimes.add(new ApiBusTime(now, trips));
    }


  }

  @Override
  public String getStopNumber() {
    return this.stopNumber;
  }

  @Override
  public List<BusTime> getBusTimes() {
    return this.busTimes;
  }
}

