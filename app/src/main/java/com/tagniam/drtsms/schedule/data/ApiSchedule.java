package com.tagniam.drtsms.schedule.data;

import com.tagniam.drtsms.schedule.exceptions.StopTimesNotAvailableException;
import com.tagniam.drtsms.schedule.fetcher.ApiScheduleFetcher.Departure;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ApiSchedule implements Schedule, Serializable {

  private String stopNumber;
  private List<BusTime> busTimes;

  /**
   * Construct a Schedule object from the API response.
   *
   * @param stopNumber stop #
   * @param departures departures at the stop
   */
  public ApiSchedule(Calendar now, String stopNumber, List<Departure> departures)
      throws StopTimesNotAvailableException {
    if (departures.isEmpty()) {
      throw new StopTimesNotAvailableException();
    }

    this.stopNumber = stopNumber;

    // Split departures by route
    HashMap<String, List<Departure>> departuresByRoutes = new LinkedHashMap<>();
    for (Departure departure : departures) {
      if (!departuresByRoutes.containsKey(departure.route)) {
        departuresByRoutes.put(departure.route, new ArrayList<Departure>());
      }
      departuresByRoutes.get(departure.route).add(departure);
    }

    // Initialize bustimes by departure
    this.busTimes = new ArrayList<>();
    for (List<Departure> routeDeparture : departuresByRoutes.values()) {
      this.busTimes.add(new ApiBusTime(now, routeDeparture));
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
