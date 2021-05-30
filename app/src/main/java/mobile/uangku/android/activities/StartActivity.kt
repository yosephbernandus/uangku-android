package mobile.uangku.android.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import mobile.uangku.android.R
import mobile.uangku.android.activities.auth.LoginActivity
import mobile.uangku.android.core.Session

class StartActivity : AppCompatActivity() {
    lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        if (Session.isAuthenticated(this)) {
            val activityIntent = Intent(this, TabActivity::class.java)
            startActivity(activityIntent)
            finish()
        } else {
            handler = Handler()
            handler.postDelayed({
                val activityIntent = Intent(this, LoginActivity::class.java)
                startActivity(activityIntent)
                finish()
            }, 700)
        }
    }

    fun startOnClick(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
