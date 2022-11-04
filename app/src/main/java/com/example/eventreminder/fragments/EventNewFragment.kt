package com.example.eventreminder

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.eventreminder.databinding.EventNewFragmentBinding
import com.example.eventreminder.models.EventReminderResponseFire
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import java.util.Calendar.getInstance

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class EventNewFragment : Fragment() {

    private var _binding: EventNewFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var calendar: Calendar = GregorianCalendar()
    private var todayCal: Calendar = getInstance()
    private var fireDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var eventHour = ""
    private var hour = 0
    private var minute = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = EventNewFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tvEventName: TextView = view.findViewById(R.id.tv_event_title)
        val tvEventDescription: TextView = view.findViewById(R.id.tv_event_description)
        val cvDate: DatePicker = view.findViewById(R.id.cv_event_date)
        val cvCheckbox: CheckBox = view.findViewById(R.id.cb_checkbox)
        val tvCheckbox: LinearLayout = view.findViewById(R.id.ll_checkbox)
        val cvSetTime: LinearLayout = view.findViewById(R.id.cv_set_time)
        val tvTimeTitle: TextView = view.findViewById(R.id.tv_time_title)
        // val df = SimpleDateFormat("dd-MM-yyyy")
        // val formattedDate: String = df.format(todayCal.time)

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
            val mTimePicker = TimePickerDialog(view.context,
                { _, selectedHour, selectedMinute ->
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
                tvEventName.text.toString(),
                tvEventDescription.text.toString(),
                date,
                false,
                cvCheckbox.isChecked,
                eventHour)

            if (null == er.eventName || er.eventName.isEmpty()) {
                tvEventName.error = "Event title is missing!"
                return@setOnClickListener
            }

            fireDb.collection("Events").get().addOnCompleteListener {

                if (it.isSuccessful && null != it.result) {
                    fireDb.collection("Events").add(er)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Event added successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Event failed to add", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}