package com.example.eventreminder.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // val service = NotificationService(context)
        // service.showNotification("test", "desc")

        val action = intent!!.getStringExtra("shutDownNotify")
        if (action == "shutDownNotify") {
            updateNotifyStatus(context)
        }
        //This is used to close the notification tray
        //This is used to close the notification tray
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context.sendBroadcast(it)
    }

    // TODO passare id notifica e quando si clicca sopra terminare l'avviso
    private fun updateNotifyStatus(context: Context) {
        var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()

        fireDb.collection("Events").get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val selectedNotifyId: String = ""
                        if (document.data.getValue("eventId") == selectedNotifyId) {
                            fireDb.collection("Events").document(document.id).update("isToNotify", false)
                        }
                    }
                    Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "An error has occured", Toast.LENGTH_LONG).show()
                }
            })
    }
}