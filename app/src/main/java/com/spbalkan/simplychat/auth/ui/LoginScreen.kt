package com.spbalkan.simplychat.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.auth.viewmodels.LoginViewModel
import com.spbalkan.simplychat.auth.viewmodels.LoginViewModel.ActionIntent.*
import com.spbalkan.simplychat.ui.CustomBlockingCircleLoader
import com.spbalkan.simplychat.ui.CustomFilledButton
import com.spbalkan.simplychat.ui.CustomInputTextView
import com.spbalkan.simplychat.ui.CustomPasswordInputText
import com.spbalkan.simplychat.ui.theme.SimplyChatTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.event.collect {
            when (it) {
                is LoginViewModel.LoginEvent.Success -> {
                    onLoginSuccess()
                }

                is LoginViewModel.LoginEvent.Error -> {
                    val snackTitle = it.error
                    snackbarHostState.showSnackbar(
                        message = snackTitle
                    )
                }
            }
        }
    }

    LoginScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onRegister = onNavigateToRegister
    )
}

@Composable
fun LoginScreenContent(
    state: LoginViewModel.LoginState,
    snackbarHostState: SnackbarHostState,
    onAction: (LoginViewModel.ActionIntent) -> Unit,
    onRegister: () -> Unit
) {

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            CustomFilledButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                textRes = R.string.login,
                onClick = {
                    onAction(OnLogin)
                }
            )
        }
    ) { paddingValues ->

        var isEmailDirty by rememberSaveable { mutableStateOf(false) }
        var isPasswordDirty by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomInputTextView(
                labelText = stringResource(R.string.label_email_),
                inputText = state.email,
                errorText = stringResource(R.string.error_email_not_valid),
                isError = state.emailError,
                isDirty = isEmailDirty,
                onInputChanged = {
                    onAction(OnEmailChange(it))
                    if (!isEmailDirty) isEmailDirty = true
                }
            )

            CustomPasswordInputText(
                labelText = stringResource(R.string.label_password),
                password = state.password,
                errorText = stringResource(R.string.error_password_not_valid),
                isError = state.passwordError,
                isDirty = isPasswordDirty,
                onPasswordChange = {
                    onAction(OnPasswordChange(it))
                    if (!isPasswordDirty) isPasswordDirty = true
                }
            )

            TextButton(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.End),
                onClick = { onRegister() }
            ) {
                Text(
                    modifier = Modifier.widthIn(min = 120.dp),
                    text = stringResource(R.string.or_register),
                    textAlign = TextAlign.End
                )
            }
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopCenter)
            .padding(top = 16.dp),
        snackbar = { data ->
            Snackbar(
                snackbarData = data
            )
        }
    )

    if (state.isLoading) {
        CustomBlockingCircleLoader()
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    val state = LoginViewModel.LoginState()
    SimplyChatTheme {
        LoginScreenContent(
            state = state,
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onRegister = {}
        )
    }
}