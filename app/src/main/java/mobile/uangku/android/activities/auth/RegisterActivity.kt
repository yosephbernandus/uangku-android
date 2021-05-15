package mobile.uangku.android.activities.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import mobile.uangku.android.R
import mobile.uangku.android.activities.TabActivity
import mobile.uangku.android.core.Session

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if (Session.isAuthenticated(this)){
            startActivity(Intent(this, TabActivity::class.java))
        }
    }

    fun signInOnClick(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}