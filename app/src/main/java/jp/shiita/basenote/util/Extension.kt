package jp.shiita.basenote.util

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.Transformations
import android.support.annotation.IdRes
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

fun View.snackbarLong(message: String) = Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()


fun <X, Y> LiveData<X>.switchMap(func: (X) -> LiveData<Y>) = Transformations.switchMap(this, func)

fun <T> Publisher<T>.toLiveData() = LiveDataReactiveStreams.fromPublisher(this)