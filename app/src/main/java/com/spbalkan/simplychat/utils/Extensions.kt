package com.spbalkan.simplychat.utils

import android.util.Log

fun String.isValidEmail(): Boolean {
    // Regex for basic email validation
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    Log.d("zzExt", "isValidEmail: ${emailRegex.matches(this)}")
    return emailRegex.matches(this)
}