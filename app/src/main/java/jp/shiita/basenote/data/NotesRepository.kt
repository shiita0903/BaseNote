package jp.shiita.basenote.data

class NotesRepository(private val notesDateSource: NotesDataSource) : NotesDataSource {
    // TODO: あとで利用
    var cachedNotes: LinkedHashMap<String, Note> = LinkedHashMap()
    var cacheIsDirty = false

    override fun getNotes(): List<Note> {
        return notesDateSource.getNotes()
    }

    override fun getNote(noteId: String): Note? {
        return notesDateSource.getNote(noteId)
    }

    override fun saveNote(note: Note) {
        notesDateSource.saveNote(note)
    }

    override fun deleteAllNotes() {
        notesDateSource.deleteAllNotes()
    }

    override fun deleteAllNotes(tag: Int) {
        notesDateSource.deleteAllNotes(tag)
    }

    override fun deleteNote(noteId: String) {
        notesDateSource.deleteNote(noteId)
    }

    override fun updateNote(note: Note) {
        notesDateSource.updateNote(note)
    }
}