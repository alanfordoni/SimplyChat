package com.spbalkan.simplychat.auth.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.auth.models.PasswordRule
import com.spbalkan.simplychat.auth.viewmodels.RegisterViewModel
import com.spbalkan.simplychat.ui.theme.GreenNephritis
import com.spbalkan.simplychat.ui.theme.SimplyChatTheme

@Composable
fun PasswordRulesView(
    list: List<PasswordRule>,
    isPasswordValid: Boolean
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isPasswordValid) GreenNephritis else Color.Red,
                shape = TooltipShape()
            )
            .padding(top = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            list.forEachIndexed { index, rule ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (rule.isFulfilled) {
                        Icon(
                            painterResource(R.drawable.ic_check_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterVertically),
                            tint = GreenNephritis
                        )
                    } else {
                        Icon(
                            painterResource(R.drawable.ic_error_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterVertically),
                            tint = Color.Red
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = rule.text,
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        color = if (rule.isFulfilled) GreenNephritis else Color.Red
                    )
                }

                if (index < list.size - 1) {
                    Spacer(Modifier.height(2.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun PasswordRulePreview() {
    val list = RegisterViewModel.passwordRules

    SimplyChatTheme {
        PasswordRulesView(
            list = list,
            isPasswordValid = false)
    }
}

