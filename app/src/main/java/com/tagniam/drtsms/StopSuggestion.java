package com.tagniam.drtsms;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.tagniam.drtsms.database.stops.Stop;

@SuppressLint("ParcelCreator")
public class StopSuggestion implements SearchSuggestion {

    private Stop stop;

    public StopSuggestion(Stop stop) {
        this.stop = stop;
    }

    public String getStopCode() {
        return stop.stopCode;
    }

    public String getStopName() {
        return stop.stopName;
    }

    @Override
    public String getBody() {
        return getStopCode() + " - " + getStopName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
