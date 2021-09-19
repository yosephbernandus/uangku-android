package mobile.uangku.android.activities.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import kotlinx.android.synthetic.main.activity_change_password.*
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import org.json.JSONObject

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
    }

    fun closeIconOnClick(view: View) {
        finish()
    }

    fun submitButtonOnClick(view: View) {
        val preferences = Preferences(this)
        if (!Validator.validateMinLength(oldPassword, 1))
            Alert.dialog(this, "Please enter your old password")
        else if(!Validator.validateMinLength(newPassword, 8))
            Alert.dialog(this, "Please enter your new password at least 8 characters")
        else if (!Validator.validateMinLength(newPasswordConfirmation, 1))
            Alert.dialog(this, "Please enter your new password confirmation")
        else if(newPassword.text.toString() != newPasswordConfirmation.text.toString())
            Alert.dialog(this, "Your confirmation password not match with your password")
        else {
            val loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            val androidNetworking = AndroidNetworking.upload(API.getBaseURL(this) + "auth/change-password")
                .addMultipartParameter("token", API.TOKEN)
                .addMultipartParameter("session_key", preferences.getString("sessionKey"))
                .addMultipartParameter("old_password", oldPassword.text.toString())
                .addMultipartParameter("new_password1", newPassword.text.toString())
                .addMultipartParameter("new_password2", newPasswordConfirmation.text.toString())

            androidNetworking.setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object: JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        loadingDialog.dismissIfNeeded()
                        Session.clear(this@ChangePasswordActivity)
                    }

                    override fun onError(error: ANError) {
                        API.handleErrorResponse(this@ChangePasswordActivity, error)
                        loadingDialog.dismissIfNeeded()
                    }
                })
        }
    }
}