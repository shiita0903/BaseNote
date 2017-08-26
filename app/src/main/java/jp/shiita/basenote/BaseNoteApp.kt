package jp.shiita.basenote

import android.app.Application
import io.realm.Realm

class BaseNoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}