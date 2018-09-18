package jp.shiita.basenote.util

import android.arch.lifecycle.*
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.View
import org.reactivestreams.Publisher

fun FragmentManager.replaceFragment(@IdRes containerViewId: Int, fragment: Fragment) {
    beginTransaction()
            .replace(containerViewId, fragment)
            .commit()
}

fun View.snackbar(text: String, duration: Int = Snackbar.LENGTH_LONG) =
        Snackbar.make(this, text, duration).show()

fun View.snackbar(@StringRes resId: Int, duration: Int = Snackbar.LENGTH_LONG) =
        Snackbar.make(this, resId, duration).show()

fun MutableLiveData<Boolean>.switch() {
    val current = value ?: return
    postValue(!current)
}

fun <T> LiveData<T>.observe(owner: LifecycleOwner, observer: (T) -> Unit) =
        observe(owner, Observer<T> { if (it != null) observer(it) })

fun <X, Y> LiveData<X>.switchMap(func: (X) -> LiveData<Y>) = Transformations.switchMap(this, func)

fun <T> Publisher<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)