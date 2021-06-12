package mobile.uangku.android.models

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import mobile.uangku.android.core.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

open class GoalTransaction : RealmObject() {

    @PrimaryKey
    var id: Int? = null
    var amount: Double? = null
    var created: Date? = null
    var goal: Goal? = null

    companion object {

        fun fromJSONArray(realm: Realm, responses: JSONArray, goal: Goal): RealmList<GoalTransaction> {
            val list = RealmList<GoalTransaction>()
            for (i in 0..(responses.length() - 1))
                list.add(fromJSON(realm, responses.getJSONObject(i), goal))

            return list
        }

        fun fromJSON(realm: Realm, transactionData: JSONObject, goal: Goal): GoalTransaction {
            val goalTransaction = GoalTransaction()

            goalTransaction.id = transactionData.getInt("id")
            goalTransaction.amount = transactionData.getDouble("amount")
            goalTransaction.created = DateUtils.fromDateString(transactionData.getString("created"))
            goalTransaction.goal = goal

            realm.copyToRealmOrUpdate(goalTransaction)
            return goalTransaction
        }

        operator fun get(id: Int): GoalTransaction? {
            return Realm.getDefaultInstance().where(GoalTransaction::class.java).equalTo("id", id).findFirst()
        }

    }
}