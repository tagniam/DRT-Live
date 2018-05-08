package com.tagniam.drtsms.schedule.fetcher;

import android.content.Intent;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tagniam.drtsms.schedule.data.ApiSchedule;
import com.tagniam.drtsms.schedule.data.Schedule;
import io.reactivex.ObservableEmitter;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class ApiScheduleFetcher extends ScheduleFetcher {

  private final static String BASE_URL = "https://drtonline.durhamregiontransit.com/webapi/";
  private String stopNumber;
  private DrtOnlineApi api;

  public ApiScheduleFetcher(String stopNumber) {
    this.stopNumber = stopNumber;
  }

  @Override
  public void onPause() {

  }

  @Override
  public void onResume() {

  }

  @Override
  public void subscribe(ObservableEmitter<Intent> emitter) throws IOException {
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build();
    api = retrofit.create(DrtOnlineApi.class);

    // Get schedule from api
    ApiResponse res = api
        .listDepartures(stopNumber)
        .execute()
        .body();

    // Generate schedule from departures
    if (res == null) {
      // TODO fix exception
      emitter.onError(new Exception("API returned null"));
    } else {
      Schedule schedule = new ApiSchedule(stopNumber, res.departures);
      Intent result = new Intent(ScheduleFetcher.Intents.SUCCESS_ACTION);
      result.putExtra(ScheduleFetcher.Intents.RESULT_EXTRA, (Serializable) schedule);
      emitter.onNext(result);
    }
    emitter.onComplete();
  }

  interface DrtOnlineApi {

    @GET("/departures/bystop/{stop}")
    Call<ApiResponse> listDepartures(@Path("stop") String stop);
  }

  public static class Departure {

    @SerializedName("route")
    @Expose
    public String route;
    @SerializedName("destination")
    @Expose
    public String destination;
    @SerializedName("strTime")
    @Expose
    public String strTime;
    @SerializedName("vehicleType")
    @Expose
    public String vehicleType;
    @SerializedName("lowfloor")
    @Expose
    public boolean lowfloor;
    @SerializedName("realtime")
    @Expose
    public boolean realtime;
    @SerializedName("traction")
    @Expose
    public int traction;

  }

  public static class ApiResponse {

    @SerializedName("timestamp")
    @Expose
    public String timestamp;
    @SerializedName("stopName")
    @Expose
    public String stopName;
    @SerializedName("departures")
    @Expose
    public List<Departure> departures;

  }
}
