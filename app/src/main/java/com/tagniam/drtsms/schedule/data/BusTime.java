package com.tagniam.drtsms.schedule.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Created by jr on 06/12/17. */

/** Specifies the times and information for buses. */
public interface BusTime {
  String getRoute();

  String getDirection();

  List<Date> getTimes();

  class Helper {

    /**
     * Returns a string listing the next bus times relative to the current time.
     * Example:
     *
     * "5 min, 1 hr 5 min, 2 hr 56 min"
     *
     * @param now current Date
     * @param times list of times
     * @return String that lists the times relative to now
     */
    public static String getRelativeTimes(Date now, List<Date> times) {
      StringBuilder str = new StringBuilder();
      for (Date time : times) {
        str.append(getRelativeTime(now, time)).append(", ");
      }

      // Remove last ', '
      return str.substring(0, str.length() - 2);
    }

    /**
     * Returns a string listing the next bus times relative to the current time.
     * Example:
     *
     * "2 hr 5 min"
     *
     * @param now current Date
     * @param next the next Date
     * @return String that lists the time relative to now
     */
    public static String getRelativeTime(Date now, Date next) {
      StringBuilder str = new StringBuilder();

      Map<TimeUnit, Long> diff = computeDiff(now, next);
      int hours = diff.get(TimeUnit.HOURS).intValue();
      int minutes = diff.get(TimeUnit.MINUTES).intValue();

      if (hours == 0) {
        str.append(minutes).append(" min");
      } else if (hours != 0 && minutes == 0) {
        str.append(hours).append(" hr");
      } else {
        str.append(hours).append(" hr ").append(minutes).append(" min");
      }

      return str.toString();
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
      List<TimeUnit> units = new ArrayList<>(EnumSet.allOf(TimeUnit.class));
      Collections.reverse(units);
      Map<TimeUnit, Long> result = new LinkedHashMap<>();
      long milliesRest = diffInMillies;
      for (TimeUnit unit : units) {
        long diff = unit.convert(milliesRest, TimeUnit.MILLISECONDS);
        long diffInMilliesForUnit = unit.toMillis(diff);
        milliesRest = milliesRest - diffInMilliesForUnit;
        result.put(unit, diff);
      }
      return result;
    }
  }
}
