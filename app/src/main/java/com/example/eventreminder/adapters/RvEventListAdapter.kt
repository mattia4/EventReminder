package com.example.eventreminder.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.eventreminder.R
import com.example.eventreminder.models.EventReminderResponseFire

class RvEventListAdapter (private val mEvent: List<EventReminderResponseFire>) :
    RecyclerView.Adapter<RvEventListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Define click listener for the ViewHolder's View.
        val tvEventName: TextView = view.findViewById(R.id.tv_event_name)
        val tvEventDate: TextView = view.findViewById(R.id.tv_event_date)
        val tvEventDescription: TextView = view.findViewById(R.id.tv_event_description)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_event_list, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val currEvent: EventReminderResponseFire = mEvent[position]
        // Decommentare sotto se si usa il db locale
        //val dateParts: List<String> = currEvent.eventDate.split("-")
        // val dateFormatted = String.format("%s/%s/%s", dateParts[2].substring(0, 2) ,dateParts[1] ,dateParts[0])

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.tvEventName.text = currEvent.eventName
        viewHolder.tvEventDate.text = currEvent.eventDate
        viewHolder.tvEventDescription.text = currEvent.eventDescription
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = mEvent.size

}