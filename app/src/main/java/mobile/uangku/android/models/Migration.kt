package mobile.uangku.android.models

import io.realm.DynamicRealm
import io.realm.RealmMigration

class Migration : RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        var oldVersion = oldVersion
        val schema = realm.schema

        oldVersion++
    }
}