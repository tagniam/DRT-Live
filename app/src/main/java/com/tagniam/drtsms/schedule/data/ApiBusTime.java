package com.tagniam.drtsms.schedule.data;

import com.tagniam.drtsms.schedule.fetcher.ApiScheduleFetcher.Departure;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApiBusTime implements BusTime {

  private String route;
  private String direction;
  private List<Date> times;

  /**
   * Generate the times of the current route
   *
   * @param departures, guaranteed to be of the same route and non-empty
   */
  ApiBusTime(List<Departure> departures) {
    this.times = new ArrayList<>();
    this.route = departures.get(0).route;
    // TODO put direction in here
    this.direction = "";

    for (Departure departure : departures) {
      try {
        this.times.add(strTimeToDate(departure.strTime));
      } catch (ParseException e) {
        // Invalid time format, skip time
      }
    }
  }

  /**
   * Converts the API response's strTime field to an actual date.
   *
   * @param strTime strTime response from api
   * @return strTime in date form
   */
  private Date strTimeToDate(String strTime) throws ParseException {
    Calendar calNow = Calendar.getInstance();
    Calendar calStrTime = Calendar.getInstance();
    calStrTime
        .set(calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH), calNow.get(Calendar.DATE));

    // Two forms: "x mins" or "xx:xx" (24hr clock)
    if (strTime.contains("min")) {
      calStrTime.add(Calendar.MINUTE, Integer.parseInt(strTime.split("\\s+")[0]));
    } else {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CANADA);
      calStrTime.setTime(simpleDateFormat.parse(strTime));
    }

    // Same day
    if (calStrTime.after(calNow)) {
      return calStrTime.getTime();
    }

    // Next day
    calStrTime.add(Calendar.DATE, 1);
    return calStrTime.getTime();
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
