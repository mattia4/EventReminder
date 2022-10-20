package com.example.eventreminder

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventreminder.adapters.RvEventListAdapter
import com.example.eventreminder.databinding.FragmentSecondBinding
import com.example.eventreminder.models.EventReminder
import com.example.eventreminder.models.EventReminderResponse
import com.example.eventreminder.models.EventReminderResponseFire
import com.example.eventreminder.utils.DateUtils
import com.example.eventreminder.ws.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var calendar: Calendar? = Calendar.getInstance()
    private var today: LocalDateTime? = LocalDateTime.now()
    private var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val tvEventName: TextView = view.findViewById(R.id.tv_event_title)
        val tvEventDescription: TextView = view.findViewById(R.id.tv_event_description)
        val cvDate: CalendarView = view.findViewById(R.id.cv_event_date)

        today?.let { calendar?.set(it.year, it.monthValue, it.dayOfMonth) }

        cvDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
            calendar?.set(year, month, dayOfMonth)
        }

        binding.btnSave.setOnClickListener {
            val er = EventReminderResponseFire(
                tvEventName.text.toString(),
                tvEventDescription.text.toString(),
                calendar?.time?.toInstant().toString(),
                false
            )

            if (null == er.eventName || er.eventName.isEmpty()) {
                tvEventName.error = "Event title is missing!"
                return@setOnClickListener
            }

            fireDb.collection("Events").get().addOnCompleteListener {

                if (it.isSuccessful && null != it.result) {
                    fireDb.collection("Events").add(er)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Event added successfully", Toast.LENGTH_SHORT)
                                .show()
                            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Event failed to add", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        }

        /* Decommentare se si vuole usare il local db
        binding.btnSave.setOnClickListener {

            val er = EventReminderResponse(
                tvEventName.text.toString(),
                tvEventDescription.text.toString(),
                calendar?.time?.toInstant().toString(),
                false
            )

            if (null == er.eventName || er.eventName.isEmpty()) {
                tvEventName.error = "Event title is missing!"
                return@setOnClickListener
            }

            val call: Call<EventReminderResponse> =
                RetrofitClient.getInstance().myApi.PostEventReminderAdd(er)

            call.enqueue(object : Callback<EventReminderResponse> {
                override fun onResponse(
                    call: Call<EventReminderResponse>,
                    response: Response<EventReminderResponse>
                ) {
                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                }

                override fun onFailure(call: Call<EventReminderResponse>, t: Throwable) {
                    t.message
                    Toast.makeText(
                        context,
                        "An error has occured",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}