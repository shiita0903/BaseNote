package jp.shiita.basenote.di.module

import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.basenote.addeditnote.AddEditNoteActivity
import jp.shiita.basenote.di.ViewModelFactory
import jp.shiita.basenote.di.module.fragment.AddEditNoteModule
import jp.shiita.basenote.di.module.fragment.NotesModule
import jp.shiita.basenote.di.module.fragment.TextShareModule
import jp.shiita.basenote.notes.NotesActivity
import jp.shiita.basenote.textshare.TextShareActivity

@Module
internal abstract class ActivityModule {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @ContributesAndroidInjector(modules = [NotesModule::class])
    internal abstract fun contributeNotesActivity(): NotesActivity

    @ContributesAndroidInjector(modules = [AddEditNoteModule::class])
    internal abstract fun contributeAddEditNoteActivity(): AddEditNoteActivity

    @ContributesAndroidInjector(modules = [TextShareModule::class])
    internal abstract fun contributeTextShareActivity(): TextShareActivity
}