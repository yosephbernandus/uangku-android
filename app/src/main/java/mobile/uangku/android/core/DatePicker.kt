package mobile.uangku.android.core

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.date_picker_layout.view.*
import mobile.uangku.android.R
import java.util.*

class DatePicker : BottomSheetDialogFragment {

    var datePicker: android.widget.DatePicker? = null
    var initialDate: Calendar = Calendar.getInstance()
    var dateSelectedHandler: DateSelectedHandler? = null

    interface DateSelectedHandler {
        fun onDateSelected(dateSelected: Calendar)
    }

    constructor() {}

    @SuppressLint("ValidFragment")
    constructor(initialDate: Calendar, selectedHandler: DateSelectedHandler) {
        dateSelectedHandler = selectedHandler
        this.initialDate = initialDate
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val view = View.inflate(context, R.layout.date_picker_layout, null)

        datePicker = view.date_picker
        datePicker!!.init(initialDate.get(Calendar.YEAR), initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH), null)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -12)
        datePicker!!.maxDate = calendar.timeInMillis

        val doneLayout = view.done as TextView
        doneLayout.setOnClickListener {
            initialDate.set(Calendar.DAY_OF_MONTH, datePicker!!.dayOfMonth)
            initialDate.set(Calendar.MONTH, datePicker!!.month)
            initialDate.set(Calendar.YEAR, datePicker!!.year)

            dismiss()
            dateSelectedHandler?.onDateSelected(initialDate)
        }

        dialog.setContentView(view)
    }
}
