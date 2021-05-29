package mobile.uangku.android.core

import android.app.Activity
import android.content.Context
import android.content.Intent
import io.realm.Realm
import mobile.uangku.android.activities.auth.LoginActivity
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
}