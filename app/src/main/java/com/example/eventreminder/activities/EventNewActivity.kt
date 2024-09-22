package com.example.eventreminder.activities

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.eventreminder.Constants.Companion.USER_UID
import com.example.eventreminder.R
import com.example.eventreminder.databinding.EventNewActivityBinding
import com.example.eventreminder.models.EventReminderResponseFire
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EventNewActivity  : AppCompatActivity() {
    private lateinit var binding: EventNewActivityBinding

    // This property is only valid between onCreateView and
    // onDestroyView.
    private var calendar: Calendar = GregorianCalendar()
    private var todayCal: Calendar = Calendar.getInstance()
    private var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var eventHour = ""
    private var hour = 0
    private var minute = 0

    private var userUID: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = EventNewActivityBinding.inflate(layoutInflater)
        setContentView(R.layout.event_new_activity)


        val etEventName: EditText = findViewById(R.id.tv_event_title)
        val tvEventDescription: TextView = findViewById(R.id.tv_event_description)
        val cvDate: DatePicker = findViewById(R.id.cv_event_date)
        val cvCheckbox: CheckBox = findViewById(R.id.cb_checkbox)
        val tvCheckbox: LinearLayout = findViewById(R.id.ll_checkbox)
        val cvSetTime: LinearLayout = findViewById(R.id.cv_set_time)
        val tvTimeTitle: TextView = findViewById(R.id.tv_time_title)
        // val df = SimpleDateFormat("dd-MM-yyyy")
        // val formattedDate: String = df.format(todayCal.time)

        val userUid: String? = intent.getStringExtra(USER_UID)
        if (userUid != null) {
            userUID = userUid
        }
        cvDate.minDate = calendar.timeInMillis
        calendar.set(todayCal.get(Calendar.YEAR), todayCal.get(Calendar.MONTH)+1, todayCal.get(Calendar.DAY_OF_MONTH))

        cvDate.setOnDateChangedListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
        }

        // CUSTOM DIALOG WITH FRAGMENT
        // cvSetTime.setOnClickListener {
        //     val fm: FragmentManager =  activity!!.supportFragmentManager
        //     val timDialog: SetTimeDialog = SetTimeDialog.newInstance("Some Title")
        //     timDialog.show(fm, null)
        // }

        // cvSetTime.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

        cvCheckbox.setOnClickListener {
            it.isHapticFeedbackEnabled = true
            it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }

        cvSetTime.setOnClickListener {
            val mTimePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                    tvTimeTitle.text = String.format("%s:%s",
                        if(selectedHour<10) "0$selectedHour" else "$selectedHour",
                        if(selectedMinute<10) "0$selectedMinute" else "$selectedMinute")
                    eventHour = String.format("%s:%s",
                        if(selectedHour<10) "0$selectedHour" else "$selectedHour",
                        if(selectedMinute<10) "0$selectedMinute" else "$selectedMinute")
                }, hour, minute, true)

            it.isHapticFeedbackEnabled = true
            it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }

        tvCheckbox.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
            cvCheckbox.performClick()
        }

        val date: String = String.format("%s/%s/%s",
            calendar.get(Calendar.DAY_OF_MONTH).toString(),
            calendar.get(Calendar.MONTH).toString(),
            calendar.get(Calendar.YEAR).toString())

        binding.btnSave.setOnClickListener {
            val er = EventReminderResponseFire(
                fireDb.collection("Events").document().id,
                binding.tvEventTitle.text.toString(),
                tvEventDescription.text.toString(),
                date,
                false,
                cvCheckbox.isChecked,
                eventHour,
                userUID)

            if (null == er.eventName || er.eventName.isEmpty()) {
                binding.tvEventTitle.error = "Event title is missing!"
                return@setOnClickListener
            }

            fireDb.collection("Events").get().addOnCompleteListener {

                if (it.isSuccessful && null != it.result) {
                    fireDb.collection("Events").add(er)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Event added successfully", Toast.LENGTH_SHORT).show()
                            // findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                            val i = Intent(this, EventListActivity::class.java)
                            startActivity(i)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Event failed to add", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        /* Decommentare se si vuole usare il db locale
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
}