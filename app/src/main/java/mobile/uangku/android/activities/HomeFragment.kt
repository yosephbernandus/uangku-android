package mobile.uangku.android.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_tab.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.photo
import kotlinx.android.synthetic.main.fragment_home.placeholderProfilePhoto
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.goal_fragment_item.view.*
import kotlinx.android.synthetic.main.goal_fragment_item.view.goalIcon
import kotlinx.android.synthetic.main.last_goal_item.view.*
import kotlinx.android.synthetic.main.last_transaction_item.view.*
import mobile.uangku.android.R
import mobile.uangku.android.core.Constants
import mobile.uangku.android.core.DateUtils
import mobile.uangku.android.core.Preferences
import mobile.uangku.android.core.Utils
import mobile.uangku.android.models.Category
import mobile.uangku.android.models.Goal
import mobile.uangku.android.models.Transaction
import mobile.uangku.android.models.UserData
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    lateinit var preferences: Preferences
    lateinit var fragmentContext: Context
    lateinit var tabActivity: TabActivity
    lateinit var transactions: RealmResults<Transaction>
    lateinit var goals: RealmResults<Goal>
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

        allTransactions.setOnClickListener {
            (fragmentContext as TabActivity).bottomNavigationView.selectedItemId = R.id.transactions_tab
        }

        allGoal.setOnClickListener {
            (fragmentContext as TabActivity).bottomNavigationView.selectedItemId = R.id.savings_tab
        }

        setupUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context!!
        tabActivity = context as TabActivity
    }

    fun setupUI() {
        if(!isAdded)
            return

        val photoUrl = UserData.getUserPhotoUrl(fragmentContext)

        if (photoUrl != null) {
            placeholderProfilePhoto.visibility = View.GONE
            photo.visibility = View.VISIBLE
            photo.setImageURI(photoUrl)
        }

        goals = Realm.getDefaultInstance().where(Goal::class.java).limit(5)
            .sort("id", Sort.DESCENDING).findAll()

        transactions = Realm.getDefaultInstance().where(Transaction::class.java).greaterThanOrEqualTo("created", firstDate.time)
            .lessThanOrEqualTo("created", currentDate.time).findAll()

        val lastTransaction = transactions.where().limit(5)
            .sort("id", Sort.DESCENDING).findAll()

        val incomeAmount = transactions.where().equalTo("type", Transaction.Type.INCOME.ordinal)
            .findAll().sum("amount")
        incomeMonthAmount.text = "Rp. ${Utils.addThousandSeparator(incomeAmount.toDouble())}"

        // TODO: ADD LAYOUT IF TRANSACTION IS NONE
        listTransactionRecylerView.visibility = View.VISIBLE
        listTransactionRecylerView.adapter = RecyclerViewAdapter(lastTransaction)
        listTransactionRecylerView.layoutManager = LinearLayoutManager(fragmentContext, LinearLayoutManager.HORIZONTAL, false)
        listTransactionRecylerView.isNestedScrollingEnabled = false

        // TODO: ADD LAYOUT IF GOAL IS NONE
        listGoalRecyclerView.visibility = View.VISIBLE
        listGoalRecyclerView.adapter = RecyclerViewAdapterGoal(goals)
        listGoalRecyclerView.layoutManager = LinearLayoutManager(fragmentContext)
        listGoalRecyclerView.isNestedScrollingEnabled = false
    }

    internal inner class RecyclerViewAdapter(var transactions: RealmResults<Transaction>): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.last_transaction_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val transaction = transactions[position]
            var amount = transaction!!.amount
            var type = transaction!!.type
            holder.id = transaction!!.id!!

            if (transaction != null && transaction.categoryId != null) {
                var category = Category[transaction.categoryId!!]
                if (category != null)
                    holder.categoryNameTextView.text = category.name
                    holder.categoryIcon.setImageURI(category!!.logoUrl)
            }
            if (type == Transaction.Type.INCOME.ordinal) {
                holder.transactionAmountTextView.text = "+ Rp. ${Utils.addThousandSeparator(amount)}"
                holder.transactionAmountTextView.setTextColor(resources.getColor(R.color.linkSection))
            } else {
                holder.transactionAmountTextView.text = "- Rp. ${Utils.addThousandSeparator(amount)}"
                holder.transactionAmountTextView.setTextColor(resources.getColor(R.color.minus))
            }
        }

        override fun getItemCount(): Int {
            return transactions.size
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var id = 0
            val transactionAmountTextView: TextView = view.transactionAmountTextView
            val categoryNameTextView: TextView = view.categoryNameTextView
            val categoryIcon: SimpleDraweeView = view.categoryIcon

        }
    }

    internal inner class RecyclerViewAdapterGoal(var goals: RealmResults<Goal>): RecyclerView.Adapter<RecyclerViewAdapterGoal.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.last_goal_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val goal = goals[position]
            var goalTransaction = "Rp. 0 dari Rp. ${Utils.addThousandSeparator(goal!!.amount)}"
            var dateFormat =  SimpleDateFormat("yyyy-MM-dd")
            var currentDate = DateUtils.fromDateString(dateFormat.format(Date()))!!.time
            var achievementDate = goal.achievementDate!!.time
            var differenceTime = Utils.getDifferenceTime((achievementDate - currentDate), Constants.DAYS)

            if (goal!!.transactions != null){
                val transactionAmount = goal!!.transactions!!.where().sum("amount").toDouble()
                goalTransaction = "Rp. ${Utils.addThousandSeparator(transactionAmount)} dari Rp. ${Utils.addThousandSeparator(goal!!.amount)}"
            }

            holder.id = goal!!.id!!
            if (goal != null && goal.categoryId != null) {
                var category = Category[goal.categoryId!!]
                if (category != null) {
                    holder.goalIcon.setImageURI(category.logoUrl)
                    holder.goalCategoryName.text = category.name
                }
            }

            holder.goalDaysComplete.text = "${differenceTime} hari lagi"
            holder.accumulateAmountGoal.text = goalTransaction

        }

        override fun getItemCount(): Int {
            return goals.size
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var id = 0
            val goalIcon: SimpleDraweeView = view.goalIcon
            val goalCategoryName: TextView = view.goalCategoryName
            val goalDaysComplete: TextView = view.goalDaysComplete
            val accumulateAmountGoal: TextView = view.accumulatedAmountGoal
        }
    }
}