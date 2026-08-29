package com.spbalkan.simplychat.chat.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.viewmodels.ChatViewModel
import com.spbalkan.simplychat.ui.CustomInputTextView

@Composable
fun NewChatScreen(
    viewModel: ChatViewModel = viewModel(),
    username: String,
    uid: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChatScreenContent(
        state = state,
        username = username
    )

    LaunchedEffect(Unit) {
        viewModel.createChat(
            username = username,
            userId = uid
        )
    }
}

@Composable
fun ChatScreenContent(
    state: ChatViewModel.ChatState,
    username: String
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                CustomInputTextView(
                    modifier = Modifier
                        .weight(1f),
                    labelText = null,
                    inputText = state.inputText,
                    onInputChanged = {

                    }
                )

                IconButton(
                    onClick = {

                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_send_24),
                        contentDescription = null
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

        }
    }
}