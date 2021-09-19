package mobile.uangku.android.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_tab.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.photo
import kotlinx.android.synthetic.main.fragment_home.placeholderProfilePhoto
import kotlinx.android.synthetic.main.fragment_settings.*
import mobile.uangku.android.R
import mobile.uangku.android.core.Preferences
import mobile.uangku.android.core.Utils
import mobile.uangku.android.models.Transaction
import mobile.uangku.android.models.UserData
import java.util.*

class HomeFragment : Fragment() {

    lateinit var preferences: Preferences
    lateinit var fragmentContext: Context
    lateinit var tabActivity: TabActivity
    lateinit var transactions: RealmResults<Transaction>
    var currentDate: Calendar = Calendar.getInstance()
    var firstDate: Calendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        firstDate[Calendar.DAY_OF_MONTH] = 1
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = Preferences(fragmentContext)

        goalSavingsOnClick.setOnClickListener {
            (fragmentContext as TabActivity).bottomNavigationView.selectedItemId = R.id.savings_tab
        }

        logTransactionOnClick.setOnClickListener {
            (fragmentContext as TabActivity).bottomNavigationView.selectedItemId = R.id.transactions_tab
        }

        setupUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context!!
        tabActivity = context as TabActivity
    }

    fun setupUI() {
        val photoUrl = UserData.getUserPhotoUrl(fragmentContext)

        if (photoUrl != null) {
            placeholderProfilePhoto.visibility = View.GONE
            photo.visibility = View.VISIBLE
            photo.setImageURI(photoUrl)
        }

        transactions = Realm.getDefaultInstance().where(Transaction::class.java).greaterThanOrEqualTo("created", firstDate.time)
            .lessThanOrEqualTo("created", currentDate.time).findAll()

        val incomeAmount = transactions.where().equalTo("type", Transaction.Type.INCOME.ordinal)
            .findAll().sum("amount")
        incomeMonthAmount.text = "Rp. ${Utils.addThousandSeparator(incomeAmount.toDouble())}"

    }
}