package mobile.uangku.android.models

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import mobile.uangku.android.core.DateUtils
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

open class Transaction: RealmObject() {

    @PrimaryKey
    var id: Int? = null

    var type: Int? = null
    var amount: Double = 0.0
    var name: String? = null
    var categoryId: Int? = null
    var notes: String? = null
    var created: Date? = null

    companion object {
        fun fromJSONArray(realm: Realm, responses: JSONArray): RealmList<Transaction> {
            val list = RealmList<Transaction>()
            for (i in 0..(responses.length() - 1))
                list.add(fromJSON(realm, responses.getJSONObject(i)))

            return list
        }

        fun fromJSON(realm: Realm, response: JSONObject): Transaction {
            val transaction = Transaction()

            transaction.id = response.getInt("id")
            transaction.name = response.getString("name")
            transaction.amount = response.getDouble("amount")
            transaction.notes = response.getString("notes")
            transaction.created = DateUtils.fromDateString(response.getString("created"))

            if (response.has("category_id"))
                transaction.categoryId = response.getInt("category_id")

            realm.copyToRealmOrUpdate(transaction)
            return transaction
        }

        operator fun get(id: Int): Transaction? {
            return Realm.getDefaultInstance().where(Transaction::class.java).equalTo("id", id).findFirst()
        }
    }
}