package com.tagniam.drtlive.schedule.exceptions;

/**
 * Created by jr on 09/12/17.
 */

public class StopTimesNotAvailableException extends Exception {

  public StopTimesNotAvailableException() {
    super("No stop times are currently available.");
  }
}
