package jp.shiita.basenote.di.module

import dagger.Module
import dagger.Provides
import jp.shiita.basenote.data.NotesDataSource
import jp.shiita.basenote.data.NotesRepository
import javax.inject.Singleton

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun provideNotesRepository(notesDataSource: NotesDataSource) = NotesRepository(notesDataSource)
}