package jp.shiita.basenote.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import jp.shiita.basenote.BaseNoteApp
import jp.shiita.basenote.di.module.ActivityModule
import jp.shiita.basenote.di.module.DataModule
import jp.shiita.basenote.di.module.ViewModelModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    ActivityModule::class,
    ViewModelModule::class,
    DataModule::class
])
interface AppComponent : AndroidInjector<BaseNoteApp> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: BaseNoteApp): Builder
        fun build(): AppComponent
    }

    override fun inject(app: BaseNoteApp)
}