package com.example.eventreminder

import android.content.ContentValues.TAG
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.example.eventreminder.adapters.RvEventListAdapter
import com.example.eventreminder.databinding.EventListFragmentBinding
import com.example.eventreminder.models.EventReminderResponseFire
import com.example.eventreminder.services.NotificationServiceForeground
import com.example.eventreminder.utils.DateUtils
import com.example.eventreminder.ws.FireBaseEventDocumentUpdate
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class EventListFragment : Fragment(), RvEventListAdapter.OnItemClick {

    // TODO su home calendario per scegliere giorno eventi + lista eventi di quel giorno
    // TODO eventuale dettaglio + modifica riga evento
    private var _binding: EventListFragmentBinding? = null
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
        _binding = EventListFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        NotificationServiceForeground.startService(context!!, "Notification service is running...")
        // NotificationServiceForeground.stopService(context!!)
        // val saveRequest = PeriodicWorkRequestBuilder<UploadWorker>(1, TimeUnit.MINUTES).build()
        // WorkManager.getInstance(context!!).enqueue(saveRequest)

        val tvDay: TextView = view.findViewById(R.id.tv_day)
        val tvDayName: TextView = view.findViewById(R.id.tv_day_name)
        val rvTodoList: RecyclerView = view.findViewById(R.id.rv_todo_list)
        val llTodoList: SwipeRefreshLayout = view.findViewById(R.id.ll_todo_list)
        val pbLoadingBar: ProgressBar = view.findViewById(R.id.pb_loading_bar)

        tvDay.text = today?.format(DateTimeFormatter.ofPattern("d"))
        tvDayName.text = LocalDate.now().dayOfWeek.name

        llTodoList.setOnRefreshListener(OnRefreshListener {
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            );
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
    private fun runInBackground() {

        val ha = Handler(Looper.getMainLooper())
        ha.postDelayed(object : Runnable {
            override fun run() {
                Thread {
                    getDataForNotification()
                    ha.postDelayed(this, 1 * 60 * 1000)
                }.start()
            }
        }, 10000)
    }
*/
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
        }*/
    private fun firebaseUpdateEventDocument(eventId: String) {
        FireBaseEventDocumentUpdate().fireEventUpdate(context, eventId)
    }

    private fun firebaseMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.v("token", msg)
            // Se si vuole visualizzare il token per firebase cloud messaging
            // Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        })
    }

    private fun getEventAllFirestore(
        rvTodoList: RecyclerView,
        pbLoadingBar: ProgressBar?,
        llTodoList: SwipeRefreshLayout) {
        pbLoadingBar?.visibility = View.VISIBLE
        eventReminders.clear()
        adapter?.notifyDataSetChanged()

        fireDb.collection("Events").get().addOnCompleteListener {

            if (it.isSuccessful && null != it.result) {
                for (ev in it.result) {
                    var dateFormatted = ""
                    var eventYear = ""
                    var eventMonth = ""
                    var eventDay = ""

                    if (ev.data.getValue("eventDate") !is String) {
                        val timestamp: com.google.firebase.Timestamp =
                            ev.data.getValue("eventDate") as com.google.firebase.Timestamp
                        dateFormatted = DateUtils().getDateString(timestamp.seconds, "d/M/y")
                    } else {
                        dateFormatted = ev.data.getValue("eventDate") as String
                        val dateParts: List<String> = dateFormatted.split("/")
                        eventYear = dateParts[2]
                        eventMonth = dateParts[1]
                        eventDay = dateParts[0]
                        dateFormatted = String.format("%s/%s/%s", eventDay, eventMonth, eventYear)
                    }
                    val today = LocalDate.now()
                    val isDeleted = ev.data.getValue("eventDeleted") as Boolean
                    if (today.dayOfMonth.toString() == eventDay && !isDeleted) {

                        eventReminders.add(
                            EventReminderResponseFire(
                                ev.data.getValue("eventId") as String,
                                ev.data.getValue("eventName") as String,
                                ev.data.getValue("eventDescription") as String,
                                dateFormatted,
                                ev.data.getValue("eventDeleted") as Boolean,
                                ev.data.getValue("isToNotify") as Boolean,
                                ev.data.getValue("eventHour") as String
                            )
                        )

                    }
                    adapter = RvEventListAdapter(context, eventReminders, this)
                    rvTodoList.adapter = adapter
                    rvTodoList.layoutManager = LinearLayoutManager(context)
                }
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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