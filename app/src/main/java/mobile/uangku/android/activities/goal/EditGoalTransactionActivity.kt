package mobile.uangku.android.activities.goal

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import io.realm.Realm
import kotlinx.android.synthetic.main.acitivity_add_goal_transaction.*
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import mobile.uangku.android.models.Goal
import mobile.uangku.android.models.GoalTransaction
import org.json.JSONObject

class EditGoalTransactionActivity : AppCompatActivity() {

    lateinit var goal: Goal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_add_goal_transaction)

        goal = Goal.get(intent.getIntExtra("id", 0))!!

        amountSavings.setText(Utils.addThousandSeparator(goal.depositAmountPerCycle))
        amountSavings.addTextChangedListener(NumberTextWatcherForThousand(amountSavings))
    }

    fun submitButtonOnClick(view: View) {
        val preferences = Preferences(this)
        if (!Validator.validateMinLength(amountSavings, 1)) {
            Alert.dialog(this, "Mohon isi jumlah uang terlebih dahulu")
        } else {
            val loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            val amount = amountSavings.text.toString().replace(".", "")
            val androidNetworking = AndroidNetworking.upload(API.getBaseURL(this) + "goals/edit-transaction")
                .addMultipartParameter("token", API.TOKEN)
                .addMultipartParameter("session_key", preferences.getString("sessionKey"))
                .addMultipartParameter("goal", goal.id.toString())
                .addMultipartParameter("amount", amount)

            androidNetworking.setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object: JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        loadingDialog.dismissIfNeeded()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }

                    override fun onError(error: ANError) {
                        loadingDialog.dismissIfNeeded()
                        API.handleErrorResponse(this@EditGoalTransactionActivity, error)
                    }
                })
        }
    }

    fun closeIconOnClick(view: View) {
        finish()
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