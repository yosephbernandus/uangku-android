package mobile.uangku.android.activities.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mobile.uangku.android.R
import mobile.uangku.android.activities.TabActivity
import mobile.uangku.android.core.Session

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Session.isAuthenticated(this)){
            startActivity(Intent(this, TabActivity::class.java))
        }
    }
}