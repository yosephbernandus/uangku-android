package mobile.uangku.android.activities.goal

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_goal_detail.*
import kotlinx.android.synthetic.main.activity_goal_detail.view.*
import kotlinx.android.synthetic.main.fragment_goal.*
import kotlinx.android.synthetic.main.fragment_goal.swipeRefreshLayout
import kotlinx.android.synthetic.main.goal_transaction_item.view.*
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import mobile.uangku.android.models.Goal
import mobile.uangku.android.models.GoalTransaction
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class GoalDetailActivity : AppCompatActivity() {

    var tab: String = "details"
    lateinit var goal: Goal
    lateinit var goalTransaction: RealmResults<GoalTransaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_detail)
        goal = Goal.get(intent.getIntExtra("id", 0))!!
        tab = intent.getStringExtra("tab")
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            sync()
        }

        sync(true)

        addGoalTransaction.setOnClickListener {
            val intent = Intent(this, EditGoalTransactionActivity::class.java)
            intent.putExtra("id", goal.id)
            startActivity(intent)
        }

        detailTab.setOnClickListener {
            tab = "details"
            detailsLayout.visibility = View.VISIBLE
            detailTabText.setTextColor(resources.getColor(R.color.white))
            detailTab.background.setColorFilter(
                Color.parseColor("#315A71"),
                PorterDuff.Mode.SRC_ATOP
            )

            // Clear transaction tab
            transactionLayout.visibility = View.GONE
            transactionTabText.setTextColor(resources.getColor(R.color.tabTextInactive))
            transactionTab.background.setColorFilter(
                Color.parseColor("#FFFFFF"),
                PorterDuff.Mode.SRC_ATOP
            )
            setupUI()
        }

        transactionTab.setOnClickListener {
            tab = "transactions"
            // Clear details tab
            detailsLayout.visibility = View.GONE
            detailTabText.setTextColor(resources.getColor(R.color.tabTextInactive))
            detailTab.background.setColorFilter(
                Color.parseColor("#FFFFFF"),
                PorterDuff.Mode.SRC_ATOP
            )

            transactionLayout.visibility = View.VISIBLE
            transactionTabText.setTextColor(resources.getColor(R.color.white))
            transactionTab.background.setColorFilter(
                Color.parseColor("#315A71"),
                PorterDuff.Mode.SRC_ATOP
            )
            setupUI()
        }
    }

    fun sync(showLoadingDialog: Boolean = false) {
        val loadingDialog = LoadingDialog(this)
        if (showLoadingDialog) loadingDialog.show()

        val request = API.createGetRequest(this, "goals/${goal.id}", null)
        request.getAsJSONObject(object : JSONObjectRequestListener {
            override fun onResponse(response: JSONObject) {
                loadingDialog.dismissIfNeeded()
                swipeRefreshLayout.isRefreshing = false

                val realm = Realm.getDefaultInstance() as Realm
                realm.executeTransactionAsync{ bgRealm ->
                    Goal.fromJSON(bgRealm, response)
                }

                setupUI()
            }

            override fun onError(error: ANError) {
                loadingDialog.dismissIfNeeded()
                swipeRefreshLayout.isRefreshing = false

                if (error.errorCode == 404)
                    finish()
                else
                    API.handleErrorResponse(this@GoalDetailActivity, error)
            }
        })
    }

    fun closeIconOnClick(view: View) {
        finish()
    }

    fun setupUI() {

        if (tab == "details") {
            detailsLayout.visibility = View.VISIBLE
            var goalTransaction = "Rp. 0 dari Rp. ${Utils.addThousandSeparator(goal!!.amount)}"
            var dateFormat =  SimpleDateFormat("yyyy-MM-dd")
            var currentDate = DateUtils.fromDateString(dateFormat.format(Date()))!!.time
            var achievementDate = goal.achievementDate!!.time
            var differenceTime = Utils.getDifferenceTime((achievementDate - currentDate), Constants.DAYS)

            if (goal!!.transactions != null){
                goalTransaction = "Rp. ${Utils.addThousandSeparator(goal!!.transactions!!.where().sum("amount").toDouble())} dari Rp. ${Utils.addThousandSeparator(goal!!.amount)}"
            }

            goalName.text = goal.name
            daysAchievmentGoal.text = "${differenceTime} hari lagi"
            accumulatedSavingAmount.text = goalTransaction
            depositAmountPerCycle.text = "Rp. ${Utils.addThousandSeparator(goal.depositAmountPerCycle)}"

            val depostiCycle = goal!!.depositCycle
            if (depostiCycle == Constants.DAY) {
                cylceDeposit.text = "Per Hari"
            } else if (depostiCycle == Constants.WEEKLY) {
                cylceDeposit.text = "Per Minggu"
            } else if (depostiCycle == Constants.MONTLY) {
                cylceDeposit.text = "Per Bulan"
            } else if (depostiCycle == Constants.YEARLY) {
                cylceDeposit.text = "Per Tahun"
            }
        } else if (tab == "transactions" && goal.transactions != null) {
            transactionLayout.visibility = View.VISIBLE
            var transactions = goal.transactions!!.where().findAll()

            goalTransactionListRecyclerView.visibility = View.VISIBLE
            goalTransactionListRecyclerView.adapter = RecyclerViewAdapter(transactions)
            goalTransactionListRecyclerView.layoutManager = LinearLayoutManager(this)
            goalTransactionListRecyclerView.isNestedScrollingEnabled = false
        }
    }

    internal inner class RecyclerViewAdapter(var transactions: RealmResults<GoalTransaction>): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.goal_transaction_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val transaction = transactions[position]
            var amount = "Rp. ${Utils.addThousandSeparator(transaction!!.amount!!)}"
            var created = DateUtils.toDisplayString(transaction!!.created!!)

            holder.id = goal!!.id!!
            holder.amountSavings.text = amount
            holder.created.text = created
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
            var id = 0
            val amountSavings: TextView = view.amountSavingsTransaction
            val created: TextView = view.transactionCreated
        }

        override fun getItemCount(): Int {
            return transactions.size
        }
    }
}