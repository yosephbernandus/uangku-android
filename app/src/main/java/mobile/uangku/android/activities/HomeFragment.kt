package mobile.uangku.android.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_tab.*
import kotlinx.android.synthetic.main.fragment_home.*
import mobile.uangku.android.R
import mobile.uangku.android.core.Preferences

class HomeFragment : Fragment() {

    lateinit var preferences: Preferences
    lateinit var fragmentContext: Context

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = Preferences(fragmentContext)

        goalSavingsOnClick.setOnClickListener {
            (fragmentContext as TabActivity).bottomNavigationView.selectedItemId = R.id.savings_tab
        }
    }
}