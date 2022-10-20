package com.example.eventreminder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.eventreminder.adapters.RvEventListAdapter
import com.example.eventreminder.databinding.FragmentFirstBinding
import com.example.eventreminder.models.EventReminder
import com.example.eventreminder.models.EventReminderResponse
import com.example.eventreminder.models.EventReminderResponseFire
import com.example.eventreminder.utils.DateUtils
import com.example.eventreminder.ws.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private var today: LocalDateTime? = LocalDateTime.now()
    private var formattedDate: String? = ""
    private var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val tvToday: TextView = view.findViewById(R.id.tv_today)
        val tvDay: TextView = view.findViewById(R.id.tv_day)
        val tvDayName: TextView = view.findViewById(R.id.tv_day_name)
        val rvTodoList: RecyclerView = view.findViewById(R.id.rv_todo_list)
        val pbLoadingBar: ProgressBar = view.findViewById(R.id.pb_loading_bar)
        // formattedDate = today?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        // tvToday.text = formattedDate

        tvDay.text = today?.format(DateTimeFormatter.ofPattern("d"))
        tvDayName.text = LocalDate.now().dayOfWeek.name

        binding.tvNew.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        // decommentare sotto se si vuol usare il db locale
        // getEventAll(rvTodoList, pbLoadingBar)
        getEventAllFirestore(rvTodoList, pbLoadingBar)
    }

    private fun getEventAll(rvTodoList: RecyclerView, pbLoadingBar: ProgressBar) {
        pbLoadingBar.visibility = View.VISIBLE

        val call: Call<List<EventReminderResponseFire>> = RetrofitClient.getInstance().myApi.eventAll
        call.enqueue(object : Callback<List<EventReminderResponseFire>> {
            override fun onResponse(
                call: Call<List<EventReminderResponseFire>>,
                response: Response<List<EventReminderResponseFire>>
            ) {
                val myEventList: List<EventReminderResponseFire>? = response.body()
                val adapter = myEventList?.let { RvEventListAdapter(it) }
                rvTodoList.adapter = adapter
                rvTodoList.layoutManager = LinearLayoutManager(context)
                pbLoadingBar.visibility = View.INVISIBLE
            }

            override fun onFailure(call: Call<List<EventReminderResponseFire>>, t: Throwable) {
               t.message
                Toast.makeText(
                    context,
                    "An error has occured",
                    Toast.LENGTH_LONG
                ).show()
                pbLoadingBar.visibility = View.INVISIBLE
            }
        })

    }

    private fun getEventAllFirestore(rvTodoList: RecyclerView, pbLoadingBar: ProgressBar) {
        pbLoadingBar.visibility = View.VISIBLE
        fireDb.collection("Events").get().addOnCompleteListener {
            val eventReminders: ArrayList<EventReminderResponseFire> = ArrayList()

            if (it.isSuccessful && null != it.result) {
                for (ev in it.result) {
                    pbLoadingBar.visibility = View.INVISIBLE
                    var dataFormatted = ""

                    if (ev.data.getValue("eventDate") !is String) {
                        val timestamp: com.google.firebase.Timestamp = ev.data.getValue("eventDate") as com.google.firebase.Timestamp
                        dataFormatted = DateUtils().getDateString(timestamp.seconds, "d/M/y")
                    } else {
                        dataFormatted = ev.data.getValue("eventDate") as String
                        val dateParts: List<String> = dataFormatted.split("-")
                        dataFormatted = String.format("%s/%s/%s", dateParts[2].substring(0, 2) ,dateParts[1] ,dateParts[0])
                    }

                    eventReminders.add(EventReminderResponseFire(
                        ev.data.getValue("eventName") as String,
                        ev.data.getValue("eventDescription") as String,
                        dataFormatted,
                        ev.data.getValue("eventDeleted") as Boolean
                    ))

                    val adapter = RvEventListAdapter(eventReminders)
                    rvTodoList.adapter = adapter
                    rvTodoList.layoutManager = LinearLayoutManager(context)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}