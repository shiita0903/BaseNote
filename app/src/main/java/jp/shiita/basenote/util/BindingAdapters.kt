package jp.shiita.basenote.util

import android.databinding.BindingAdapter
import android.databinding.BindingMethod
import android.databinding.BindingMethods
import android.databinding.InverseBindingAdapter
import android.support.annotation.DrawableRes
import android.support.constraint.Guideline
import android.support.design.widget.FloatingActionButton
import android.text.Spannable
import android.widget.EditText


@BindingAdapter("spannable")
fun EditText.bindSpannable(spannable: Spannable?) {
    if (text == null) setText(spannable)
    spannable ?: return
    if (!spannable.equal(text as Spannable)) setText(spannable)
}

@InverseBindingAdapter(attribute = "spannable", event = "android:textAttrChanged")
fun EditText.inverseBindSpannable(): Spannable = text

@BindingAdapter("imageResource")
fun FloatingActionButton.bindImageResource(@DrawableRes resId: Int) = setImageResource(resId)

@Suppress("unused")
@BindingMethods(BindingMethod(type = Guideline::class, attribute = "layout_constraintGuide_percent", method = "setGuidelinePercent"))
object BindingRenameAdapters

private fun Spannable.equal(s: Spannable): Boolean {
    if (toString() != s.toString()) return false
    val spans1 = getSpans(0, length, ClickableURLSpan::class.java).toList()
    val spans2 = s.getSpans(0, s.length, ClickableURLSpan::class.java).toList()
    return spans1 == spans2
}
