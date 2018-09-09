package jp.shiita.basenote.di.module.fragment

import dagger.Module
import dagger.android.ContributesAndroidInjector
import jp.shiita.basenote.textshare.SelectNoteDialogFragment
import jp.shiita.basenote.textshare.ShareMenuDialogFragment

@Module
internal abstract class TextShareModule {
    @ContributesAndroidInjector
    internal abstract fun contributeShareMenuDialogFragment(): ShareMenuDialogFragment

    @ContributesAndroidInjector
    internal abstract fun contributeSelectNoteDialogFragment(): SelectNoteDialogFragment
}