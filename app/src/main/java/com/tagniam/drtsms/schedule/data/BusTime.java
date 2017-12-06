package com.tagniam.drtsms.schedule.data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by jr on 06/12/17.
 */

/**
 * Specifies the times and information for buses.
 */
public interface BusTime {
    String getRoute();
    String getDirection();
    List<Date> getTimes();
}
