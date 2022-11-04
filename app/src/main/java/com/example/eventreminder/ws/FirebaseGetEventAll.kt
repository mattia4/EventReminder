package com.example.eventreminder.ws

import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import com.example.eventreminder.services.NotificationService
import com.example.eventreminder.utils.DateUtils
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.util.*

class FirebaseGetEventAll {
    private var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getDataForNotification(context: Context?) {
        fireDb.collection("Events").get().addOnCompleteListener {

            if (it.isSuccessful && null != it.result) {
                var idNotification = 0
                for (ev in it.result) {
                    var dateFormatted = ""
                    var eventYear = ""
                    var eventMonth = ""
                    var eventDay = ""

                    if (ev.data.getValue("eventDate") !is String) {
                        val timestamp: com.google.firebase.Timestamp = ev.data.getValue("eventDate") as com.google.firebase.Timestamp
                        dateFormatted = DateUtils().getDateString(timestamp.seconds, "d/M/y")
                    } else {
                        dateFormatted = ev.data.getValue("eventDate") as String
                        val dateParts: List<String> = dateFormatted.split("/")
                        eventYear= dateParts[2]
                        eventMonth = dateParts[1]
                        eventDay = dateParts[0]
                        dateFormatted = String.format("%s/%s/%s", eventDay, eventMonth, eventYear)
                    }
                    val eventHour: String = ev.data.getValue("eventHour") as String
                    var hourParts: List<String>
                    var hour = 0
                    var minute = 0
                    if(eventHour.isNotEmpty()) {
                        hourParts = eventHour.split(":")
                        hour = hourParts[0].toInt()
                        minute = hourParts[1].toInt()
                    }

                    val calendar: Calendar = Calendar.getInstance()
                    // val currDay: String = calendar.get(Calendar.DAY_OF_MONTH).toString()
                    // val currMonth: String = calendar.get(Calendar.MONTH).toString()
                    // val currYear: String = calendar.get(Calendar.YEAR).toString()
                    val currHour: Int = calendar.get(Calendar.HOUR_OF_DAY)
                    val currMinutes: Int = calendar.get(Calendar.MINUTE)

                    val today = LocalDate.now()
                    val isDeleted = ev.data.getValue("eventDeleted") as Boolean
                    if (today.dayOfMonth.toString() == eventDay && !isDeleted) {

                        val title: String = ev.data.getValue("eventName") as String
                        val description: String = ev.data.getValue("eventDescription") as String
                        val notifyMe: Boolean = ev.data.getValue("isToNotify") as Boolean
                        val eventId: String = ev.data.getValue("eventId") as String

                        if(notifyMe && currHour == hour && currMinutes >= minute-5 && currMinutes <= minute) {
                            NotificationService(context).showMultipleNotificationBigText(title, description, idNotification, "test")
                            FireBaseEventDocumentUpdate().fireEventUpdate(context, eventId)
                        }
                        idNotification++
                        SystemClock.sleep(1000)
                    }
                }
            } else {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT)
            }
        }
    }
}