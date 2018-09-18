package jp.shiita.basenote.di.module.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.basenote.notes.NotesFragment

@Suppress("unused")
@Module
abstract class NotesModule {
    @ContributesAndroidInjector
    abstract fun contributeNotesFragment(): NotesFragment
}