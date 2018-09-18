package jp.shiita.basenote.di.module.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.basenote.textshare.SelectNoteDialogFragment
import jp.shiita.basenote.textshare.ShareMenuDialogFragment

@Suppress("unused")
@Module
abstract class TextShareModule {
    @ContributesAndroidInjector
    abstract fun contributeShareMenuDialogFragment(): ShareMenuDialogFragment

    @ContributesAndroidInjector
    abstract fun contributeSelectNoteDialogFragment(): SelectNoteDialogFragment
}