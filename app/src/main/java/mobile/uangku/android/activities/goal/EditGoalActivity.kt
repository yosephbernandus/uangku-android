package mobile.uangku.android.activities.goal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_add_goal.*
import mobile.uangku.android.R
import mobile.uangku.android.activities.CategorySelectionActivity
import mobile.uangku.android.core.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale


class EditGoalActivity : AppCompatActivity() {

    var goalDate: Calendar = Calendar.getInstance()
    val CATEGORY_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goal)

        achievmentDate.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            datePicker.show(supportFragmentManager, datePicker.tag)

            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                achievmentDate.setText(DateUtils.toDateString(date))
            }

        }

        goalCategory.setOnClickListener {
            startActivityForResult(Intent(this, CategorySelectionActivity::class.java), CATEGORY_REQUEST_CODE)
        }
    }

}