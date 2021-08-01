package mobile.uangku.android.activities.goal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.datepicker.MaterialDatePicker
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_add_goal.*
import mobile.uangku.android.R
import mobile.uangku.android.activities.CategorySelectionActivity
import mobile.uangku.android.core.*
import mobile.uangku.android.models.Category
import mobile.uangku.android.models.Goal
import org.json.JSONObject
import java.util.*


class EditGoalActivity : AppCompatActivity() {

    lateinit var preferences: Preferences
    var goalDate: Calendar = Calendar.getInstance()
    var selectedCategoryID: Int = 0
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

        val adapter = ArrayAdapter(this@EditGoalActivity, R.layout.list_item, Constants.cycles)
        depositCycle.setAdapter(adapter)

        amountTextView.addTextChangedListener(NumberTextWatcherForThousand(amountTextView))
    }


    fun syncCategories() {
        val loadingDialog = LoadingDialog(this)

        loadingDialog.show()

        val request = API.createGetRequest(this, "categories/", null)
        request.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                loadingDialog.dismissIfNeeded()

                val realm = Realm.getDefaultInstance()
                realm.executeTransactionAsync { bgRealm ->
                    Category.fromJSONArray(bgRealm, response.getJSONArray("categories"))
                }
            }

            override fun onError(error: ANError) {
                loadingDialog.dismissIfNeeded()
                API.handleErrorResponse(this@EditGoalActivity, error)
            }
        })
    }

    fun closeIconOnClick(view: View) {
        finish()
    }

    override fun onPause() {
        super.onPause()
        val loadingDialog = LoadingDialog(this)
        if (loadingDialog != null && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CATEGORY_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedCategoryID = data!!.getIntExtra("categoryID", 0)
            goalCategory.setText(data!!.getStringExtra("name"))
        }
    }

    fun submitButtonOnClick(view: View) {
        if (!Validator.validateMinLength(amountTextView, 1)) {
            Alert.dialog(this, "Mohon isi jumlah uang terlebih dahulu")
        } else if(achievmentDate.text.isNullOrEmpty()) {
            Alert.dialog(this, "Mohon isi tanggal terlebih dahulu")
        } else if(selectedCategoryID == 0) {
            Alert.dialog(this, "Mohon pilih category terlebih dahulu")
        } else {
            val loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            // TODO: Add name and deposit cycle
            val androidNetworking = AndroidNetworking.upload(API.getBaseURL(this) + "goals/edit-saving-goals")
                .addMultipartParameter("token", API.TOKEN)
                .addMultipartParameter("session_key", preferences.getString("sessionKey"))
                .addMultipartParameter("category", selectedCategoryID.toString())
                .addMultipartParameter("amount", amountTextView.text.toString())
                .addMultipartParameter("achievement_date", achievmentDate.text.toString())
                .addMultipartParameter("name", "Rumah")
                .addMultipartParameter("deposit_cycle", "2")


            val activity = this
            androidNetworking.setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        loadingDialog.dismissIfNeeded()
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransactionAsync(Realm.Transaction{ bgRealm ->
                            Goal.fromJSON(bgRealm, response)
                        }, Realm.Transaction.OnSuccess {
                            setResult(Activity.RESULT_OK)
                            finish()
                        })
                    }

                    override fun onError(error: ANError) {
                        loadingDialog.dismissIfNeeded()
                        API.handleErrorResponse(this@EditGoalActivity, error)
                    }
                })
        }
    }

    internal inner class NumberTextWatcherForThousand(internal var editText: EditText): TextWatcher {

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(value: Editable) {
            editText.removeTextChangedListener(this)
            val value = editText.text.toString()
            val amount : Double
            if (value.isNotEmpty()) {
                if (value.startsWith("0") || value.startsWith(".") || value.startsWith(","))
                    editText.setText("")
                else {
                    val str = editText.text.toString().replace(".", "")
                    amount = str.toDouble()
                    editText.setText(Utils.addThousandSeparator(amount))
                }
            }

            editText.setSelection(editText.text.toString().length)
            editText.addTextChangedListener(this)
        }
    }

}