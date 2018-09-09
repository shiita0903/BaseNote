package jp.shiita.basenote.di.module

import dagger.Module
import dagger.Provides
import io.realm.Realm
import jp.shiita.basenote.data.NotesDataSource
import jp.shiita.basenote.data.NotesRealmDataSource
import javax.inject.Singleton

@Module(includes = [RepositoryModule::class])
class DataModule {
    @Provides
    @Singleton
    fun provideRealm(): Realm = Realm.getDefaultInstance()

    @Provides
    @Singleton
    fun provideNotesDataSource(realm: Realm): NotesDataSource = NotesRealmDataSource(realm)
}