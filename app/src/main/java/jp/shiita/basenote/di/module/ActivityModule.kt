package jp.shiita.basenote.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.basenote.addeditnote.AddEditNoteActivity
import jp.shiita.basenote.di.module.fragment.AddEditNoteModule
import jp.shiita.basenote.di.module.fragment.NotesModule
import jp.shiita.basenote.di.module.fragment.TextShareModule
import jp.shiita.basenote.notes.NotesActivity
import jp.shiita.basenote.textshare.TextShareActivity

@Suppress("unused")
@Module
abstract class ActivityModule {
    @ContributesAndroidInjector(modules = [NotesModule::class])
    abstract fun contributeNotesActivity(): NotesActivity

    @ContributesAndroidInjector(modules = [AddEditNoteModule::class])
    abstract fun contributeAddEditNoteActivity(): AddEditNoteActivity

    @ContributesAndroidInjector(modules = [TextShareModule::class])
    abstract fun contributeTextShareActivity(): TextShareActivity
}