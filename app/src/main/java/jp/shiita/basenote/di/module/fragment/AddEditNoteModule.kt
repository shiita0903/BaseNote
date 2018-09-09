package jp.shiita.basenote.di.module.fragment

import android.arch.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import jp.shiita.basenote.addeditnote.AddEditNoteFragment
import jp.shiita.basenote.addeditnote.AddEditNoteViewModel
import jp.shiita.basenote.di.ViewModelKey

@Module
internal abstract class AddEditNoteModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddEditNoteViewModel::class)
    internal abstract fun bindMainViewModel(viewModel: AddEditNoteViewModel): ViewModel

    @ContributesAndroidInjector
    internal abstract fun contributeAddEditNoteFragment(): AddEditNoteFragment
}