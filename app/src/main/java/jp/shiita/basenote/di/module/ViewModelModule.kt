package jp.shiita.basenote.di.module

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import jp.shiita.basenote.addeditnote.AddEditNoteViewModel
import jp.shiita.basenote.di.ViewModelFactory
import jp.shiita.basenote.di.ViewModelKey
import jp.shiita.basenote.notes.NotesViewModel

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddEditNoteViewModel::class)
    abstract fun bindAddEditNoteViewModel(viewModel: AddEditNoteViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotesViewModel::class)
    abstract fun bindNotesViewModel(viewModel: NotesViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}