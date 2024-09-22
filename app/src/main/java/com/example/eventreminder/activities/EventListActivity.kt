package com.example.eventreminder.activities

import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.eventreminder.Constants.Companion.USER_UID
import com.example.eventreminder.R
import com.example.eventreminder.adapters.RvEventListAdapter
import com.example.eventreminder.adapters.RvEventListAdapter.OnItemClick
import com.example.eventreminder.databinding.EventListActivityBinding
import com.example.eventreminder.models.EventReminderResponseFire
import com.example.eventreminder.services.NotificationServiceForeground
import com.example.eventreminder.utils.DateUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList

private lateinit var binding: EventListActivityBinding

// TODO su home calendario per scegliere giorno eventi + lista eventi di quel giorno
// TODO eventuale dettaglio + modifica riga evento
// TODO avviso vocale di eventi

private var today: LocalDateTime? = LocalDateTime.now()
private var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()
private val eventReminders: ArrayList<EventReminderResponseFire> = ArrayList()
private var adapter: RvEventListAdapter? = null
private var currentUserUID: String = ""
private var tts: TextToSpeech? = null

class EventListActivity : AppCompatActivity(), OnItemClick {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_list_activity)

        binding = EventListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userUid: String? = intent.getStringExtra(USER_UID)
        if (userUid != null)
            currentUserUID = userUid

        NotificationServiceForeground.startService(this, "Notification service is running...")
        // NotificationServiceForeground.stopService(context!!)
        // val saveRequest = PeriodicWorkRequestBuilder<UploadWorker>(1, TimeUnit.MINUTES).build()
        // WorkManager.getInstance(context!!).enqueue(saveRequest)

        val tvDay: TextView = findViewById(R.id.tv_day)
        val tvDayName: TextView = findViewById(R.id.tv_day_name)
        val rvTodoList: RecyclerView = findViewById(R.id.rv_todo_list)
        val llTodoList: SwipeRefreshLayout = findViewById(R.id.ll_todo_list)
        val pbLoadingBar: ProgressBar = findViewById(R.id.pb_loading_bar)

        tvDay.text = today?.format(DateTimeFormatter.ofPattern("d"))
        tvDayName.text = LocalDate.now().dayOfWeek.name

        llTodoList.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            this.window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )

            getEventAllFirestore(rvTodoList, null, llTodoList)
        })

        binding.tvNew.setOnClickListener {
            val i = Intent(this, EventNewActivity::class.java)
            i.putExtra(USER_UID, currentUserUID)
            startActivity(i)
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
    // private fun firebaseUpdateEventDocument(eventId: String) {
    //     FireBaseEventDocumentUpdate().fireEventUpdate(context, eventId)
    // }

    private fun firebaseMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.exception)
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
        llTodoList: SwipeRefreshLayout
    ) {
        pbLoadingBar?.visibility = View.VISIBLE
        eventReminders.clear()
        adapter?.notifyDataSetChanged()

        fireDb.collection("Events").get().addOnCompleteListener {

            if (it.isSuccessful && null != it.result) {
                for (ev in it.result) {
                    var dateFormatted: String
                    var eventYear: String
                    var eventMonth: String
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

                    val userUid =  ev.data.getValue("userUid") as String
                    if (today.dayOfMonth.toString() == eventDay && !isDeleted &&
                        currentUserUID == userUid) {

                        eventReminders.add(
                            EventReminderResponseFire(
                                ev.data.getValue("eventId") as String,
                                ev.data.getValue("eventName") as String,
                                ev.data.getValue("eventDescription") as String,
                                dateFormatted,
                                ev.data.getValue("eventDeleted") as Boolean,
                                ev.data.getValue("isToNotify") as Boolean,
                                ev.data.getValue("eventHour") as String,
                                ev.data.getValue("userUid") as String)
                        )
                    }
                    adapter = RvEventListAdapter(this, eventReminders, this)
                    rvTodoList.adapter = adapter
                    rvTodoList.layoutManager = LinearLayoutManager(this)
                }
                this.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
            pbLoadingBar?.visibility = View.INVISIBLE
            llTodoList.isRefreshing = false
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Log out")
        builder.setMessage("Do you want to log out?")
        builder.setCancelable(false)
        builder.setPositiveButton("Yes",
            DialogInterface.OnClickListener { _: DialogInterface?, _: Int ->
                val i = Intent(this, RegisterLoginActivity::class.java)
                startActivity(i)
                FirebaseAuth.getInstance().signOut()
                intent.removeExtra(USER_UID)
                NotificationServiceForeground.stopService(this)
                finish()
            } as DialogInterface.OnClickListener)

        builder.setNegativeButton("No",
            DialogInterface.OnClickListener { dialog: DialogInterface, _: Int ->
                dialog.cancel()
            } as DialogInterface.OnClickListener)
        val alertDialog = builder.create()
        alertDialog.show()

    }
    override fun onItemClick(position: Int) {
        eventReminders.removeAt(position)
        adapter?.notifyDataSetChanged()
    }
}