package com.tagniam.drtsms.schedule.data;

import com.tagniam.drtsms.schedule.exceptions.StopNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jr on 06/12/17.
 */

public class SmsSchedule implements Schedule {
    private List<BusTime> busTimes = new ArrayList<>();
    private String stopNumber;
    private static final String STOP_NOT_FOUND_REGEX = "Stop Number .* not found\\.";

    /**
     * Generates all bus times for the given DRT sms message.
     * See the MockScheduleFetcher for an example of such an sms.
     * @param msg msg from DRT
     */
    public SmsSchedule(String msg) throws StopNotFoundException {
        // DRT can't find that stop number
        if (msg.matches(STOP_NOT_FOUND_REGEX)) {
            throw new StopNotFoundException();
        }

        String[] info = msg.split("\r\n");
        // Extract bus number, second string
        stopNumber = info[0].split("[ :]")[1];

        // Last line will be "std rates" message, just skip that
        for (int i = 1; i < info.length-1; i++) {
            busTimes.add(new SmsBusTime(info[i]));
        }
    }


    @Override
    public String getStopNumber() {
        return stopNumber;
    }

    @Override
    public List<BusTime> getBusTimes() {
        return busTimes;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("Stop number: ").append(stopNumber).append(" ");
        ret.append("Bus times:\n");
        for (BusTime busTime : busTimes) {
            ret.append(busTime).append("\n");
        }
        return ret.toString();
    }
}
