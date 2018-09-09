package jp.shiita.basenote

import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import jp.shiita.basenote.di.DaggerAppComponent

class BaseNoteApp : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()
        Fabric.with(this, Crashlytics())

        Realm.init(this)
        val config = RealmConfiguration.Builder()
                .name(getString(R.string.realm_name))
                .schemaVersion(1)
                .build()
        Realm.setDefaultConfiguration(config)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder()
                .application(this)
                .build()
    }
}