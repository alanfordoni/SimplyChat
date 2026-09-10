package com.spbalkan.simplychat.utils

import android.util.Log
import com.spbalkan.simplychat.auth.models.PasswordRule
import com.spbalkan.simplychat.auth.models.PasswordRuleType

fun String.isValidEmail(): Boolean {
    // Regex for basic email validation
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    Log.d("zzExt", "isValidEmail: ${emailRegex.matches(this)}")
    return emailRegex.matches(this)
}

fun String.isPasswordValid(): Boolean {
    val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")

    return passwordRegex.matches(this)
}

fun String.checkPasswordRules(rules: List<PasswordRule>): List<PasswordRule> {
    val list = mutableListOf<PasswordRule>()

    for (rule in rules) {
        when (rule.id) {
            PasswordRuleType.MIN_LENGTH -> {
                val item = PasswordRule(
                    id = PasswordRuleType.MIN_LENGTH,
                    text = rule.text,
                    isFulfilled = this.length >= 8
                )
                list.add(item)
            }

            PasswordRuleType.CONTAINS_LETTER -> {
                val item = PasswordRule(
                    id = PasswordRuleType.CONTAINS_LETTER,
                    text = rule.text,
                    isFulfilled = this.any { it.isLetter() }
                )
                list.add(item)
            }

            PasswordRuleType.CONTAINS_DIGIT -> {
                val item = PasswordRule(
                    id = PasswordRuleType.CONTAINS_DIGIT,
                    text = rule.text,
                    isFulfilled = this.any { it.isDigit() }
                )
                list.add(item)
            }
        }
    }

    return list.toList()
}