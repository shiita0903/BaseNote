package com.example.shiita.notepad.data

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class Note(
    open var title: String = "",
    open var content: String = "",
    @PrimaryKey open var id: String = UUID.randomUUID().toString()
) : RealmObject() {
    val titleForList: String
        get() = if (title.isNullOrEmpty()) content else title

    val isEmpty: Boolean
        get() = title.isNullOrEmpty() && content.isNullOrEmpty()
}
