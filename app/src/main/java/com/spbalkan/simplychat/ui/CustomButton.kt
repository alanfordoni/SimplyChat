package com.spbalkan.simplychat.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.ui.theme.SimplyChatTheme

@Composable
fun CustomFilledButton(
    modifier: Modifier = Modifier,
    @StringRes textRes: Int,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = isEnabled,
        onClick = onClick
    ) {
        Text(
            text = stringResource(textRes)
        )
    }
}

@Preview
@Composable
fun CustomFilledButtonPreview() {
    SimplyChatTheme {
        CustomFilledButton(
            textRes = R.string.login,
            onClick = {}
        )
    }
}