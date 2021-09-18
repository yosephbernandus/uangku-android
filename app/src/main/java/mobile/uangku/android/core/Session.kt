package mobile.uangku.android.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.realm.Realm
import mobile.uangku.android.activities.TabActivity
import mobile.uangku.android.activities.auth.LoginActivity
import mobile.uangku.android.models.UserData
import org.json.JSONObject

object Session {

    fun isAuthenticated(context: Context): Boolean {
        return key(context).isNotEmpty()
    }

    fun key(context: Context): String {
        return Preferences(context).getString("sessionKey")
    }

    fun bootstrap(activity: Activity, response: JSONObject) {
        val preferences = Preferences(activity)
        preferences["sessionKey"] = response.getString("session_key")
        saveUserData(activity, response.getJSONObject("user"))

        val intent = Intent(activity, TabActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(intent)
    }

    fun clear(context: Context) {
        val preferences = Preferences(context)
        preferences.clear()

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()

        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("action", "logout")
        context.startActivity(intent)
        (context as Activity).finish()
    }

    fun saveUserData(context: Context, userDataJSON: JSONObject) {
        UserData.setUserName(context, userDataJSON.getString("name"))
        if (!userDataJSON.isNull("nik")) UserData.setNik(context, userDataJSON.getString("nik"))
        UserData.setEmail(context, userDataJSON.getString("email"))
        UserData.setPhone(context, userDataJSON.getString("phone"))
        UserData.setGender(context, userDataJSON.getInt("gender"))
        UserData.setBirthday(context, userDataJSON.getString("birthday"))
        UserData.setAddress(context, userDataJSON.getString("address"))
        if (!userDataJSON.isNull("photoUrl"))
            UserData.setUserPhotoUrl(context, userDataJSON.getString("photoUrl"))
    }
}