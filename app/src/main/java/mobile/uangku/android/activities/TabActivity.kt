package mobile.uangku.android.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.LabelVisibilityMode
import kotlinx.android.synthetic.main.activity_tab.*
import mobile.uangku.android.R
import mobile.uangku.android.activities.goal.GoalFragment
import mobile.uangku.android.activities.transaction.TransactionFragment

class TabActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            val fragment: Fragment
            val menuItemSelectedID = menuItem.itemId

            if (menuItemSelectedID == R.id.home_tab) fragment = HomeFragment()
            else if (menuItemSelectedID == R.id.savings_tab) fragment = GoalFragment()
            else if (menuItemSelectedID == R.id.transactions_tab) fragment = TransactionFragment()
            else if (menuItemSelectedID == R.id.chart_tab) fragment = ChartFragment()
            else fragment = SettingsFragment()

            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.content, fragment)
            fragmentTransaction.commit()

            menuItem.isChecked = true
            false
        }

        bottomNavigationView.selectedItemId = R.id.home_tab
        disableShiftMode()
    }

    @SuppressLint("RestrictedApi")
    fun disableShiftMode() {
        val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        try {
            val field = menuView.javaClass.getDeclaredField("mShiftingMode")
            field.isAccessible = true
            field.setBoolean(menuView, false)
            field.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView

                item.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED)

                // set once again checked value, so view will be updated
                item.setChecked(item.itemData.isChecked)
            }
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        }
    }
}