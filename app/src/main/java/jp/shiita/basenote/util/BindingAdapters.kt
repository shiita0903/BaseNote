package jp.shiita.basenote.util

import android.databinding.BindingAdapter
import android.databinding.InverseBindingAdapter
import android.support.annotation.DrawableRes
import android.support.design.widget.FloatingActionButton
import android.text.Spannable
import android.widget.EditText


@BindingAdapter("spannable")
fun EditText.setSpannable(spannable: Spannable?) {
    if (text == null) setText(spannable)
    spannable ?: return
    if (!spannable.equal(text as Spannable)) setText(spannable)
}

@InverseBindingAdapter(attribute = "spannable", event = "android:textAttrChanged")
fun EditText.getSpannable(): Spannable = text

@BindingAdapter("imageResource")
fun FloatingActionButton.setImageResource(@DrawableRes resId: Int) = setImageResource(resId)

private fun Spannable.equal(s: Spannable): Boolean {
    if (toString() != s.toString()) return false
    val spans1 = getSpans(0, length, ClickableURLSpan::class.java).toList()
    val spans2 = s.getSpans(0, s.length, ClickableURLSpan::class.java).toList()
    return spans1 == spans2
}
