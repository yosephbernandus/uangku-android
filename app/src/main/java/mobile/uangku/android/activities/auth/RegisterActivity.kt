package mobile.uangku.android.activities.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.emailEditText
import kotlinx.android.synthetic.main.activity_register.passwordEditText
import kotlinx.android.synthetic.main.activity_register.rootLayout
import mobile.uangku.android.R
import mobile.uangku.android.activities.TabActivity
import mobile.uangku.android.core.*
import org.json.JSONObject
import java.util.*


class RegisterActivity : AppCompatActivity() {

    var birthday: Calendar = Calendar.getInstance()
    lateinit var datePicker: DatePicker
    lateinit var inputMethodManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        if (Session.isAuthenticated(this)){
            startActivity(Intent(this, TabActivity::class.java))
        }

        phoneEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(value: Editable?) {
                if (value.isNullOrBlank()) return
                if (value.toString().substring(0, 1) != "0") {
                    Alert.dialog(this@RegisterActivity, "Your phone number must start from 0")
                    phoneEditText.setText("")
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        datePicker = DatePicker(birthday, object : DatePicker.DateSelectedHandler {
            override fun onDateSelected(dateSelected: Calendar) {
                birthday = dateSelected
                birthdayEditText.setText(DateUtils.toDisplayString(birthday.time))
            }
        })

        birthdayEditText.setOnTouchListener { _, _ ->
            if (datePicker.dialog == null) {
                inputMethodManager.hideSoftInputFromWindow(rootLayout.windowToken, 0)
                datePicker.show(supportFragmentManager, datePicker.tag)
            }
            true
        }
        rootLayout.setOnTouchListener(DismissKeyboardOnTouch(this))
    }

    fun birthdayTextViewOnClick(view: View) {
        if (datePicker.dialog == null) {
            inputMethodManager.hideSoftInputFromWindow(rootLayout.windowToken, 0)
            datePicker.show(supportFragmentManager, datePicker.tag)
        }
    }

    fun signInOnClick(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    fun registerButtonOnClick(view: View) {
        if (!Validator.validateMinLength(nameEditText, 1))
            Alert.dialog(this, "Please enter your name")
        else if (!Validator.validateEmail(emailEditText))
            Alert.dialog(this, "Please enter your email")
        else if (!Validator.validateMinLength(phoneEditText, 1))
            Alert.dialog(this, "Please enter your phone number")
        else if (!Validator.validateMinLength(birthdayEditText, 1))
            Alert.dialog(this, "Please select your date of birth")
        else if (!Validator.validateMinLength(passwordEditText, 8))
            Alert.dialog(this, "Please enter your password at least 8 characters")
        else if (!Validator.validateMinLength(confirmPasswordEditText, 1))
            Alert.dialog(this, "Please enter your password confirmation")
        else if (passwordEditText.text.toString() != confirmPasswordEditText.text.toString())
            Alert.dialog(this, "Your confirmation password not match with your password")
        else {
            val json = JSONObject()
            json.put("name", nameEditText.text.toString())
            json.put("email", emailEditText.text.toString())
            json.put("phone", phoneEditText.text.toString())
            json.put("birthday", birthdayEditText.text.toString())
            json.put("password", passwordEditText.text.toString())

            val loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            val request = API.createPostRequest(this, "auth/register", json)
            request.getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    loadingDialog.dismissIfNeeded()
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                }

                override fun onError(error: ANError) {
                    API.handleErrorResponse(this@RegisterActivity, error)
                    loadingDialog.dismissIfNeeded()
                }
            })
        }
    }
}