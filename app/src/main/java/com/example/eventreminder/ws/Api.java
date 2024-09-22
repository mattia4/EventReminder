package com.example.eventreminder.ws;

import com.example.eventreminder.models.EventReminderResponse;
import com.example.eventreminder.models.EventReminderResponseFire;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POTS;

public interface Api {

    String BASE_URL = "http://10.0.2.2:5000/";

    @GET("EventReminder/All")
    Call<List<EventReminderResponseFire>> getEventAll();

    @POST("EventReminder/New")
    @Headers({"Content-Type: application/json"})
    Call<EventReminderResponse> PostEventReminderAdd(@Body EventReminderResponse er);

}
