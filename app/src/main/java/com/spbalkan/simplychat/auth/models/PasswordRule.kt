package com.spbalkan.simplychat.auth.models

data class PasswordRule(
    val id: PasswordRuleType,
    val text: String,
    val isFulfilled: Boolean
)

enum class PasswordRuleType {
    CONTAINS_LETTER,
    CONTAINS_DIGIT,
    MIN_LENGTH
}
