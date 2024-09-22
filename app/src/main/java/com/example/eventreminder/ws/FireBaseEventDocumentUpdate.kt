package com.example.eventreminder.ws

import android.content.Context
import android.widget.Toast
import com.example.eventreminder.models.EventReminderResponseFire
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class FireBaseEventDocumentUpdate() {
    private var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun fireEventUpdate(context: Context?, eventId: String) {
        fireDb.collection("Events").get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val selectedEventId: String = eventId
                        if (document.data.getValue("eventId") == selectedEventId) {
                            fireDb.collection("Events").document(document.id)
                                .update("isToNotify", false)
                        }
                    }
                    Toast.makeText(context, "Notification updated successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "An error has occurred", Toast.LENGTH_LONG).show()
                }
            })
    }
    // TODO generalizzare update
    fun fireEventUpdateG(context: Context?, event: EventReminderResponseFire) {
        fireDb.collection("Events").get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val selectedEventId: String = event.eventId
                        if (document.data.getValue("eventId") == selectedEventId) {
                            fireDb.collection("Events").document(document.id).update("isToNotify", false)
                        }
                    }
                    Toast.makeText(context, "Update success", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "An error has occurred", Toast.LENGTH_LONG).show()
                }
            })
    }
}