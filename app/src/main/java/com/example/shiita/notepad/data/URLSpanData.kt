package com.example.shiita.notepad.data

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass
open class URLSpanData(
        open var url: String = "",
        open var start: Int = 0,
        open var end: Int = 0
) : RealmObject()