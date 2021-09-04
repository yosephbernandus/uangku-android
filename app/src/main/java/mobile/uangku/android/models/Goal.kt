package mobile.uangku.android.models

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import mobile.uangku.android.core.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

open class Goal: RealmObject() {

    @PrimaryKey
    var id: Int? = null

    var achievementDate: Date? = null
    var categoryId: Int? = null
    var amount: Double = 0.0
    var name: String? = null
    var depositCycle: Int = 0
    var depositAmountPerCycle: Double = 0.0
    var transactions: RealmList<GoalTransaction>? = null
    var goals: RealmList<Goal>? = null

    companion object {

        fun fromJSONArray(realm: Realm, responses: JSONArray): RealmList<Goal> {
            val list = RealmList<Goal>()
            for (i in 0..(responses.length() - 1))
                list.add(fromJSON(realm, responses.getJSONObject(i)))

            return list
        }

        fun fromJSON(realm: Realm, response: JSONObject): Goal {
            val goal = Goal()

            goal.id = response.getInt("id")
            goal.name = response.getString("name")
            goal.amount = response.getDouble("amount")
            goal.achievementDate = DateUtils.fromDateString(response.getString("achievement_date"))
            goal.depositCycle = response.getInt("deposit_cycle")
            goal.depositAmountPerCycle = response.getDouble("deposit_amount_per_cycle")

            if (response.has("category_id"))
                goal.categoryId = response.getInt("category_id")

            if (response.has("transactions"))
                goal.transactions = GoalTransaction.fromJSONArray(realm, response.getJSONArray("transactions"), goal)

            realm.copyToRealmOrUpdate(goal)
            return  goal
        }

        operator fun get(id: Int): Goal? {
            return Realm.getDefaultInstance().where(Goal::class.java).equalTo("id", id).findFirst()
        }

        fun amountSavings(goals: RealmResults<Goal>): Double {
            var amountSavings = 0.0
            for (goal in goals) {
                if (goal.transactions != null) {
                    amountSavings += goal.transactions!!.where().sum("amount").toDouble()
                }
            }
            return amountSavings
        }

        enum class Cycle(val value: String) {
            NULL("null"),
            DAILY("Harian"),
            WEEKLY("Mingguan"),
            MONTHLY("Bulanan"),
            YEARLY("Tahunan");

            companion object {

                fun getStringValue(index: Int): String {
                    return getValue(index).value
                }

                fun getValue(index: Int): Cycle {
                    return values()[index]
                }
            }
        }
    }
}