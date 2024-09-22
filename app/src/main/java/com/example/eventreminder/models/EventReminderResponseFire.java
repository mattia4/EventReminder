package com.example.eventreminder.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventReminderResponseFire {

    @SerializedName("eventId")
    @Expose
    private final String EventId;

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

    @SerializedName("isDeleted")
    @Expose
    private final boolean IsToNotify;

    @SerializedName("eventHour")
    @Expose
    private final String EventHour;

    @SerializedName("userUid")
    @Expose
    private final String UserUid;

    public EventReminderResponseFire(String eventId,
                                     String eventName,
                                     String eventDescription,
                                     String eventDate,
                                     boolean eventDeleted,
                                     boolean isToNotify,
                                     String eventHour,
                                     String userUid) {
        this.EventId = eventId;
        this.EventName = eventName;
        this.EventDescription = eventDescription;
        this.EventDate = eventDate;
        this.EventDeleted = eventDeleted;
        this.IsToNotify = isToNotify;
        this.EventHour = eventHour;
        this.UserUid = userUid;
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

    public boolean getIsToNotify() {
        return IsToNotify;
    }

    public String getEventId() {
        return EventId;
    }

    public String getEventHour() {
        return EventHour;
    }
    public String getUserUid() {
        return UserUid;
    }
}



