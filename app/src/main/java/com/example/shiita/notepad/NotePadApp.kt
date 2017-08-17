package com.example.shiita.notepad

import android.app.Application
import io.realm.Realm

class NotePadApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}