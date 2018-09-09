package jp.shiita.basenote.data

interface NotesDataSource {

    fun getNotes(): List<Note>

    fun getNote(noteId: String): Note?

    fun saveNote(note: Note)

    fun deleteAllNotes()

    fun deleteAllNotes(tag: Int)

    fun deleteNote(noteId: String)

    fun updateNote(note: Note)
}