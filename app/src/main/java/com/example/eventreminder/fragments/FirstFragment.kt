package com.example.eventreminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.example.eventreminder.adapters.RvEventListAdapter
import com.example.eventreminder.databinding.FragmentFirstBinding
import com.example.eventreminder.models.EventReminderResponseFire
import com.example.eventreminder.utils.DateUtils
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), RvEventListAdapter.OnItemClick {

    private var _binding: FragmentFirstBinding? = null
    private var today: LocalDateTime? = LocalDateTime.now()
    private var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val eventReminders: ArrayList<EventReminderResponseFire> = ArrayList()
    private var adapter: RvEventListAdapter? = null

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

        val tvDay: TextView = view.findViewById(R.id.tv_day)
        val tvDayName: TextView = view.findViewById(R.id.tv_day_name)
        val rvTodoList: RecyclerView = view.findViewById(R.id.rv_todo_list)
        val llTodoList: SwipeRefreshLayout = view.findViewById(R.id.ll_todo_list)
        val pbLoadingBar: ProgressBar = view.findViewById(R.id.pb_loading_bar)

        tvDay.text = today?.format(DateTimeFormatter.ofPattern("d"))
        tvDayName.text = LocalDate.now().dayOfWeek.name

        llTodoList.setOnRefreshListener(OnRefreshListener {
            getEventAllFirestore(rvTodoList, null, llTodoList)
        })

        binding.tvNew.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        // decommentare sotto se si vuol usare il db locale
        // getEventAll(rvTodoList, pbLoadingBar)
        getEventAllFirestore(rvTodoList, pbLoadingBar, llTodoList)
    }

    /*
        private  fun getEventAll(rvTodoList: RecyclerView, pbLoadingBar: ProgressBar) {
            pbLoadingBar.visibility = View.VISIBLE

            val call: Call<List<EventReminderResponseFire>> = RetrofitClient.getInstance().myApi.eventAll
            call.enqueue(object : Callback<List<EventReminderResponseFire>> {
                override fun onResponse(
                    call: Call<List<EventReminderResponseFire>>,
                    response: Response<List<EventReminderResponseFire>>
                ) {
                    val myEventList: List<EventReminderResponseFire>? = response.body()
                    val adapter = myEventList?.let { RvEventListAdapter(context, it, this) }
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
    */
    private fun getEventAllFirestore(rvTodoList: RecyclerView, pbLoadingBar: ProgressBar?, llTodoList: SwipeRefreshLayout) {
        pbLoadingBar?.visibility = View.VISIBLE
        eventReminders.clear()

        fireDb.collection("Events").get().addOnCompleteListener {

            if (it.isSuccessful && null != it.result) {
                for (ev in it.result) {
                    var dateFormatted = ""

                    if (ev.data.getValue("eventDate") !is String) {
                        val timestamp: com.google.firebase.Timestamp =
                            ev.data.getValue("eventDate") as com.google.firebase.Timestamp
                        dateFormatted = DateUtils().getDateString(timestamp.seconds, "d/M/y")
                    } else {
                        dateFormatted = ev.data.getValue("eventDate") as String
                        val dateParts: List<String> = dateFormatted.split("-")
                        dateFormatted = String.format(
                            "%s/%s/%s",
                            dateParts[2].substring(0, 2),
                            dateParts[1],
                            dateParts[0]
                        )
                    }
                    val today = LocalDate.now()
                    val isDeleted = ev.data.getValue("eventDeleted") as Boolean
                    if (today.dayOfMonth.toString() == dateFormatted.substring(0, 2) && !isDeleted) {

                        eventReminders.add(
                            EventReminderResponseFire(
                                ev.data.getValue("eventId") as String,
                                ev.data.getValue("eventName") as String,
                                ev.data.getValue("eventDescription") as String,
                                dateFormatted,
                                ev.data.getValue("eventDeleted") as Boolean
                            )
                        )

                    }
                    adapter = RvEventListAdapter(context, eventReminders, this)
                    rvTodoList.adapter = adapter
                    rvTodoList.layoutManager = LinearLayoutManager(context)
                }
            }
            pbLoadingBar?.visibility = View.INVISIBLE
            llTodoList.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(position: Int) {
        eventReminders.removeAt(position)
        adapter?.notifyDataSetChanged()
    }
}