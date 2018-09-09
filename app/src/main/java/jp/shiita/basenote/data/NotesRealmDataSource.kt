package jp.shiita.basenote.data

import io.realm.Realm

class NotesRealmDataSource(private val realm: Realm) : NotesDataSource {

    /**
     * ノートを全て取得して、リストにして返す
     * @return 全てのノートのリスト
     */
    override fun getNotes(): List<Note> = realm.where(Note::class.java)
            .findAll()
            .map { realm.copyFromRealm(it) }
            .sortedByDescending { it.date }     // 日付の降順

    /**
     * ノートのIDで検索し、見つかったならばそれを返す
     * @param noteId ノートのUUID
     * @return UUIDに一致したノート。見つからなければnull
     */
    override fun getNote(noteId: String): Note? = realm.where(Note::class.java)
            .equalTo(Note::id.name, noteId)
            .findAll()                          // 見つかるのは1つだが、リストのままでmapを使って加工
            .map { realm.copyFromRealm(it) }
            .firstOrNull()

    /**
     * ノートを保存する
     * @param note 保存するノート
     */
    override fun saveNote(note: Note) = realm.executeTransaction { realm.copyToRealm(note) }

    /**
     * 全てのノートを削除する
     */
    override fun deleteAllNotes() = realm.executeTransaction { realm.deleteAll() }

    /**
     * 特定のタグを持つノートを全て削除する
     * @param tag 削除するノートのタグ
     */
    override fun deleteAllNotes(tag: Int) = realm.executeTransaction {
        realm.where(Note::class.java)
                .equalTo(Note::tag.name, tag)
                .findAll()
                .forEach { it.deleteFromRealm() }
    }

    /**
     * ノートのIDで検索し、見つかったならばそれを削除する
     * @param noteId ノートのUUID
     */
    override fun deleteNote(noteId: String) = realm.executeTransaction {
        realm.where(Note::class.java)
                .equalTo(Note::id.name, noteId)
                .findFirst()
                ?.deleteFromRealm()     // 見つからなかった時のために"?."を使用
    }

    /**
     * 引数のノートのIDが存在するならば更新し、そうでなければ新規作成する
     * @param note 更新するノート
     */
    override fun updateNote(note: Note) = realm.executeTransaction { realm.copyToRealmOrUpdate(note) }
}