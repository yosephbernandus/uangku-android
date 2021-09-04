package mobile.uangku.android.activities.goal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_goal_detail.*
import kotlinx.android.synthetic.main.activity_goal_detail.view.*
import kotlinx.android.synthetic.main.fragment_goal.*
import kotlinx.android.synthetic.main.fragment_goal.swipeRefreshLayout
import mobile.uangku.android.R
import mobile.uangku.android.core.*
import mobile.uangku.android.models.Goal
import mobile.uangku.android.models.GoalTransaction
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class GoalDetailActivity : AppCompatActivity() {

    lateinit var goal: Goal
    lateinit var goalTransaction: RealmResults<GoalTransaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_detail)
        goal = Goal.get(intent.getIntExtra("id", 0))!!
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            sync()
        }

        sync(true)
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

    fun setupUI() {
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
        depositAmountPerCycle.text = "Rp. ${Utils.addThousandSeparator(goal.depositAmountPerCycle.toDouble())}"
    }
}