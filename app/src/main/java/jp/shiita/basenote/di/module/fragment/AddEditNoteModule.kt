package jp.shiita.basenote.di.module.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.basenote.addeditnote.AddEditNoteFragment

@Suppress("unused")
@Module
abstract class AddEditNoteModule {
    @ContributesAndroidInjector
    abstract fun contributeAddEditNoteFragment(): AddEditNoteFragment
}