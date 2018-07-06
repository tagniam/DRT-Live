package com.tagniam.drtsms.schedule.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by jr on 06/12/17.
 */
public class SmsBusTime implements BusTime, Serializable {

  private String route;
  private String direction;
  private List<Date> times = new ArrayList<>();

  /**
   * Generates the bus time through the given line in a DRT sms.
   *
   * @param now calendar instance of the current time
   * @param msg single line from DRT sms listing bus name, direction and times, in the following
   * form:
   * <p>Rt {route} {direction}: {time}| {time}| ...</p>
   */
  SmsBusTime(Calendar now, String msg) {
    String[] info = msg.split(": ");

    // Parse first part, contains route and direction
    parseRouteAndDirection(info[0]);
    // Parse second part, contains times
    parseTimes(now, info[1]);
  }

  /**
   * Extracts the name and direction of the bus time.
   *
   * @param info single line in form: Rt {route} {direction}
   */
  private void parseRouteAndDirection(String info) {
    String[] words = info.split(" ");
    // Parse route, second word
    route = words[1];

    // Rest of array is the direction
    StringBuilder directionParts = new StringBuilder();
    for (int i = 2; i < words.length; i++) {
      directionParts.append(words[i]);
      directionParts.append(" ");
    }

    // Remove last space
    direction = directionLongForm(
        directionParts.substring(0, directionParts.length() - 1));
  }

  /**
   * Convert shortform directions like "NB" to longform like "Northbound".
   *
   * @param direction direction string from SMS
   * @return longform direction
   */
  private String directionLongForm(String direction) {
    switch (direction) {
      case NORTHBOUND:
        return "Northbound";
      case WESTBOUND:
        return "Westbound";
      case EASTBOUND:
        return "Eastbound";
      case SOUTHBOUND:
        return "Southbound";
      case LOOP:
        return "Loop";
      case CLOCKWISE:
        return "Clockwise";
      case COUNTER_CLOCKWISE:
        return "Counter Clockwise";
    }
    return direction;
  }

  /**
   * Extracts the times of the bus time.
   *
   * @param info single line in the form: {time}| {time}| {time}| ... where each time is in the
   * form: hh:mm{a|p}
   */
  private void parseTimes(Calendar now, String info) {
    String[] timeParts = info.split("\\| ");

    // Generate date time objects for each time part
    for (String part : timeParts) {
      try {
        // Make it easier to parse am/pm
        String time = convertToSimpleTime(part);
        // Get day given the time
        Date date = getCurrentDateFromTime(now, time);

        times.add(date);
      } catch (ParseException e) {
        // Invalid time format, skip adding the time
      }
    }
  }

  /**
   * Returns the today's date if the given time is <= current clock time, or tomorrow's date if the
   * given time is > current clock time.
   *
   * @param time time string in format hh:mm a
   * @return date object of the upcoming bus time
   */
  private Date getCurrentDateFromTime(Calendar calNow, String time) throws ParseException {
    Calendar calTime = (Calendar) calNow.clone();

    // Set calTime without changing date
    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.CANADA);
    calTime.setTime(dateFormat.parse(time));
    calTime.set(calNow.get(Calendar.YEAR), calNow.get(Calendar.MONTH), calNow.get(Calendar.DATE));

    // Same day
    if (calTime.before(calNow)) {
      calTime.add(Calendar.DATE, 1);
    }

    return calTime.getTime();
  }

  /**
   * Converts the time string sent by DRT to a more easily parsable time string.
   *
   * @param time in form hh:mm{a|p}
   * @return in new form hh:mm {AM/PM}
   */
  private String convertToSimpleTime(String time) {
    String hhmm = time.substring(0, time.length() - 1);
    String amPmMarker = Character.toUpperCase(time.charAt(time.length() - 1)) + "M";
    return hhmm + " " + amPmMarker;
  }

  @Override
  public String getRoute() {
    return route;
  }

  @Override
  public String getDirection() {
    return direction;
  }

  @Override
  public List<Date> getTimes() {
    return times;
  }

  @Override
  public String toString() {
    return "Route: " + route + ", " +
        "Direction: " + direction + ", " +
        "Times: " + times;
  }
}
