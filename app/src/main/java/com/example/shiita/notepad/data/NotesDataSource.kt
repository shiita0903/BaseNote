package com.example.shiita.notepad.data

import io.realm.Realm

/**
 * Realmの操作をするobject。Realmのインスタンスはメソッドの終了時にcloseされるため、同期はしない。
 */
object NotesDataSource {

    /**
     * ノートを全て取得して、リストにして返す
     * @return 全てのノートのリスト
     */
    fun getNotes(): List<Note> {
        Realm.getDefaultInstance().use { realm ->
            return realm.where(Note::class.java)
                    .findAll()
                    .map { realm.copyFromRealm(it) }
        }
    }

    /**
     * ノートのIDで検索し、見つかったならばそれを返す
     * @param noteId ノートのUUID
     * @return UUIDに一致したノート。見つからなければnull
     */
    fun getNote(noteId: String): Note? {
        Realm.getDefaultInstance().use { realm ->
            return realm.where(Note::class.java)
                    .equalTo(Note::id.name, noteId)
                    .findAll()  // 見つかるのは1つだが、リストのままでmapを使って加工
                    .map { realm.copyFromRealm(it) }
                    .firstOrNull()
        }
    }

    /**
     * ノートを保存する
     * @param note 保存するノート
     */
    fun saveNote(note: Note) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { realm.copyToRealm(note) }
        }
    }

    /**
     * 全てのノートを削除する
     */
    fun deleteAllNotes() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { realm.deleteAll() }
        }
    }

    /**
     * ノートのIDで検索し、見つかったならばそれを削除する
     * @param noteId ノートのUUID
     */
    fun deleteNote(noteId: String) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                realm.where(Note::class.java)
                        .equalTo(Note::id.name, noteId)
                        .findFirst()
                        ?.deleteFromRealm()     // 見つからなかった時のために"?."を使用
            }
        }
    }

    /**
     * 引数のノートのIDが存在するならば更新し、そうでなければ新規作成する
     * @param note 更新するノート
     */
    fun updateNote(note: Note) {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction { realm.copyToRealmOrUpdate(note) }
        }
    }
}