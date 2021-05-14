package mobile.uangku.android.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import mobile.uangku.android.R
import mobile.uangku.android.activities.auth.LoginActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    fun startOnClick(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
