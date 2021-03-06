package com.tagniam.drtlive.schedule.exceptions;

/** Exception when the DRT can't find the stop number the user has entered. */
public class StopNotFoundException extends Exception {

  public StopNotFoundException() {
    super("Stop not found.");
  }
}
