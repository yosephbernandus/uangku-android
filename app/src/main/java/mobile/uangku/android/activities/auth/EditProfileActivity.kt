package mobile.uangku.android.activities.auth

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_edit_profile.*
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import mobile.uangku.android.models.UserData
import org.json.JSONObject
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    lateinit var preferences: Preferences
    var gender: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        preferences = Preferences(this)

        var name = UserData.getUserName(this)
        if (name != null)
            nameTextView.setText(name)

        var phone = UserData.getPhone(this)
        if (phone != null)
            phone = phone.replace("+62", "0")
            phoneNumberEditText.setText(phone)

        var birthday = UserData.getBirthday(this)
        if (birthday != null)
            birthdayEditText.setText(birthday)

        var gender = UserData.getGender(this)
        if (gender != null){
            if (gender == 1)
                male.isChecked = true
            else
                female.isChecked = true
        }

        var address = UserData.getAddress(this)
        if (address != null)
            addressTextView.setText(address)

        phoneNumberEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(value: Editable?) {
                if (value.isNullOrBlank()) return
                if (value.toString().substring(0, 1) != "0") {
                    Alert.dialog(this@EditProfileActivity, "Nomor telepon harus mulai dari 0")
                    phoneNumberEditText.setText("")
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        birthdayEditText.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build()
            datePicker.show(supportFragmentManager, datePicker.tag)
            datePicker.addOnPositiveButtonClickListener { selection ->
                val date = Date(selection)
                birthdayEditText.setText(DateUtils.toDisplayString(date))
            }
        }
    }

    fun closeIconOnClick(view: View) {
        finish()
    }

    fun submitButtonOnClick(view: View) {
        val preferences = Preferences(this)
        if (!Validator.validateMinLength(nameTextView, 1))
            Alert.dialog(this, "Mohon isi nama anda")
        else if (!Validator.validateMinLength(phoneNumberEditText, 1))
            Alert.dialog(this, "Mohon Masukan nomor telepon anda")
        else if (birthdayEditText.text.isNullOrEmpty())
            Alert.dialog(this, "Mohon isi tanggal lahir anda terlebih dahulu")
        else if (!male.isChecked && !female.isChecked)
            Alert.dialog(this, "Mohon pilih jenis kelamin anda")
        else {
            val loadingDialog = LoadingDialog(this)
            loadingDialog.show()

            if (male.isChecked)
                gender = 1
            else if (female.isChecked)
                gender = 2

            val androidNetworking = AndroidNetworking.upload(API.getBaseURL(this) + "auth/edit-profile")
                .addMultipartParameter("token", API.TOKEN)
                .addMultipartParameter("session_key", preferences.getString("sessionKey"))
                .addMultipartParameter("name", nameTextView.text.toString())
                .addMultipartParameter("phone", phoneNumberEditText.text.toString())
                .addMultipartParameter("birthday", birthdayEditText.text.toString())
                .addMultipartParameter("gender", gender.toString())
                .addMultipartParameter("address", addressTextView.text.toString())

            androidNetworking.setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        loadingDialog.dismissIfNeeded()
                        Session.saveUserData(this@EditProfileActivity, response)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }

                    override fun onError(error: ANError) {
                        API.handleErrorResponse(this@EditProfileActivity, error)
                        loadingDialog.dismissIfNeeded()
                    }
                })

        }
    }

}