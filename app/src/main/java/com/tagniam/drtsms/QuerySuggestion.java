package com.tagniam.drtsms;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

@SuppressLint("ParcelCreator")
public class QuerySuggestion implements SearchSuggestion {
    String body;

    public QuerySuggestion(String body) {
        this.body = body;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
