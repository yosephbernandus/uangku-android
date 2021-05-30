package mobile.uangku.android.models

import android.content.Context
import mobile.uangku.android.core.Preferences

object UserData {

    fun getUserName(context: Context): String? {
        val preferences = Preferences(context)
        if (preferences.has("name")) return preferences.getString("name")
        return null
    }

    fun setUserName(context: Context, name: String) {
        Preferences(context)["name"] = name
    }

    fun getNik(context: Context): String? {
        val preferences = Preferences(context)
        if (preferences.has("nik")) return preferences.getString("nik")
        return null
    }

    fun setNik(context: Context, nik: String) {
        Preferences(context)["nik"] = nik
    }

    fun getEmail(context: Context): String? {
        val preferences = Preferences(context)
        if (preferences.has("email")) return preferences.getString("email")
        return null
    }

    fun setEmail(context: Context, email: String) {
        Preferences(context)["email"] = email
    }

    fun getPhone(context: Context): String? {
        val preferences = Preferences(context)
        if (preferences.has("phone")) return preferences.getString("phone")
        return null
    }

    fun setPhone(context: Context, phone: String) {
        Preferences(context)["phone"] = phone
    }

    fun getGender(context: Context): Int? {
        val preferences = Preferences(context)
        if (preferences.has("gender")) return preferences.getInt("gender")
        return null
    }

    fun setGender(context: Context, gender: Int) {
        Preferences(context)["gender"] = gender
    }

    fun getBirthday(context: Context): String? {
        val preferences = Preferences(context)
        if (preferences.has("birthday")) return preferences.getString("birthday")
        return null
    }

    fun setBirthday(context: Context, birthday: String) {
        Preferences(context)["birthday"] = birthday
    }

    fun getAddress(context: Context): String? {
        val preferences = Preferences(context)
        if (preferences.has("address")) return preferences.getString("address")
        return null
    }

    fun setAddress(context: Context, address: String) {
        Preferences(context)["address"] = address
    }
}