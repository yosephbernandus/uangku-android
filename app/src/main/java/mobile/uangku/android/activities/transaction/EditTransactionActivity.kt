package mobile.uangku.android.activities.transaction

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.PersistableBundle
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
import kotlinx.android.synthetic.main.activity_add_goal.*
import kotlinx.android.synthetic.main.activity_add_transaction.*
import kotlinx.android.synthetic.main.activity_add_transaction.amountTextView
import kotlinx.android.synthetic.main.activity_add_transaction.nameTextView
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import mobile.uangku.android.models.Transaction
import org.json.JSONObject

class EditTransactionActivity : AppCompatActivity() {

    lateinit var preferences: Preferences
    var type: Int = Transaction.Type.INCOME.ordinal
    var selectedCategoryID: Int = 0
    val CATEGORY_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        incomeTab.setOnClickListener {
            type = Transaction.Type.INCOME.ordinal
            incomeTabText.setTextColor(resources.getColor(R.color.white))
            incomeTab.background.setColorFilter(
                Color.parseColor("#315A71"),
                PorterDuff.Mode.SRC_ATOP
            )

            outcomeTabText.setTextColor(resources.getColor(R.color.tabTextInactive))
            outcomeTab.background.setColorFilter(
                Color.parseColor("#FFFFFF"),
                PorterDuff.Mode.SRC_ATOP
            )
        }

        outcomeTab.setOnClickListener {
            type = Transaction.Type.OUTCOME.ordinal
            outcomeTabText.setTextColor(resources.getColor(R.color.white))
            outcomeTab.background.setColorFilter(
                Color.parseColor("#315A71"),
                PorterDuff.Mode.SRC_ATOP
            )

            incomeTabText.setTextColor(resources.getColor(R.color.tabTextInactive))
            incomeTab.background.setColorFilter(
                Color.parseColor("#FFFFFF"),
                PorterDuff.Mode.SRC_ATOP
            )
        }
        amountTextView.addTextChangedListener(NumberTextWatcherForThousand(amountTextView))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CATEGORY_REQUEST_CODE && resultCode == RESULT_OK) {
            selectedCategoryID = data!!.getIntExtra("categoryID", 0)
            transactionCategory.setText(data!!.getStringExtra("name"))
        }
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

    fun submitButtonOnClick(view: View) {
        val preferences = Preferences(this)
        if (!Validator.validateMinLength(nameTextView, 1)) {
            Alert.dialog(this, "Mohon isi nama transaksi")
        } else if (!Validator.validateMinLength(amountTextView, 1)) {
            Alert.dialog(this, "Mohon isi jumlah uang terlebih dahulu")
        } else if(selectedCategoryID == 0) {
            Alert.dialog(this, "Mohon pilih category terlebih dahulu")
        } else {
            val loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            val amount = amountTextView.text.toString().replace(".", "")
            val androidNetworking = AndroidNetworking.upload(API.getBaseURL(this) + "transactions/edit-transaction")
                .addMultipartParameter("token", API.TOKEN)
                .addMultipartParameter("session_key", preferences.getString("sessionKey"))
                .addMultipartParameter("name", nameTextView.text.toString())
                .addMultipartParameter("type", type.toString())
                .addMultipartParameter("amount", amount)
                .addMultipartParameter("category", selectedCategoryID.toString())
                .addMultipartParameter("notes", notesTextView.text.toString())

            androidNetworking.setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object: JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        loadingDialog.dismissIfNeeded()
                        val realm = Realm.getDefaultInstance()
                        realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                            Transaction.fromJSON(bgRealm, response)
                        }, Realm.Transaction.OnSuccess {
                            setResult(Activity.RESULT_OK)
                            finish()
                        })
                    }

                    override fun onError(error: ANError) {
                        loadingDialog.dismissIfNeeded()
                        API.handleErrorResponse(this@EditTransactionActivity, error)
                    }
                })
        }

    }

    internal inner class NumberTextWatcherForThousand(internal var editText: EditText):
        TextWatcher {

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