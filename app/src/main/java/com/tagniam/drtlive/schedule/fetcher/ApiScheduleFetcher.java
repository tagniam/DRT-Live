package com.tagniam.drtlive.schedule.fetcher;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tagniam.drtlive.schedule.data.ApiSchedule;
import com.tagniam.drtlive.schedule.data.Schedule;
import com.tagniam.drtlive.schedule.exceptions.NullResponseException;
import com.tagniam.drtlive.schedule.exceptions.StopTimesNotAvailableException;
import io.reactivex.ObservableEmitter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ApiScheduleFetcher extends ScheduleFetcher {

  private final static String DRT_SERVICES_URL = "https://www.durhamregiontransit.com/Modules/NextRide/services/";
  private String stopNumber;

  private Retrofit retrofit;
  private Calendar now;

  ApiScheduleFetcher(String stopNumber) {
    this.stopNumber = stopNumber;
    this.retrofit = new Retrofit.Builder()
            .baseUrl(DRT_SERVICES_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    this.now = Calendar.getInstance();
  }

  @Override
  public void onPause() {
  }

  @Override
  public void onResume() {

  }

  @Override
  public void subscribe(ObservableEmitter<Intent> emitter) throws IOException {
    DrtServicesApi api = retrofit.create(DrtServicesApi.class);

    // Get stop times from API
    StopTimes stopTimes = api.getStopTimes(stopNumber).execute().body();

    if (stopTimes == null) {
      emitter.onError(new NullResponseException());
    } else {
      try {
        Schedule schedule = new ApiSchedule(now, stopNumber, stopTimes);
        Intent result = new Intent(ScheduleFetcher.Intents.SUCCESS_ACTION);
        result.putExtra(ScheduleFetcher.Intents.RESULT_EXTRA, (Serializable) schedule);
        emitter.onNext(result);
      } catch (NullResponseException e) {
        emitter.onError(e);
      }
    }
    emitter.onComplete();
  }

  interface DrtServicesApi {

    @GET("GetStopTimes.ashx")
    Call<StopTimes> getStopTimes(@Query("stopId") String stopId);

  }

  public class StopTimes {
    @SerializedName("Name")
    @Expose public String name;
    @SerializedName("StopId")
    @Expose public String stopId;
    @SerializedName("Trips")
    @Expose public Trip[] trips;
  }

  public class Trip {
    @SerializedName("HasRealTime")
    @Expose public boolean hasRealTime;
    @SerializedName("Headsign")
    @Expose public String headSign;
    @SerializedName("RealTime")
    @Expose public long realTime;
    @SerializedName("RealTimeFormatted")
    @Expose public String realTimeFormatted;
    @SerializedName("RouteId")
    @Expose public String routeId;
    @SerializedName("ScheduledTime")
    @Expose public long scheduledTime;
    @SerializedName("TripId")
    @Expose public String tripId;
  }
}
