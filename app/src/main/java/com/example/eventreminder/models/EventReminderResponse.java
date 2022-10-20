package com.example.eventreminder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventReminderResponse {

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

    public EventReminderResponse( String eventName, String eventDescription, String eventDate, boolean isDeleted) {
        this.EventName = eventName;
        this.EventDescription = eventDescription;
        this.EventDate = eventDate;
        this.IsDeleted = isDeleted;
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


