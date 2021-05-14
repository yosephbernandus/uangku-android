package mobile.uangku.android.core

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class Preferences(context: Context, key: String = "preferences") {

    val sharedPreferences: SharedPreferences = context.getSharedPreferences(key, 0)
    private val editor: SharedPreferences.Editor

    init {
        editor = sharedPreferences.edit()
    }

    operator fun set(key: String, value: String) {
        editor.putString(key, value)
        editor.commit()
    }

    operator fun set(key: String, value: Int) {
        editor.putInt(key, value)
        editor.commit()
    }

    operator fun set(key: String, value: Long) {
        editor.putLong(key, value)
        editor.commit()
    }

    operator fun set(key: String, value: Boolean) {
        editor.putBoolean(key, value)
        editor.commit()
    }

    operator fun set(key: String, data: JSONArray) {
        val stringData = data.toString().replace("\"", "")
        set(key, stringData.substring(1, stringData.length - 1))
    }

    fun getString(key: String): String {
        return sharedPreferences.getString(key, "").toString()
    }

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun getLong(key: String): Long {
        return sharedPreferences.getLong(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun delete(key: String) {
        sharedPreferences.edit().remove(key).commit()
    }

    fun clear() {
        editor.clear().commit()
    }

    fun has(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
