package mobile.uangku.android.core

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.exceptions.RealmMigrationNeededException
import mobile.uangku.android.models.Migration

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .schemaVersion(0)
                .migration(Migration())
                .build()
        try {
            Realm.getInstance(realmConfig)
        } catch (e: RealmMigrationNeededException) {
            e.printStackTrace()
            Realm.deleteRealm(realmConfig)
            Preferences(this).clear()
        }
        Realm.setDefaultConfiguration(realmConfig)
    }
}