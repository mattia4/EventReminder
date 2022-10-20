package com.example.eventreminder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventReminderResponseFire {

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
    private final boolean EventDeleted;

    public EventReminderResponseFire(String eventName, String eventDescription, String eventDate, boolean eventDeleted) {
        this.EventName = eventName;
        this.EventDescription = eventDescription;
        this.EventDate = eventDate;
        this.EventDeleted = eventDeleted;
    }


    public String getEventName() {
        return EventName;
    }

    public String getEventDescription() {
        return EventDescription;
    }


    public String getEventDate() {
        return EventDate;
    }

    public boolean getEventDeleted() {
        return EventDeleted;
    }
}



