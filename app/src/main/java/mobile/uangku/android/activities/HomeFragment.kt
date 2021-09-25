package mobile.uangku.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.facebook.drawee.view.SimpleDraweeView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_tab.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.goal_fragment_item.view.*
import kotlinx.android.synthetic.main.last_goal_item.view.*
import kotlinx.android.synthetic.main.last_transaction_item.view.*
import mobile.uangku.android.R
import mobile.uangku.android.activities.goal.EditGoalActivity
import mobile.uangku.android.activities.transaction.EditTransactionActivity
import mobile.uangku.android.core.*
import mobile.uangku.android.models.Category
import mobile.uangku.android.models.Goal
import mobile.uangku.android.models.Transaction
import mobile.uangku.android.models.UserData
import org.json.JSONObject
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

    val transactions_key ="transactions"
    val goals_key ="goals"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        firstDate[Calendar.DAY_OF_MONTH] = 1
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = Preferences(fragmentContext)

        statisticTransaction.setOnClickListener {
            (fragmentContext as TabActivity).bottomNavigationView.selectedItemId = R.id.chart_tab
        }

        goalSavingsOnClick.setOnClickListener {
            startActivity(Intent(fragmentContext, EditGoalActivity::class.java))
        }

        logTransactionOnClick.setOnClickListener {
            startActivity(Intent(fragmentContext, EditTransactionActivity::class.java))
        }

        allTransactions.setOnClickListener {
            (fragmentContext as TabActivity).bottomNavigationView.selectedItemId = R.id.transactions_tab
        }

        allGoal.setOnClickListener {
            (fragmentContext as TabActivity).bottomNavigationView.selectedItemId = R.id.savings_tab
        }

        homeSwipeRefreshLayout.setOnRefreshListener {
            homeSwipeRefreshLayout.isRefreshing = true
            syncCategory()
            syncGoal()
            syncTransaction()
        }
    }

    override fun onResume() {
        super.onResume()
        syncCategory()
        syncGoal()
        syncTransaction()
        setupUI()
    }

    fun syncCategory(withLoadingDialog: Boolean = false) {
        var loadingDialog: LoadingDialog? = null
        if (withLoadingDialog) {
            loadingDialog = LoadingDialog(fragmentContext)
            loadingDialog.show()
        }

        val request = API.createGetRequest(fragmentContext, "categories", null)
        request.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                if (!isVisible) return

                if (homeSwipeRefreshLayout != null) homeSwipeRefreshLayout.isRefreshing = false
                val realm = Realm.getDefaultInstance()
                realm.executeTransactionAsync{ bgRealm ->
                    Category.fromJSONArray(bgRealm, response.getJSONArray("categories"))
                }
            }

            override fun onError(error: ANError) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                homeSwipeRefreshLayout.isRefreshing = false
                API.handleErrorResponse(fragmentContext, error)
            }
        })
    }

    fun syncTransaction(withLoadingDialog: Boolean = false) {
        var loadingDialog: LoadingDialog? = null
        if (withLoadingDialog) {
            loadingDialog = LoadingDialog(fragmentContext)
            loadingDialog.show()
        }

        val request = API.createGetRequest(fragmentContext, "transactions", null)
        request.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                if (!isVisible) return

                if (homeSwipeRefreshLayout != null) homeSwipeRefreshLayout.isRefreshing = false
                val realm = Realm.getDefaultInstance() as Realm

                realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                    Transaction.fromJSONArray(bgRealm, response.getJSONArray("transactions"))
                }, Realm.Transaction.OnSuccess {
                    Sync.setSyncTime(fragmentContext, transactions_key)
                    setupUI()
                })
            }

            override fun onError(error: ANError) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                homeSwipeRefreshLayout.isRefreshing = false
                API.handleErrorResponse(fragmentContext, error)
            }
        })
    }


    fun syncGoal(withLoadingDialog: Boolean = false) {
        var loadingDialog: LoadingDialog? = null
        if (withLoadingDialog) {
            loadingDialog = LoadingDialog(fragmentContext)
            loadingDialog.show()
        }

        val request = API.createGetRequest(fragmentContext, "goals", null)
        request.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                if (!isVisible) return

                if (homeSwipeRefreshLayout != null) homeSwipeRefreshLayout.isRefreshing = false
                val realm = Realm.getDefaultInstance() as Realm

                realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                    Goal.fromJSONArray(bgRealm, response.getJSONArray("goals"))
                }, Realm.Transaction.OnSuccess {
                    Sync.setSyncTime(fragmentContext, goals_key)
                    setupUI()
                })
            }

            override fun onError(error: ANError) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                homeSwipeRefreshLayout.isRefreshing = false
                API.handleErrorResponse(fragmentContext, error)
            }
        })
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


        var outcomeAmountTransaction = transactions.where().equalTo("type", Transaction.Type.OUTCOME.ordinal)
            .findAll().sum("amount").toDouble()
        totalOutcomeMonth.text = "Rp. ${Utils.addThousandSeparator(outcomeAmountTransaction)}"
        var averageInAmonth = outcomeAmountTransaction / 30
        averageInAmonth = averageInAmonth.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()
        averageOutcomeMonth.text = "Rp. ${Utils.addThousandSeparator(averageInAmonth)}"

        if (lastTransaction.size != 0){
            noTransactionItem.visibility = View.GONE
            listTransactionRecylerView.visibility = View.VISIBLE
            listTransactionRecylerView.adapter = RecyclerViewAdapter(lastTransaction)
            listTransactionRecylerView.layoutManager = LinearLayoutManager(fragmentContext, LinearLayoutManager.HORIZONTAL, false)
            listTransactionRecylerView.isNestedScrollingEnabled = false
        }

        if (goals.size != 0) {
            noGoalItem.visibility = View.GONE
            listGoalRecyclerView.visibility = View.VISIBLE
            listGoalRecyclerView.adapter = RecyclerViewAdapterGoal(goals)
            listGoalRecyclerView.layoutManager = LinearLayoutManager(fragmentContext)
            listGoalRecyclerView.isNestedScrollingEnabled = false
        }
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
                if (category != null) {
                    holder.categoryNameTextView.text = category.name
                    holder.categoryIcon.setImageURI(category.logoUrl)
                }
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
            val goalIcon: SimpleDraweeView = view.categoryGoalIcon
            val goalCategoryName: TextView = view.goalCategoryName
            val goalDaysComplete: TextView = view.goalDaysComplete
            val accumulateAmountGoal: TextView = view.accumulatedAmountGoal
        }
    }
}