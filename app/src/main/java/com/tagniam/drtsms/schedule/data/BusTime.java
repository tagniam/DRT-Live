package com.tagniam.drtsms.schedule.data;

import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Specifies the times and information for buses. */
public interface BusTime {

  // Directions
  String NORTHBOUND = "NB";
  String EASTBOUND = "EB";
  String SOUTHBOUND = "SB";
  String WESTBOUND = "WB";
  String COUNTER_CLOCKWISE = "Counter Clockwise";
  String CLOCKWISE = "Clockwise";
  String LOOP = "LP";

  String getRoute();

  String getDirection();

  List<Date> getTimes();

  class Helper {

    /**
     * Returns a string listing the next bus times relative to the current time.
     *
     * @param now current Date
     * @param times list of times
     * @return List<String> that lists the times relative to now
     */
    public static List<String> getRelativeTimes(Date now, List<Date> times) {
      List<String> ret = new ArrayList<>();
      for (Date time : times) {
        ret.add(getRelativeTime(now, time));
      }

      return ret;
    }

    public static List<Pair<String, String>> getStringTimes(Date now, List<Date> times) {
      List<Pair<String, String>> ret = new ArrayList<>();
      for (Date time : times) {
        // Add absolute time if > 1 hr away
        Map<TimeUnit, Long> diff = computeDiff(now, time);
        if (diff.get(TimeUnit.DAYS) == 0 && diff.get(TimeUnit.HOURS) == 0) {
          ret.add(getRelativeTimeNew(now, time));
        } else {
          ret.add(getAbsoluteTimeNew(time));
        }
      }
      return ret;
    }

    /**
     * Returns a list listing the next bus times.
     *
     * @param times list of times
     * @return List<String> that lists the times
     */
    public static List<String> getAbsoluteTimes(List<Date> times) {
      List<String> ret = new ArrayList<>();
      for (Date time : times) {
        ret.add(getAbsoluteTime(time));
      }

      return ret;
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
      } else if (minutes == 0) {
        str.append(hours).append(" hr");
      } else {
        str.append(hours).append(" hr ").append(minutes).append(" min");
      }

      return str.toString();
    }

    /**
     * Returns a string that gets the absolute time of a Date, in the form HH:mm (a/p).
     * Example:
     * 9:23a
     * 2:04p
     *
     * @return absolute time in above format
     */
    public static String getAbsoluteTime(Date time) {
      SimpleDateFormat df = new SimpleDateFormat("hh:mm", Locale.CANADA);
      Calendar cal = Calendar.getInstance();

      StringBuilder ret = new StringBuilder().append(df.format(time));
      cal.setTime(time);
      if (cal.get(Calendar.AM_PM) == Calendar.AM) {
        ret.append("a");
      } else {
        ret.append("p");
      }

      return ret.toString();
    }

    public static Pair<String, String> getAbsoluteTimeNew(Date time) {
      SimpleDateFormat df = new SimpleDateFormat("hh:mm", Locale.CANADA);
      Calendar cal = Calendar.getInstance();
      cal.setTime(time);
      return new Pair<>(
              df.format(time),
              cal.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM"
      );
    }

    public static Pair<String, String> getRelativeTimeNew(Date now, Date next) {
      Map<TimeUnit, Long> diff = computeDiff(now, next);
      return new Pair<>(
              diff.get(TimeUnit.MINUTES).toString(),
              "min"
      );
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
