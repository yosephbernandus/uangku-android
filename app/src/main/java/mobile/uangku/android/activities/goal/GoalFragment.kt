package mobile.uangku.android.activities.goal

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
import kotlinx.android.synthetic.main.fragment_goal.*
import kotlinx.android.synthetic.main.goal_fragment_item.view.*
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import mobile.uangku.android.models.Category
import mobile.uangku.android.models.Goal
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class GoalFragment : Fragment() {

    lateinit var preferences: Preferences
    lateinit var fragmentContext: Context

    val key ="goals"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_goal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferences = Preferences(fragmentContext)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            syncGoal()
        }

        if (Sync.isNeeded(fragmentContext, key, 60))
            syncGoal()

        addGoal.setOnClickListener {
            startActivity(Intent(fragmentContext, EditGoalActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        syncGoal()
        setupUI()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context!!
    }

    fun setupUI() {
        if(!isAdded)
            return

        val goals = Realm.getDefaultInstance().where(Goal::class.java).findAll()
        if (goals.size == 0) {
            goalsListRecyclerView.visibility = View.GONE
            return
        }

        goalsListRecyclerView.visibility = View.VISIBLE
        totalAmountSavings.text = "Rp. ${Utils.addThousandSeparator(Goal.amountSavings(goals))}"
        goalsListRecyclerView.adapter = RecyclerViewAdapter(goals)
        goalsListRecyclerView.layoutManager = LinearLayoutManager(fragmentContext)
        goalsListRecyclerView.isNestedScrollingEnabled = false
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

                if (swipeRefreshLayout != null) swipeRefreshLayout.isRefreshing = false
                val realm = Realm.getDefaultInstance() as Realm

                realm.executeTransactionAsync(Realm.Transaction { bgRealm ->
                    Goal.fromJSONArray(bgRealm, response.getJSONArray("goals"))
                }, Realm.Transaction.OnSuccess {
                    Sync.setSyncTime(fragmentContext, key)
                    setupUI()
                })
            }

            override fun onError(error: ANError) {
                if (withLoadingDialog) loadingDialog!!.dismissIfNeeded()
                swipeRefreshLayout.isRefreshing = false
                API.handleErrorResponse(fragmentContext, error)
            }
        })
    }


    internal inner class RecyclerViewAdapter(var goals: RealmResults<Goal>): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.goal_fragment_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val goal = goals[position]
            var goalTransaction = "Rp. 0 dari Rp. ${Utils.addThousandSeparator(goal!!.amount)}"
            var percentageTotalSaving = "0 %"
            var dateFormat =  SimpleDateFormat("yyyy-MM-dd")
            var currentDate = DateUtils.fromDateString(dateFormat.format(Date()))!!.time
            var achievementDate = goal.achievementDate!!.time
            var differenceTime = Utils.getDifferenceTime((achievementDate - currentDate), Constants.DAYS)

            if (goal!!.transactions != null){
                goalTransaction = "Rp. ${Utils.addThousandSeparator(goal!!.transactions!!.where().sum("amount").toDouble())} dari Rp. ${Utils.addThousandSeparator(goal!!.amount)}"
                percentageTotalSaving = "${(((goal!!.transactions!!.where().sum("amount").toDouble() / goal.amount) * 100).toString())} %"
            }
            holder.id = goal!!.id!!

            if (goal != null && goal.categoryId != null) {
                var category = Category[goal.categoryId!!]
                if (category != null)
                    holder.goalIcon.setImageURI(category.logoUrl)
            }

            holder.textSavingGoal.text = goal.name
            holder.savingGoalAmount.text = "Rp. ${Utils.addThousandSeparator(goal.amount)}"
            holder.accumulatedSavingAmount.text = goalTransaction
            holder.percentageTotalSaving.text = percentageTotalSaving
            holder.daysGoalComplete.text = "${differenceTime} hari lagi"
        }

        override fun getItemCount(): Int {
            return goals.size
        }

        internal inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
            var id = 0
            val textSavingGoal: TextView = view.textSavingGoal
            val savingGoalAmount: TextView = view.savingGoalAmount
            val accumulatedSavingAmount: TextView = view.accumulatedSavingAmount
            val daysGoalComplete: TextView = view.daysGoalComplete
            val percentageTotalSaving: TextView = view.percentageTotalSaving
            val goalIcon: SimpleDraweeView = view.goalIcon

            init {
                view.setOnClickListener(this)
            }

            override fun onClick(view: View) {
                val intent = Intent(fragmentContext, GoalDetailActivity::class.java)
                intent.putExtra("id", id)
                startActivity(intent)
            }
        }
    }
}