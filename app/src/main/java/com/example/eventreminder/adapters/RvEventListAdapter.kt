package com.example.eventreminder.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.eventreminder.R
import com.example.eventreminder.models.EventReminderResponseFire
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class RvEventListAdapter(
    private val context: Context?,
    private val mEvent: List<EventReminderResponseFire>,
    private val mOnItemClick: OnItemClick
) :
    RecyclerView.Adapter<RvEventListAdapter.ViewHolder>() {

    interface OnItemClick {
        fun onItemClick(position: Int)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Define click listener for the ViewHolder's View.
        val tvEventName: TextView = view.findViewById(R.id.tv_event_name)
        val tvEventDate: TextView = view.findViewById(R.id.tv_event_date)
        val tvEventDescription: TextView = view.findViewById(R.id.tv_event_description)
        val tvNotifyMe: ImageView = view.findViewById(R.id.iv_notfy_me)
        val ivDelete: ImageView = view.findViewById(R.id.ic_delete)
        val ivEventHour: ImageView = view.findViewById(R.id.iv_event_hour)
        val tvEventHour: TextView = view.findViewById(R.id.tv_event_hour)
        var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_event_list_row, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currEvent: EventReminderResponseFire = mEvent[position]
        // Decommentare sotto se si usa il db locale
        // val dateParts: List<String> = currEvent.eventDate.split("-")
        // val dateFormatted = String.format("%s/%s/%s", dateParts[2].substring(0, 2) ,dateParts[1] ,dateParts[0])

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.tvEventName.text = currEvent.eventName
        viewHolder.tvEventDate.text = currEvent.eventDate

        if(currEvent.isToNotify) viewHolder.tvNotifyMe.visibility =  View.VISIBLE
        else viewHolder.tvNotifyMe.visibility =  View.GONE

        if (currEvent.eventDescription.isEmpty()) {
            viewHolder.tvEventDescription.visibility = View.GONE
        } else {
            viewHolder.tvEventDescription.visibility = View.VISIBLE
            viewHolder.tvEventDescription.text = currEvent.eventDescription
        }

        if (currEvent.eventHour.isEmpty()) {
            viewHolder.tvEventHour.visibility = View.GONE
            viewHolder.ivEventHour.visibility = View.GONE
        } else {
            viewHolder.tvEventHour.visibility = View.VISIBLE
            viewHolder.ivEventHour.visibility = View.VISIBLE
            viewHolder.tvEventHour.text = currEvent.eventHour
        }
        viewHolder.ivDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setMessage(R.string.dialog_message)
                .setTitle(R.string.dialog_title)
                .setCancelable(false)
                .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->

                    viewHolder.fireDb.collection("Events").get()
                        .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                            if (task.isSuccessful) {
                                for (document in task.result) {
                                    val selectedEventId: String = currEvent.eventId
                                    if (document.data.getValue("eventId") == selectedEventId) {
                                        viewHolder.fireDb.collection("Events").document(document.id)
                                            .update("eventDeleted", true)
                                        mOnItemClick.onItemClick(position)
                                    }
                                }

                                Toast.makeText(context, "Event deleted successfully", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "An error has occurred", Toast.LENGTH_LONG).show()
                            }
                        })
                })
                ?.setNegativeButton("No", DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                })?.show()
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mEvent.size

}