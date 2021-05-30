package mobile.uangku.android.activities.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_login.*
import mobile.uangku.android.R
import mobile.uangku.android.activities.TabActivity
import mobile.uangku.android.core.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Session.isAuthenticated(this)){
            startActivity(Intent(this, TabActivity::class.java))
        }

        rootLayout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
                }
                return false
            }
        })
        addTextChangeListener(emailEditText)
        addTextChangeListener(passwordEditText)
    }

    fun loginButtonOnClick(view: View) {
        if (!Validator.validateMinLength(emailEditText, 1))
            Alert.dialog(this, "Mohon masukan surel / nomor ponsel dengan benar")
        else if (!Validator.validateMinLength(passwordEditText, 1))
            Alert.dialog(this, "Mohon masukan password anda")
        else {
            val json = JSONObject()
            json.put("username", emailEditText.text.toString())
            json.put("password", passwordEditText.text.toString())

            val loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            val request = API.createPostRequest(this, "auth/login", json)
            request.getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    loadingDialog.dismissIfNeeded()
                    Session.bootstrap(this@LoginActivity, response)
                }

                override fun onError(error: ANError) {
                    API.handleErrorResponse(this@LoginActivity, error)
                    loadingDialog.dismissIfNeeded()
                }
            })
        }
    }

    fun signUpOnClick(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    fun addTextChangeListener(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(value: Editable?) {
                if (value.isNullOrBlank()) return
                setupButtonStyle()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })
    }

    fun setupButtonStyle() {
        if (Validator.validateMinLength(emailEditText, 1) || Validator.validateMinLength(passwordEditText, 1)){
            loginButton.isEnabled = true
            loginButton.getBackground().setAlpha(255)
        } else {
            loginButton.isEnabled = false
            loginButton.getBackground().setAlpha(51)
        }
    }
}