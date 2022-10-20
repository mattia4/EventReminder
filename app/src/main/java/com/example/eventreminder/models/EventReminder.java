package com.example.eventreminder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class EventReminder {
    @SerializedName("id")
    @Expose
    private final int Id;

    @SerializedName("eventName")
    @Expose
    private final String EventName;

    @SerializedName("eventDescription")
    @Expose
    private final String EventDescription;

    @SerializedName("eventDate")
    @Expose
    private final String EventDate;

    @SerializedName("isDeleted")
    @Expose
    private final boolean IsDeleted;

    public EventReminder(int id, String eventName, String eventDescription, String eventDate, boolean isDeleted ) {
        this.Id = id;
        this.EventName = eventName;
        this.EventDescription = eventDescription;
        this.EventDate = eventDate;
        this.IsDeleted = isDeleted;
    }

    public int getId() {
        return Id;
    }

    public String getEventName() {
        return EventName;
    }

    public String getEventDescription() {
        return EventDescription;
    }

    public boolean isDeleted() {
        return IsDeleted;
    }

    public String getEventDate() {
        return EventDate;
    }
}
