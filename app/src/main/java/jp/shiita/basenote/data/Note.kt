package jp.shiita.basenote.data

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.text.SimpleDateFormat
import java.util.*

@RealmClass
open class Note(
    open var title: String = "",
    open var content: String = "",
    open var urlSpanList: RealmList<URLSpanData> = RealmList(),
    open var tag: Int = 0,
    open var date: Long = Calendar.getInstance().time.time,
    @PrimaryKey open var id: String = UUID.randomUUID().toString()
) : RealmObject() {
    val titleForList: String
        get() = if (title.isBlank()) content.trim() else title.trim()

    val isEmpty: Boolean
        get() = title.isBlank() && content.isBlank()

    companion object {
        @Ignore val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
    }
}
