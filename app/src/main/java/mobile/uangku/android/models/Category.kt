package mobile.uangku.android.models

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

open class Category : RealmObject() {

    @PrimaryKey
    var id: Int? = null
    var name: String? = null
    var logoUrl: String? = null
    var isActive: Boolean = false

    companion object {

        fun fromJSONArray(realm: Realm, responses: JSONArray): RealmList<Category> {
            for (category in realm.where(Category::class.java).findAll()) {
                category.isActive = false
                realm.copyToRealmOrUpdate(category)
            }

            val list = RealmList<Category>()
            for (i in 0..(responses.length() - 1))
                list.add(fromJSON(realm, responses.getJSONObject(i)))

            return list
        }

        fun fromJSON(realm: Realm, response: JSONObject): Category {
            val category = Category()

            category.id = response.getInt("id")
            category.name = response.getString("name")
            category.isActive = true

            if (!response.isNull("logo_url"))
                category.logoUrl = response.getString("logo_url")

            realm.copyToRealmOrUpdate(category)
            return category
        }

        operator fun get(id: Int): Category? {
            return Realm.getDefaultInstance().where(Category::class.java).equalTo("id", id).findFirst()
        }
    }
}