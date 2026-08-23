package com.spbalkan.simplychat.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.ui.CustomFilledButton
import com.spbalkan.simplychat.ui.theme.SimplyChatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                }
            )
        }, bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomFilledButton(
                    textRes = R.string.register,
                    onClick = {

                    }
                )

                CustomFilledButton(
                    textRes = R.string.login,
                    onClick = {

                    }
                )
            }
        }

    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues))
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    SimplyChatTheme {
        WelcomeScreen(
            onNavigateToRegister = {},
            onNavigateToLogin = {}
        )
    }
}

