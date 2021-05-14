package mobile.uangku.android.core

import android.app.Activity
import android.content.Context
import org.json.JSONObject

object Session {

    fun isAuthenticated(context: Context): Boolean {
        return key(context).isNotEmpty()
    }

    fun key(context: Context): String {
        return Preferences(context).getString("sessionKey")
    }

    fun userKeySet(activity: Activity, response: JSONObject) {
        val preferences = Preferences(activity)
        preferences.set("sessionKey", response.getString("session_key"))
    }
}