package jp.shiita.basenote.di.module.fragment

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.shiita.basenote.di.ViewModelKey
import jp.shiita.basenote.notes.NotesFragment
import jp.shiita.basenote.notes.NotesViewModel

@Module
internal abstract class NotesModule {
    @Binds
    @IntoMap
    @ViewModelKey(NotesViewModel::class)
    internal abstract fun bindMainViewModel(viewModel: NotesViewModel): ViewModel

    @ContributesAndroidInjector
    internal abstract fun contributeNotesFragment(): NotesFragment
}