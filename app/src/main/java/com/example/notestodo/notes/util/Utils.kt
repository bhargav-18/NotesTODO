package com.example.notestodo.notes.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

val <T> T.exhaustive: T
    get() = this

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}