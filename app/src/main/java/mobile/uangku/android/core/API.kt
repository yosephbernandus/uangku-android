package mobile.uangku.android.core

import android.app.AlertDialog
import com.androidnetworking.error.ANError

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.ANRequest
import com.androidnetworking.common.Priority
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject
import java.io.File

object API {

    val PRODUCTION_URL = "https://apps.uangku.id/"
    val TOKEN = "Ip089Lww4CgHGea9Yr3N6Pfb6Juea6OedTqHcBv1WtVAdcKDY4hAxnf1MwEqaX617UyieEW"

    val BASE_API_KEY = "baseURL"
    val PREFERENCES_KEY = "APIPreferences"

    fun setPreferencesURL(context: Context, baseURL: String) {
        val preferences = Preferences(context, PREFERENCES_KEY)
        preferences[BASE_API_KEY] = baseURL
    }

    fun getBaseURL(context: Context): String {
        var url = Preferences(context, PREFERENCES_KEY).getString(BASE_API_KEY)
        if (url.isEmpty())
            url = PRODUCTION_URL

        return "${url}api/"
    }

    fun handleErrorResponse(context: Context, error: ANError) {
        when (error.errorCode) {
            0 -> Alert.dialog(context, "Tidak dapat terhubung ke internet. Harap periksa internet Anda.")
            403 -> promptForLogin(context)
            429 -> Alert.dialog(context,"Maaf, Permintaan ini belum bisa di proses dikarenakan banyaknya jumlah permintaan yang dikirimkan. Mohon coba lagi dalam beberapa menit.")
            else -> {
                try {
                    val jsonData = JSONObject(error.errorBody)
                    if (jsonData.has("error_message"))
                        Alert.dialog(context, jsonData.getString("error_message"))
                    else
                        Alert.dialog(context, "Maaf, kami kesulitan memproses permintaan Anda, Silahkan coba lagi nanti.")
                } catch (_: JSONException) {
                    Alert.dialog(context, "Tidak dapat terhubung ke server. Silahkan coba lagi nanti.")
                }
            }
        }
    }

    fun createPostRequest(context: Context, url: String, body: JSONObject?): ANRequest<out ANRequest<*>> {
        val json = addAuthorizationToBody(context, body)

        return AndroidNetworking.post("${getBaseURL(context)}$url")
                .addJSONObjectBody(json)
                .setPriority(Priority.HIGH)
                .build()
    }

    fun createMultipartPostRequest(context: Context, url: String, body: MutableMap<String, String>, image: MutableMap<String, File>?): ANRequest<out ANRequest<*>> {
        body.put("token", API.TOKEN)
        val sessionKey = Session.key(context)
        if (sessionKey.isNotEmpty())
            body.put("session_key", sessionKey)

        val androidNetworking = AndroidNetworking.upload("${getBaseURL(context)}$url")
                .addMultipartParameter(body)
        if (image != null && image.size != 0) {
            androidNetworking.addMultipartFile(image)
        }

        return androidNetworking.setPriority(Priority.HIGH).build()
    }

    fun createGetRequest(context: Context, url: String, body: JSONObject? = null, useStampsServer: Boolean = false): ANRequest<out ANRequest<*>> {
        val json = addAuthorizationToBody(context, body)

        var requestUrl = "${getBaseURL(context)}$url?"
        for (key in json.keys())
            requestUrl += "$key=${json[key]}&"

        return AndroidNetworking.get(requestUrl)
                .setPriority(Priority.HIGH)
                .build()
    }

    fun promptForLogin(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Sepertinya sesi Anda telah berakhir. Harap keluar dan masuk lagi.")
        builder.setPositiveButton("OK") { _, _ -> Session.clear(context) }
        val alert = builder.create()
        alert.show()
    }

    fun getJSONParams(context: Context): JsonObject {
        val json = JsonObject()
        json.addProperty("token", TOKEN)
        json.addProperty("session_key", Session.key(context))
        return json
    }

    fun addAuthorizationToBody(context: Context, body: JSONObject?): JSONObject {
        var json = body
        if (json == null)
            json = JSONObject()

        json.put("token", API.TOKEN)

        val sessionKey = Session.key(context)
        if (sessionKey.isNotEmpty())
            json.put("session_key", sessionKey)

        return json
    }
}