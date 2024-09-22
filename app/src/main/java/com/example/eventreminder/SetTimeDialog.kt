package com.example.eventreminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TimePicker
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import java.util.*


class SetTimeDialog : DialogFragment() {
    private var mTime: TimePicker? = null
    private var mHour = 0
    private var mMinute = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.set_time_dialog, container)
    }

    override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val c = Calendar.getInstance()
        mHour = c.get(Calendar.HOUR)
        mMinute = c.get(Calendar.MINUTE)

        // Get field from view
        val mTime: TimePicker = view.findViewById(R.id.tp_set_time_dialog)

        // Fetch arguments from bundle and set title
        // val title: String = arguments?.getString("title", "Enter Name") ?: dialog.setTitle(title)
        // Show soft keyboard automatically and request focus to field

        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
    }

    companion object {
        fun newInstance(title: String?): SetTimeDialog {
            val frag = SetTimeDialog()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
    }
}